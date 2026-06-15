const { pool } = require('../config/db');

exports.updateLocation = async (req, res, next) => {
  try {
    const { userId, lat, lng, address } = req.body;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: 'User ID is required'
      });
    }

    const updatedAt = Date.now();

    const queryText = `
      INSERT INTO users (id, address, latitude, longitude, updated_at)
      VALUES ($1, $2, $3, $4, $5)
      ON CONFLICT (id) 
      DO UPDATE SET 
        address = EXCLUDED.address,
        latitude = EXCLUDED.latitude,
        longitude = EXCLUDED.longitude,
        updated_at = EXCLUDED.updated_at
      RETURNING *;
    `;

    await pool.query(queryText, [userId, address || '', lat || 0.0, lng || 0.0, updatedAt]);

    res.json({
      success: true,
      data: null,
      message: 'Location updated successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};
