const crypto = require('crypto');
const { pool } = require('../config/db');

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
    reviewSentiment: dbOrder.review_sentiment || 'Neutral'
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

    const userId = bodyUserId || req.user || 'guest';
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

// Get orders for a user
exports.getUserOrders = async (req, res, next) => {
  try {
    const userId = req.query.userId || req.user || 'guest';

    const result = await pool.query(
      'SELECT * FROM orders WHERE user_id = $1 ORDER BY created_at DESC;',
      [userId]
    );

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

// Track order status and simulate driver coordinates updating
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

    const order = result.rows[0];
    let status = order.status;
    let driverLat = order.driver_lat;
    let driverLng = order.driver_lng;

    // Dynamic state machine simulation for order tracking
    if (status === 'Placed') {
      status = 'Confirmed';
      console.log(`[Order] ${id} -> Confirmed`);
    } else if (status === 'Confirmed') {
      status = 'Cooking';
      console.log(`[Order] ${id} -> Cooking`);
    } else if (status === 'Cooking') {
      status = 'OutForDelivery';
      driverLat = (order.restaurant_lat + order.customer_lat) / 2;
      driverLng = (order.restaurant_lng + order.customer_lng) / 2;
      console.log(`[Order] ${id} -> OutForDelivery`);
    } else if (status === 'OutForDelivery') {
      const latDiff = order.customer_lat - driverLat;
      const lngDiff = order.customer_lng - driverLng;
      const distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);

      // dynamic step size to guarantee delivery completes within 12 steps (approx 1 minute)
      const step = Math.max(0.0008, distance / 12); 
      if (distance <= step || (Math.abs(latDiff) < step && Math.abs(lngDiff) < step)) {
        status = 'Delivered';
        driverLat = order.customer_lat;
        driverLng = order.customer_lng;
        console.log(`[Order] ${id} -> Delivered`);
      } else {
        driverLat = driverLat + (latDiff / distance) * step;
        driverLng = driverLng + (lngDiff / distance) * step;
      }
    }

    const updateResult = await pool.query(`
      UPDATE orders SET
        status = $1,
        driver_lat = $2,
        driver_lng = $3
      WHERE id = $4
      RETURNING *;
    `, [status, driverLat, driverLng, id]);

    const formatted = formatOrder(updateResult.rows[0]);

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

// Update order status manually
exports.updateStatus = async (req, res, next) => {
  try {
    const id = req.params.id;
    const { status } = req.body;

    const result = await pool.query(`
      UPDATE orders SET
        status = $1
      WHERE id = $2
      RETURNING *;
    `, [status, id]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: `Order not found with id: ${id}`
      });
    }

    const formatted = formatOrder(result.rows[0]);
    console.log(`[Order] Status updated: ${id} -> ${status}`);

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
