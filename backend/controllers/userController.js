const crypto = require('crypto');
const { pool } = require('../config/db');

// Sync user profile
exports.syncProfile = async (req, res, next) => {
  try {
    const { phone, name, email } = req.body;
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone number is required' });
    }

    const now = Date.now();
    let userRes = await pool.query('SELECT * FROM users WHERE phone = $1;', [phone]);
    let user;

    if (userRes.rows.length > 0) {
      user = userRes.rows[0];
      const updateRes = await pool.query(
        `UPDATE users SET 
           last_login_at = $1,
           name = COALESCE(NULLIF($2, ''), name),
           email = COALESCE(NULLIF($3, ''), email)
         WHERE phone = $4 RETURNING *;`,
        [now, name, email, phone]
      );
      user = updateRes.rows[0];
    } else {
      const userId = crypto.randomUUID();
      const insertRes = await pool.query(
        `INSERT INTO users (id, phone, name, email, wallet_balance, created_at, last_login_at)
         VALUES ($1, $2, $3, $4, 100.0, $5, $5) RETURNING *;`,
        [userId, phone, name, email, now]
      );
      user = insertRes.rows[0];
    }

    res.json({
      success: true,
      data: {
        id: 1, // Room local auto-gen matches integer
        phone: user.phone,
        name: user.name,
        email: user.email,
        avatarUrl: user.avatar_url || '',
        sessionToken: user.session_token || '',
        isVerified: user.is_verified || false,
        defaultAddressId: user.default_address_id || -1,
        isGoldMember: user.is_gold_member || false,
        walletBalance: parseFloat(user.wallet_balance),
        createdAt: parseInt(user.created_at),
        lastLoginAt: parseInt(user.last_login_at)
      },
      message: 'Profile synced successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Update legacy location format (optional but kept for backwards compatibility)
exports.updateLocation = async (req, res, next) => {
  try {
    const { userId, lat, lng, address } = req.body;
    if (!userId) {
      return res.status(400).json({ success: false, message: 'User ID is required' });
    }
    const updatedAt = Date.now();
    await pool.query(
      `INSERT INTO users (id, phone, address, latitude, longitude, updated_at, created_at, last_login_at)
       VALUES ($1, $1, $2, $3, $4, $5, $5, $5)
       ON CONFLICT (id) 
       DO UPDATE SET 
         address = EXCLUDED.address,
         latitude = EXCLUDED.latitude,
         longitude = EXCLUDED.longitude,
         updated_at = EXCLUDED.updated_at
       RETURNING *;`,
      [userId, address || '', lat || 0.0, lng || 0.0, updatedAt]
    );
    res.json({ success: true, data: null, message: 'Location updated successfully', cached: false });
  } catch (error) {
    next(error);
  }
};

// Wallet: Get balance and transactions
exports.getWalletTransactions = async (req, res, next) => {
  try {
    const { phone } = req.params;
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone is required' });
    }

    const userRes = await pool.query('SELECT wallet_balance FROM users WHERE phone = $1;', [phone]);
    if (userRes.rows.length === 0) {
      // If user does not exist on remote db yet, create it on-demand
      const userId = crypto.randomUUID();
      const now = Date.now();
      const insertRes = await pool.query(
        `INSERT INTO users (id, phone, name, wallet_balance, created_at, last_login_at)
         VALUES ($1, $2, $2, 100.0, $3, $3) RETURNING *;`,
        [userId, phone, now]
      );
      userRes.rows.push(insertRes.rows[0]);
    }

    const txRes = await pool.query(
      'SELECT * FROM wallet_transactions WHERE user_id = $1 ORDER BY timestamp DESC;',
      [phone]
    );

    const formattedTxs = txRes.rows.map(tx => ({
      id: 0, // client auto-generates Room ID, we keep 0 for autogen compatibility
      type: tx.type,
      amount: parseFloat(tx.amount),
      description: tx.description,
      timestamp: parseInt(tx.timestamp),
      memberName: tx.member_name || ''
    }));

    res.json({
      success: true,
      data: {
        walletBalance: parseFloat(userRes.rows[0].wallet_balance),
        transactions: formattedTxs
      },
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Wallet: Create new transaction (deposit, payment, etc.)
exports.addWalletTransaction = async (req, res, next) => {
  try {
    const { phone } = req.params;
    const { type, amount, description, memberName } = req.body;

    if (!phone || !type || amount === undefined) {
      return res.status(400).json({ success: false, message: 'Required fields missing' });
    }

    const txId = crypto.randomUUID();
    const timestamp = Date.now();

    // Start a transaction block
    const client = await pool.connect();
    try {
      await client.query('BEGIN');

      const userRes = await client.query('SELECT wallet_balance FROM users WHERE phone = $1;', [phone]);
      if (userRes.rows.length === 0) {
        throw new Error('User not found');
      }

      let balance = parseFloat(userRes.rows[0].wallet_balance);
      if (type === 'Deposit' || type === 'Refund' || type === 'Cashback') {
        balance += parseFloat(amount);
      } else {
        balance -= parseFloat(amount);
      }

      // Update user balance
      await client.query('UPDATE users SET wallet_balance = $1 WHERE phone = $2;', [balance, phone]);

      // Insert transaction record
      const insertTxQuery = `
        INSERT INTO wallet_transactions (id, user_id, type, amount, description, timestamp, member_name)
        VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *;
      `;
      const txResult = await client.query(insertTxQuery, [
        txId, phone, type, amount, description || '', timestamp, memberName || ''
      ]);

      await client.query('COMMIT');

      res.json({
        success: true,
        data: {
          walletBalance: balance,
          transaction: {
            id: 0,
            type: txResult.rows[0].type,
            amount: parseFloat(txResult.rows[0].amount),
            description: txResult.rows[0].description,
            timestamp: parseInt(txResult.rows[0].timestamp),
            memberName: txResult.rows[0].member_name || ''
          }
        },
        message: 'Transaction saved successfully',
        cached: false
      });
    } catch (e) {
      await client.query('ROLLBACK');
      throw e;
    } finally {
      client.release();
    }
  } catch (error) {
    next(error);
  }
};

// Saved Addresses: Get all addresses
exports.getAddresses = async (req, res, next) => {
  try {
    const { phone } = req.params;
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone is required' });
    }

    const addrRes = await pool.query(
      'SELECT * FROM saved_addresses WHERE user_id = $1 ORDER BY is_default DESC, created_at DESC;',
      [phone]
    );

    const formattedList = addrRes.rows.map(addr => ({
      id: 0, // client auto-generates Room ID, we keep 0 for autogen compatibility
      userId: 1,
      label: addr.label,
      fullAddress: addr.full_address,
      latitude: parseFloat(addr.latitude),
      longitude: parseFloat(addr.longitude),
      isDefault: addr.is_default,
      createdAt: parseInt(addr.created_at)
    }));

    res.json({ success: true, data: formattedList, message: 'Success', cached: false });
  } catch (error) {
    next(error);
  }
};

// Saved Addresses: Add new address
exports.addAddress = async (req, res, next) => {
  try {
    const { phone } = req.params;
    const { label, fullAddress, latitude, longitude, isDefault } = req.body;

    if (!phone || !label || !fullAddress || latitude === undefined || longitude === undefined) {
      return res.status(400).json({ success: false, message: 'Required fields missing' });
    }

    const addrId = crypto.randomUUID();
    const createdAt = Date.now();

    const client = await pool.connect();
    try {
      await client.query('BEGIN');

      if (isDefault) {
        // Clear previous defaults
        await client.query('UPDATE saved_addresses SET is_default = FALSE WHERE user_id = $1;', [phone]);
      }

      const queryText = `
        INSERT INTO saved_addresses (id, user_id, label, full_address, latitude, longitude, is_default, created_at)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8) RETURNING *;
      `;

      const result = await client.query(queryText, [
        addrId, phone, label, fullAddress, latitude, longitude, isDefault || false, createdAt
      ]);

      await client.query('COMMIT');

      const addr = result.rows[0];
      res.json({
        success: true,
        data: {
          id: 0,
          userId: 1,
          label: addr.label,
          fullAddress: addr.full_address,
          latitude: parseFloat(addr.latitude),
          longitude: parseFloat(addr.longitude),
          isDefault: addr.is_default,
          createdAt: parseInt(addr.created_at)
        },
        message: 'Address saved successfully',
        cached: false
      });
    } catch (e) {
      await client.query('ROLLBACK');
      throw e;
    } finally {
      client.release();
    }
  } catch (error) {
    next(error);
  }
};

// Saved Addresses: Delete address
exports.deleteAddress = async (req, res, next) => {
  try {
    const { phone, id } = req.params;
    if (!phone || !id) {
      return res.status(400).json({ success: false, message: 'Required fields missing' });
    }

    await pool.query('DELETE FROM saved_addresses WHERE user_id = $1 AND id = $2;', [phone, id]);

    res.json({ success: true, data: null, message: 'Address deleted successfully', cached: false });
  } catch (error) {
    next(error);
  }
};
