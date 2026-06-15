const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const authMiddleware = require('./middleware/auth');
const errorHandler = require('./middleware/errorHandler');

const restaurantRoutes = require('./routes/restaurantRoutes');
const orderRoutes = require('./routes/orderRoutes');
const userRoutes = require('./routes/userRoutes');
const paymentRoutes = require('./routes/paymentRoutes');

const app = express();

// Enable CORS
app.use(cors());

// HTTP Request Logger
app.use(morgan('dev'));

// Express Parser - Note: webhook needs raw body sometimes, but a simple parser is fine here
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Actuator-like health check endpoint (Open, no auth)
app.get('/actuator/health', (req, res) => {
  res.json({
    status: 'UP',
    components: {
      db: { status: 'UP' },
      diskSpace: { status: 'UP' },
      ping: { status: 'UP' }
    }
  });
});

// Stripe webhook bypasses auth (Must be registered before basic auth middleware)
app.post('/api/payments/webhook', paymentRoutes);

// Apply Basic Auth Middleware to all other API routes
app.use('/api', authMiddleware);

// Bind API routes
app.use('/api/restaurants', restaurantRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/users', userRoutes);
app.use('/api/payments', paymentRoutes); // handles /intent

// Catch-all 404
app.use((req, res, next) => {
  res.status(404).json({
    success: false,
    data: null,
    message: `Resource not found: ${req.method} ${req.url}`,
    cached: false
  });
});

// Global Error Handler
app.use(errorHandler);

module.exports = app;
