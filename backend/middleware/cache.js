const { getCache, setCache } = require('../config/redis');

const cacheMiddleware = (cacheName, ttlSeconds = 600) => {
  return async (req, res, next) => {
    // Generate key based on route, query params, etc.
    // e.g. restaurants-cuisine=Biryani-page=0-size=20
    const queryParts = Object.entries(req.query)
      .map(([k, v]) => `${k}=${v}`)
      .sort()
      .join('-');
    const paramParts = Object.values(req.params).join('-');
    
    const key = `${cacheName}:${paramParts || 'all'}:${queryParts || 'none'}`;

    try {
      const cachedData = await getCache(key);
      if (cachedData) {
        // Cache hit! Return immediately
        return res.json({
          success: true,
          data: cachedData,
          message: 'Success (Cached)',
          cached: true
        });
      }

      // Cache miss! Override res.json to capture response and cache it
      res.originalJson = res.json;
      res.json = (body) => {
        // Only cache success responses that have valid data
        if (body && body.success && body.data) {
          setCache(key, body.data, ttlSeconds);
        }
        return res.originalJson(body);
      };

      next();
    } catch (error) {
      console.warn(`Cache middleware error: ${error.message}. Bypassing.`);
      next();
    }
  };
};

module.exports = cacheMiddleware;
