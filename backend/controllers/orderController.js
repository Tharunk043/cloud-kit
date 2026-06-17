const crypto = require('crypto');
const { pool } = require('../config/db');

// Helper to normalize phone numbers to their last 10 digits
const cleanPhone = (phone) => {
  if (!phone) return '';
  const digits = phone.replace(/\D/g, '');
  return digits.slice(-10);
};

// Helper to format database orders into Kotlin client DTOs
const formatOrder = (dbOrder) => {
  if (!dbOrder) return null;
  return {
    id: dbOrder.id,
    userId: dbOrder.user_id,
    restaurantId: dbOrder.restaurant_id,
    items: JSON.parse(dbOrder.items_json),
    totalAmount: parseFloat(dbOrder.total_amount),
    status: dbOrder.status,
    deliveryAddress: dbOrder.delivery_address,
    paymentMethod: dbOrder.payment_method,
    driverLat: dbOrder.driver_lat,
    driverLng: dbOrder.driver_lng,
    customerLat: dbOrder.customer_lat,
    customerLng: dbOrder.customer_lng,
    restaurantLat: dbOrder.restaurant_lat,
    restaurantLng: dbOrder.restaurant_lng,
    createdAt: parseInt(dbOrder.created_at),
    ratingGiven: parseFloat(dbOrder.rating_given || 0.0),
    reviewText: dbOrder.review_text || '',
    reviewSentiment: dbOrder.review_sentiment || 'Neutral',
    driverName: dbOrder.driver_name || '',
    driverPhone: dbOrder.driver_phone || ''
  };
};

