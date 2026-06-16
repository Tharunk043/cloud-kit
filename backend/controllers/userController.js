const crypto = require('crypto');
const { pool } = require('../config/db');

// Helper to normalize phone numbers to their last 10 digits
const cleanPhone = (phone) => {
  if (!phone) return '';
  // Remove all non-digit characters
  const digits = phone.replace(/\D/g, '');
  // Extract the last 10 digits
  return digits.slice(-10);
};

// Helper to format a DB user row into the Kotlin DTO shape
const formatUser = (user) => ({
  phone: user.phone,
  name: user.name || '',
  email: user.email || '',
  avatarUrl: user.avatar_url || '',
  sessionToken: user.session_token || '',
  isVerified: user.is_verified || false,
  defaultAddressId: user.default_address_id || -1,
  isGoldMember: user.is_gold_member || false,
  walletBalance: parseFloat(user.wallet_balance || 0),
  createdAt: parseInt(user.created_at || 0),
  lastLoginAt: parseInt(user.last_login_at || 0),
  role: user.role || 'Customer'
});

// GET /api/users/:phone — fetch user profile
exports.getProfile = async (req, res, next) => {
  try {
    const phone = cleanPhone(req.params.phone);
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone is required' });
    }
    const userRes = await pool.query('SELECT * FROM users WHERE phone = $1;', [phone]);
    if (userRes.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, data: formatUser(userRes.rows[0]), message: 'Success', cached: false });
  } catch (error) {
    next(error);
  }
};

// POST /api/users/profile — create or update user (upsert by phone)
exports.syncProfile = async (req, res, next) => {
  try {
    const { phone: rawPhone, name, email, role } = req.body;
    const phone = cleanPhone(rawPhone);
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone number is required' });
    }

    const now = Date.now();
    const userId = crypto.randomUUID();

    // Upsert: insert new user OR update last_login + name/email/role if phone already exists
    const upsertRes = await pool.query(
      `INSERT INTO users (id, phone, name, email, wallet_balance, is_verified, created_at, last_login_at, updated_at, role)
       VALUES ($1, $2, $3, $4, 100.0, TRUE, $5, $5, $5, $6)
       ON CONFLICT (phone) DO UPDATE SET
         last_login_at = EXCLUDED.last_login_at,
         name = CASE WHEN EXCLUDED.name <> '' THEN EXCLUDED.name ELSE users.name END,
         email = CASE WHEN EXCLUDED.email <> '' THEN EXCLUDED.email ELSE users.email END,
         role = CASE WHEN EXCLUDED.role <> 'Customer' THEN EXCLUDED.role ELSE users.role END,
         is_verified = TRUE
       RETURNING *;`,
      [userId, phone, name || '', email || '', now, role || 'Customer']
    );

    const user = upsertRes.rows[0];
    console.log(`[User] Profile synced: phone=${phone}, name=${user.name}, role=${user.role}, walletBalance=${user.wallet_balance}`);

    res.json({
      success: true,
      data: formatUser(user),
      message: 'Profile synced successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// PUT /api/users/location — update legacy location format
exports.updateLocation = async (req, res, next) => {
  try {
    const { userId, lat, lng, address } = req.body;
    const phone = cleanPhone(userId);
    if (!phone) {
      return res.status(400).json({ success: false, message: 'User ID (phone) is required' });
    }
    const updatedAt = Date.now();
    await pool.query(
      `UPDATE users SET
         address = $1,
         latitude = $2,
         longitude = $3,
         updated_at = $4
       WHERE phone = $5;`,
      [address || '', lat || 0.0, lng || 0.0, updatedAt, phone]
    );
    res.json({ success: true, data: null, message: 'Location updated successfully', cached: false });
  } catch (error) {
    next(error);
  }
};

// GET /api/users/:phone/wallet — get balance and transactions
exports.getWalletTransactions = async (req, res, next) => {
  try {
    const phone = cleanPhone(req.params.phone);
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone is required' });
    }

    // Ensure user exists (auto-create if not)
    let userRes = await pool.query('SELECT wallet_balance FROM users WHERE phone = $1;', [phone]);
    if (userRes.rows.length === 0) {
      const userId = crypto.randomUUID();
      const now = Date.now();
      const insertRes = await pool.query(
        `INSERT INTO users (id, phone, name, wallet_balance, is_verified, created_at, last_login_at, updated_at)
         VALUES ($1, $2, $2, 100.0, TRUE, $3, $3, $3)
         ON CONFLICT (phone) DO UPDATE SET last_login_at = EXCLUDED.last_login_at
         RETURNING *;`,
        [userId, phone, now]
      );
      userRes = { rows: insertRes.rows };
    }

    const txRes = await pool.query(
      'SELECT * FROM wallet_transactions WHERE user_id = $1 ORDER BY timestamp DESC;',
      [phone]
    );

    const formattedTxs = txRes.rows.map(tx => ({
      id: 0,
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

// POST /api/users/:phone/wallet — create new transaction (deposit, payment, etc.)
exports.addWalletTransaction = async (req, res, next) => {
  try {
    const phone = cleanPhone(req.params.phone);
    const { type, amount, description, memberName } = req.body;

    if (!phone || !type || amount === undefined) {
      return res.status(400).json({ success: false, message: 'Required fields missing' });
    }

    const txId = crypto.randomUUID();
    const timestamp = Date.now();

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
      const txResult = await client.query(
        `INSERT INTO wallet_transactions (id, user_id, type, amount, description, timestamp, member_name)
         VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *;`,
        [txId, phone, type, amount, description || '', timestamp, memberName || '']
      );

      await client.query('COMMIT');
      console.log(`[Wallet] Transaction: phone=${phone}, type=${type}, amount=${amount}, newBalance=${balance}`);

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

// GET /api/users/:phone/addresses — get all saved addresses
exports.getAddresses = async (req, res, next) => {
  try {
    const phone = cleanPhone(req.params.phone);
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone is required' });
    }

    const addrRes = await pool.query(
      'SELECT * FROM saved_addresses WHERE user_id = $1 ORDER BY is_default DESC, created_at DESC;',
      [phone]
    );

    const formattedList = addrRes.rows.map(addr => ({
      id: 0,
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

// POST /api/users/:phone/addresses — add new address
exports.addAddress = async (req, res, next) => {
  try {
    const phone = cleanPhone(req.params.phone);
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
        await client.query('UPDATE saved_addresses SET is_default = FALSE WHERE user_id = $1;', [phone]);
      }

      const result = await client.query(
        `INSERT INTO saved_addresses (id, user_id, label, full_address, latitude, longitude, is_default, created_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8) RETURNING *;`,
        [addrId, phone, label, fullAddress, latitude, longitude, isDefault || false, createdAt]
      );

      await client.query('COMMIT');

      const addr = result.rows[0];
      console.log(`[Address] Saved for phone=${phone}: label=${label}, address=${fullAddress}`);

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

// DELETE /api/users/:phone/addresses/:id — delete address
exports.deleteAddress = async (req, res, next) => {
  try {
    const phone = cleanPhone(req.params.phone);
    const { id } = req.params;
    if (!phone || !id) {
      return res.status(400).json({ success: false, message: 'Required fields missing' });
    }

    await pool.query('DELETE FROM saved_addresses WHERE user_id = $1 AND id = $2;', [phone, id]);
    console.log(`[Address] Deleted id=${id} for phone=${phone}`);

    res.json({ success: true, data: null, message: 'Address deleted successfully', cached: false });
  } catch (error) {
    next(error);
  }
};
