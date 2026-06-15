const { pool } = require('../config/db');
const { evictCache } = require('../config/redis');

// Get all restaurants (paginated, optional cuisine filter)
exports.getRestaurants = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = parseInt(req.query.size) || 20;
    const cuisine = req.query.cuisine;

    const offset = page * size;
    let queryText = '';
    let queryParams = [];

    if (cuisine && cuisine.trim()) {
      queryText = `
        SELECT id, name, cuisine, rating, delivery_time AS "deliveryTime", 
               delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
               latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
               description 
        FROM restaurants 
        WHERE cuisine ILIKE $1 
        ORDER BY rating DESC 
        LIMIT $2 OFFSET $3;
      `;
      queryParams = [`%${cuisine.trim()}%`, size, offset];
    } else {
      queryText = `
        SELECT id, name, cuisine, rating, delivery_time AS "deliveryTime", 
               delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
               latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
               description 
        FROM restaurants 
        ORDER BY rating DESC 
        LIMIT $1 OFFSET $2;
      `;
      queryParams = [size, offset];
    }

    const result = await pool.query(queryText, queryParams);
    
    res.json({
      success: true,
      data: result.rows,
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Get single restaurant by ID
exports.getRestaurantById = async (req, res, next) => {
  try {
    const id = req.params.id;
    const queryText = `
      SELECT id, name, cuisine, rating, delivery_time AS "deliveryTime", 
             delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
             latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
             description 
      FROM restaurants 
      WHERE id = $1;
    `;
    const result = await pool.query(queryText, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: `Restaurant not found with id: ${id}`
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Get nearby restaurants (PostGIS spatial query)
exports.getNearby = async (req, res, next) => {
  try {
    const lat = parseFloat(req.query.lat);
    const lng = parseFloat(req.query.lng);
    const radius = parseFloat(req.query.radius) || 5.0; // Defaults to 5km

    if (isNaN(lat) || isNaN(lng)) {
      return res.status(400).json({
        success: false,
        message: 'Latitude and Longitude query parameters are required and must be numbers'
      });
    }

    // ST_DistanceSphere returns distance in meters. ST_DWithin performs fast bounding-box filtering first.
    const queryText = `
      SELECT id, name, cuisine, rating, delivery_time AS "deliveryTime", 
             delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
             latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
             description,
             ST_Distance(location, ST_SetSRID(ST_Point($1, $2), 4326)::geography) / 1000 AS "distanceKm"
      FROM restaurants
      WHERE ST_DWithin(location::geography, ST_SetSRID(ST_Point($1, $2), 4326)::geography, $3 * 1000)
      ORDER BY "distanceKm" ASC;
    `;
    
    const result = await pool.query(queryText, [lng, lat, radius]);

    res.json({
      success: true,
      data: result.rows,
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Search restaurants
exports.search = async (req, res, next) => {
  try {
    const q = req.query.q || '';
    const queryText = `
      SELECT id, name, cuisine, rating, delivery_time AS "deliveryTime", 
             delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
             latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
             description 
      FROM restaurants 
      WHERE name ILIKE $1 OR description ILIKE $1;
    `;
    const result = await pool.query(queryText, [`%${q}%`]);

    res.json({
      success: true,
      data: result.rows,
      message: 'Success',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Create restaurant
exports.create = async (req, res, next) => {
  try {
    const {
      id, name, cuisine, rating, deliveryTime, deliveryFee, 
      imageUrl, address, latitude, longitude, isVeg, isPromoted, description
    } = req.body;

    const queryText = `
      INSERT INTO restaurants (
        id, name, cuisine, rating, delivery_time, delivery_fee, 
        image_url, address, latitude, longitude, is_veg, is_promoted, description, location
      ) VALUES (
        $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13,
        ST_SetSRID(ST_Point($10, $9), 4326)
      ) RETURNING id, name, cuisine, rating, delivery_time AS "deliveryTime", 
                  delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
                  latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
                  description;
    `;

    const result = await pool.query(queryText, [
      id, name, cuisine, rating || 0.0, deliveryTime || 30, deliveryFee || 0.0,
      imageUrl || '', address || '', latitude, longitude, isVeg || false, isPromoted || false, description || ''
    ]);

    await evictCache('restaurants:*');

    res.json({
      success: true,
      data: result.rows[0],
      message: 'Restaurant created successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Update restaurant
exports.update = async (req, res, next) => {
  try {
    const id = req.params.id;
    const {
      name, cuisine, rating, deliveryTime, deliveryFee, 
      imageUrl, address, latitude, longitude, isVeg, isPromoted, description
    } = req.body;

    const queryText = `
      UPDATE restaurants SET
        name = COALESCE($1, name),
        cuisine = COALESCE($2, cuisine),
        rating = COALESCE($3, rating),
        delivery_time = COALESCE($4, delivery_time),
        delivery_fee = COALESCE($5, delivery_fee),
        image_url = COALESCE($6, image_url),
        address = COALESCE($7, address),
        latitude = COALESCE($8, latitude),
        longitude = COALESCE($9, longitude),
        is_veg = COALESCE($10, is_veg),
        is_promoted = COALESCE($11, is_promoted),
        description = COALESCE($12, description),
        location = CASE 
          WHEN $8 IS NOT NULL AND $9 IS NOT NULL THEN ST_SetSRID(ST_Point($9, $8), 4326)
          ELSE location
        END
      WHERE id = $13
      RETURNING id, name, cuisine, rating, delivery_time AS "deliveryTime", 
                delivery_fee AS "deliveryFee", image_url AS "imageUrl", address, 
                latitude, longitude, is_veg AS "isVeg", is_promoted AS "isPromoted", 
                description;
    `;

    const result = await pool.query(queryText, [
      name, cuisine, rating, deliveryTime, deliveryFee,
      imageUrl, address, latitude, longitude, isVeg, isPromoted, description, id
    ]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: `Restaurant not found with id: ${id}`
      });
    }

    await evictCache('restaurants:*');
    await evictCache(`restaurant:${id}:*`);

    res.json({
      success: true,
      data: result.rows[0],
      message: 'Restaurant updated successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};

// Delete restaurant
exports.delete = async (req, res, next) => {
  try {
    const id = req.params.id;
    const result = await pool.query('DELETE FROM restaurants WHERE id = $1 RETURNING id;', [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: `Restaurant not found with id: ${id}`
      });
    }

    await evictCache('restaurants:*');
    await evictCache(`restaurant:${id}:*`);

    res.json({
      success: true,
      data: null,
      message: 'Restaurant deleted successfully',
      cached: false
    });
  } catch (error) {
    next(error);
  }
};
