const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

router.post('/profile', userController.syncProfile);
router.put('/location', userController.updateLocation);
router.get('/:phone/wallet', userController.getWalletTransactions);
router.post('/:phone/wallet', userController.addWalletTransaction);
router.get('/:phone/addresses', userController.getAddresses);
router.post('/:phone/addresses', userController.addAddress);
router.delete('/:phone/addresses/:id', userController.deleteAddress);

module.exports = router;
