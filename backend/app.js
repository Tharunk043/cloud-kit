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

// Friendly welcome root endpoint
app.get('/', (req, res) => {
  res.send(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>BiteCraft API Server</title>
      <style>
        body {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          background: linear-gradient(135deg, #0f172a, #1e1b4b);
          color: #f1f5f9;
          display: flex;
          align-items: center;
          justify-content: center;
          min-height: 100vh;
          margin: 0;
          padding: 20px;
          box-sizing: border-box;
        }
        .container {
          max-width: 600px;
          background: rgba(255, 255, 255, 0.03);
          border: 1px solid rgba(255, 255, 255, 0.08);
          border-radius: 24px;
          padding: 40px;
          text-align: center;
          backdrop-filter: blur(16px);
          box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }
        .logo {
          font-size: 3rem;
          font-weight: 800;
          background: linear-gradient(to right, #f97316, #ef4444);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          margin-bottom: 8px;
        }
        .badge {
          background: rgba(34, 197, 94, 0.15);
          color: #4ade80;
          padding: 6px 16px;
          border-radius: 9999px;
          font-size: 0.85rem;
          font-weight: 600;
          display: inline-block;
          border: 1px solid rgba(74, 222, 128, 0.2);
          margin-bottom: 24px;
        }
        p {
          color: #94a3b8;
          line-height: 1.6;
          margin-bottom: 32px;
        }
        .endpoints {
          text-align: left;
          background: rgba(0, 0, 0, 0.2);
          padding: 20px;
          border-radius: 16px;
          border: 1px solid rgba(255, 255, 255, 0.05);
        }
        .endpoints h3 {
          margin-top: 0;
          color: #f1f5f9;
          font-size: 1rem;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1);
          padding-bottom: 8px;
          margin-bottom: 12px;
        }
        .endpoint-item {
          display: flex;
          justify-content: space-between;
          font-family: monospace;
          margin-bottom: 8px;
          font-size: 0.9rem;
        }
        .endpoint-item:last-child {
          margin-bottom: 0;
        }
        .method {
          font-weight: bold;
          color: #f97316;
        }
        .path {
          color: #cbd5e1;
        }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="logo">BiteCraft</div>
        <div class="badge">● Server Live</div>
        <p>Production-grade lightweight Node.js backend system for the BiteCraft Food Delivery platform, connected to Supabase PostgreSQL in ap-south-1.</p>
        
        <div class="endpoints">
          <h3>Available Endpoints</h3>
          <div class="endpoint-item">
            <span class="method">GET</span>
            <span class="path">/actuator/health</span>
          </div>
          <div class="endpoint-item">
            <span class="method">GET</span>
            <span class="path">/api/restaurants</span>
          </div>
          <div class="endpoint-item">
            <span class="method">GET</span>
            <span class="path">/api/restaurants/nearby</span>
          </div>
          <div class="endpoint-item">
            <span class="method">POST</span>
            <span class="path">/api/orders</span>
          </div>
          <div class="endpoint-item">
            <span class="method">GET</span>
            <span class="path">/api/orders/:id/track</span>
          </div>
        </div>
      </div>
    </body>
    </html>
  `);
});

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
