const redis = require('redis');

const REDIS_HOST = process.env.SPRING_REDIS_HOST || 'localhost';
const REDIS_PORT = process.env.SPRING_REDIS_PORT || '6379';
const REDIS_PASSWORD = process.env.REDIS_PASSWORD || '';

const redisUrl = REDIS_PASSWORD 
  ? `redis://:${REDIS_PASSWORD}@${REDIS_HOST}:${REDIS_PORT}`
  : `redis://${REDIS_HOST}:${REDIS_PORT}`;

let redisClient = null;
let isConnected = false;

if (process.env.NODE_ENV !== 'test') {
  redisClient = redis.createClient({
    url: redisUrl,
    socket: {
      reconnectStrategy: (retries) => {
        if (retries > 5) {
          console.warn('Redis reconnection failed. Bypassing Redis cache...');
          isConnected = false;
          return false; // Stop reconnecting after 5 attempts
        }
        return Math.min(retries * 100, 3000); // Reconnect delay
      }
    }
  });

  redisClient.on('connect', () => {
    console.log('Redis connecting...');
  });

  redisClient.on('ready', () => {
    console.log('Redis Connected and Ready');
    isConnected = true;
  });

  redisClient.on('error', (err) => {
    console.warn(`Redis Connection Error: ${err.message}. Caching disabled.`);
    isConnected = false;
  });

  redisClient.connect().catch(err => {
    console.warn(`Redis startup connection failed: ${err.message}. Running without cache.`);
    isConnected = false;
  });
}

const getCache = async (key) => {
  if (!isConnected || !redisClient) return null;
  try {
    const value = await redisClient.get(key);
    return value ? JSON.parse(value) : null;
  } catch (error) {
    console.warn(`Error getting cache for key ${key}: ${error.message}`);
    return null;
  }
};

const setCache = async (key, value, ttlSeconds = 600) => {
  if (!isConnected || !redisClient) return false;
  try {
    await redisClient.set(key, JSON.stringify(value), {
      EX: ttlSeconds
    });
    return true;
  } catch (error) {
    console.warn(`Error setting cache for key ${key}: ${error.message}`);
    return false;
  }
};

const evictCache = async (pattern) => {
  if (!isConnected || !redisClient) return false;
  try {
    // Basic flush or key deletion. For wildcards in v4:
    const keys = await redisClient.keys(pattern);
    if (keys.length > 0) {
      await redisClient.del(keys);
    }
    return true;
  } catch (error) {
    console.warn(`Error evicting cache for pattern ${pattern}: ${error.message}`);
    return false;
  }
};

module.exports = {
  redisClient,
  isConnected: () => isConnected,
  getCache,
  setCache,
  evictCache
};