// Place a new order
exports.placeOrder = async (req, res, next) => {
  try {
    const {
      userId: bodyUserId,
      restaurantId,
      items,
      totalAmount,
      deliveryAddress,
      paymentMethod,
      customerLat,
      customerLng,
      restaurantLat,
      restaurantLng
    } = req.body;

    const userId = cleanPhone(bodyUserId || req.user || 'guest');
    const orderId = crypto.randomUUID();
    const itemsJson = JSON.stringify(items);
    const createdAt = Date.now();

    const client = await pool.connect();
    try {
      await client.query('BEGIN');

      // If payment method is Wallet or Family Wallet, verify and deduct balance in database
      if (paymentMethod === 'Wallet' || paymentMethod === 'Family Wallet') {
        const userRes = await client.query('SELECT wallet_balance FROM users WHERE phone = $1;', [userId]);
        if (userRes.rows.length === 0) {
          throw new Error(`User profile for phone '${userId}' not found. Please login first.`);
        }
        const balance = parseFloat(userRes.rows[0].wallet_balance);
        if (balance < parseFloat(totalAmount)) {
          throw new Error(`Insufficient wallet balance. Available: $${balance.toFixed(2)}, Required: $${parseFloat(totalAmount).toFixed(2)}`);
        }
        const newBalance = balance - parseFloat(totalAmount);

        // Update user balance
        await client.query('UPDATE users SET wallet_balance = $1 WHERE phone = $2;', [newBalance, userId]);

        // Insert wallet transaction record
        const txId = crypto.randomUUID();
        const txType = paymentMethod === 'Family Wallet' ? 'FamilyDebit' : 'Payment';
        const txDesc = `Payment for order at restaurant #${restaurantId}`;
        await client.query(
          `INSERT INTO wallet_transactions (id, user_id, type, amount, description, timestamp)
           VALUES ($1, $2, $3, $4, $5, $6);`,
          [txId, userId, txType, totalAmount, txDesc, createdAt]
        );
      }

      // Insert the order
      const queryText = `
        INSERT INTO orders (
          id, user_id, restaurant_id, items_json, total_amount, status,
          delivery_address, payment_method, driver_lat, driver_lng,
          customer_lat, customer_lng, restaurant_lat, restaurant_lng, created_at
        ) VALUES (
          $1, $2, $3, $4, $5, 'Placed', $6, $7, $8, $9, $10, $11, $12, $13, $14
        ) RETURNING *;
      `;

      const result = await client.query(queryText, [
        orderId, userId, restaurantId, itemsJson, totalAmount,
        deliveryAddress || '', paymentMethod || '', restaurantLat, restaurantLng,
        customerLat, customerLng, restaurantLat, restaurantLng, createdAt
      ]);

      await client.query('COMMIT');

      const formatted = formatOrder(result.rows[0]);
      console.log(`[Order] Placed: orderId=${orderId}, userId=${userId}, amount=${totalAmount}, payment=${paymentMethod}`);

      res.json({
        success: true,
        data: formatted,
        message: 'Order placed successfully',
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

// Get orders with optional filters (userId, restaurantId, status, unassigned)
exports.getOrders = async (req, res, next) => {
  try {
    const { userId: rawUserId, restaurantId, status, unassigned } = req.query;
    const userId = cleanPhone(rawUserId);
    
    let queryText = 'SELECT * FROM orders';
    const params = [];
    const conditions = [];

    if (userId) {
      params.push(userId);
      conditions.push(`user_id = $${params.length}`);
    }
    if (restaurantId) {
      params.push(restaurantId);
      conditions.push(`restaurant_id = $${params.length}`);
    }
    if (status) {
      const statusList = status.split(',');
      const placeholders = statusList.map(s => {
        params.push(s);
        return `$${params.length}`;
      }).join(', ');
      conditions.push(`status IN (${placeholders})`);
    }
    if (unassigned === 'true') {
      conditions.push(`(driver_name IS NULL OR driver_name = '')`);
    }

    if (conditions.length > 0) {
      queryText += ' WHERE ' + conditions.join(' AND ');
    }
    queryText += ' ORDER BY created_at DESC;';

    const result = await pool.query(queryText, params);
    const formattedList = result.rows.map(formatOrder);

    res.json({
      success: true,
      data: formattedList,
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Track order status (retrieve direct database state without simulation)
exports.trackOrder = async (req, res, next) => {
  try {
    const id = req.params.id;
    const result = await pool.query('SELECT * FROM orders WHERE id = $1;', [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: `Order not found with id: ${id}`
      });
    }

    const formatted = formatOrder(result.rows[0]);

    res.json({
      success: true,
      data: formatted,
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Update order status manually (supports coordinates as well)
exports.updateStatus = async (req, res, next) => {
  try {
    const id = req.params.id;
    const { status, driverLat, driverLng } = req.body;

    let queryText = 'UPDATE orders SET status = $1';
    const params = [status];

    if (driverLat !== undefined && driverLng !== undefined) {
      params.push(parseFloat(driverLat), parseFloat(driverLng));
      queryText += `, driver_lat = $2, driver_lng = $3`;
    }

    params.push(id);
    queryText += ` WHERE id = $${params.length} RETURNING *;`;

    const result = await pool.query(queryText, params);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: `Order not found with id: ${id}`
      });
    }

    const formatted = formatOrder(result.rows[0]);
    console.log(`[Order] Status updated: ${id} -> ${status} (lat=${driverLat}, lng=${driverLng})`);

    res.json({
      success: true,
      data: formatted,
      message: 'Order status updated successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Accept order as rider
exports.acceptRider = async (req, res, next) => {
  try {
    const id = req.params.id;
    const { driverName, driverPhone, driverLat, driverLng } = req.body;

    const result = await pool.query(`
      UPDATE orders SET
        status = CASE WHEN status IN ('Placed', 'Accepted') THEN 'Confirmed' ELSE status END,
        driver_name = $1,
        driver_phone = $2,
        driver_lat = $3,
        driver_lng = $4
      WHERE id = $5 AND (driver_name IS NULL OR driver_name = '' OR driver_name = 'Dash Rider' OR driver_name = 'Professional Rider' OR driver_name = $1)
      RETURNING *;
    `, [driverName || '', driverPhone || '', parseFloat(driverLat || 0.0), parseFloat(driverLng || 0.0), id]);

    if (result.rows.length === 0) {
      const checkRes = await pool.query('SELECT driver_name FROM orders WHERE id = $1;', [id]);
      if (checkRes.rows.length > 0) {
        return res.status(409).json({
          success: false,
          message: `Order already accepted by another rider: ${checkRes.rows[0].driver_name}`
        });
      }
      return res.status(404).json({
        success: false,
        message: `Order not found with id: ${id}`
      });
    }

    const formatted = formatOrder(result.rows[0]);
    console.log(`[Order] Rider accepted order ${id}: rider=${driverName}`);

    res.json({
      success: true,
      data: formatted,
      message: 'Rider accepted order successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Submit review for an order
exports.submitOrderReview = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { rating, reviewText, sentiment } = req.body;

    if (!id || rating === undefined) {
      return res.status(400).json({ success: false, message: 'Order ID and rating are required' });
    }

    const result = await pool.query(
      `UPDATE orders SET
         rating_given = $1,
         review_text = $2,
         review_sentiment = $3
       WHERE id = $4
       RETURNING *;`,
      [rating, reviewText || '', sentiment || 'Neutral', id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    const formatted = formatOrder(result.rows[0]);
    console.log(`[Order] Review submitted for ${id}: rating=${rating}, sentiment=${sentiment}`);

    res.json({
      success: true,
      data: formatted,
      message: 'Review submitted successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};
