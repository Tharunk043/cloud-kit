const crypto = require('crypto');
const { pool } = require('../config/db');
const { publishEvent } = require('../config/kafka');

// Helper to format database orders into Kotlin client DTOs (converts items_json string back to items object)
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
    createdAt: parseInt(dbOrder.created_at)
  };
};

// Place a new order
exports.placeOrder = async (req, res, next) => {
  try {
    const {
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

    const userId = req.user || 'tharun';
    const orderId = crypto.randomUUID();
    const itemsJson = JSON.stringify(items);
    const createdAt = Date.now();

    const queryText = `
      INSERT INTO orders (
        id, user_id, restaurant_id, items_json, total_amount, status,
        delivery_address, payment_method, driver_lat, driver_lng,
        customer_lat, customer_lng, restaurant_lat, restaurant_lng, created_at
      ) VALUES (
        $1, $2, $3, $4, $5, 'Placed', $6, $7, $8, $9, $10, $11, $12, $13, $14
      ) RETURNING *;
    `;

    const result = await pool.query(queryText, [
      orderId, userId, restaurantId, itemsJson, totalAmount,
      deliveryAddress || '', paymentMethod || '', restaurantLat, restaurantLng,
      customerLat, customerLng, restaurantLat, restaurantLng, createdAt
    ]);

    const formatted = formatOrder(result.rows[0]);

    // Publish event
    await publishEvent('bitecraft-orders', orderId, {
      eventType: 'OrderPlaced',
      order: formatted
    });

    res.json({
      success: true,
      data: formatted,
      message: 'Order placed successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Get orders for a user
exports.getUserOrders = async (req, res, next) => {
  try {
    const userId = req.query.userId || req.user || 'tharun';
    
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
      await publishEvent('bitecraft-orders', id, { eventType: 'OrderConfirmed', order: formatOrder({ ...order, status }) });
    } else if (status === 'Confirmed') {
      status = 'Cooking';
      await publishEvent('bitecraft-orders', id, { eventType: 'OrderCooking', order: formatOrder({ ...order, status }) });
    } else if (status === 'Cooking') {
      status = 'OutForDelivery';
      driverLat = (order.restaurant_lat + order.customer_lat) / 2;
      driverLng = (order.restaurant_lng + order.customer_lng) / 2;
      await publishEvent('bitecraft-orders', id, { eventType: 'OrderOutForDelivery', order: formatOrder({ ...order, status, driverLat, driverLng }) });
    } else if (status === 'OutForDelivery') {
      const latDiff = order.customer_lat - driverLat;
      const lngDiff = order.customer_lng - driverLng;

      if (Math.abs(latDiff) < 0.001 && Math.abs(lngDiff) < 0.001) {
        status = 'Delivered';
        driverLat = order.customer_lat;
        driverLng = order.customer_lng;
        await publishEvent('bitecraft-orders', id, { eventType: 'OrderDelivered', order: formatOrder({ ...order, status, driverLat, driverLng }) });
      } else {
        // Move driver 50% closer
        driverLat = driverLat + latDiff * 0.5;
        driverLng = driverLng + lngDiff * 0.5;
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

    await publishEvent('bitecraft-orders', id, {
      eventType: `OrderStatusUpdated-${status}`,
      order: formatted
    });

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
