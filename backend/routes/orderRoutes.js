const express = require('express');
const router = express.Router();
const orderController = require('../controllers/orderController');

router.post('/', orderController.placeOrder);
router.get('/', orderController.getUserOrders);
router.get('/:id/track', orderController.trackOrder);
router.put('/:id/status', orderController.updateStatus);

module.exports = router;
