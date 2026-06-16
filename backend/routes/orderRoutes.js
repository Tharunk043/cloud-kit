const express = require('express');
const router = express.Router();
const orderController = require('../controllers/orderController');

router.post('/', orderController.placeOrder);
router.get('/', orderController.getOrders);
router.get('/:id/track', orderController.trackOrder);
router.put('/:id/status', orderController.updateStatus);
router.put('/:id/accept-rider', orderController.acceptRider);
router.post('/:id/review', orderController.submitOrderReview);

module.exports = router;
