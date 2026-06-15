require('dotenv').config();
const app = require('./app');
const { initDB } = require('./config/db');

const PORT = process.env.PORT || 8080;

const startServer = async () => {
  // Initialize and connect to Supabase PostgreSQL database
  await initDB();
  
  app.listen(PORT, () => {
    console.log(`BiteCraft Supabase Backend Server running on port ${PORT}`);
    console.log(`Press Ctrl+C to terminate`);
  });
};

startServer().catch(err => {
  console.error('Server startup crash:', err.message);
  process.exit(1);
});
