const express = require('express');
const router = express.Router();
const restaurantController = require('../controllers/restaurantController');
const cacheMiddleware = require('../middleware/cache');

router.get('/', cacheMiddleware('restaurants', 600), restaurantController.getRestaurants);
router.get('/nearby', restaurantController.getNearby);
router.get('/search', restaurantController.search);
router.get('/:id', cacheMiddleware('restaurant', 600), restaurantController.getRestaurantById);

router.post('/', restaurantController.create);
router.put('/:id', restaurantController.update);
router.delete('/:id', restaurantController.delete);

module.exports = router;
