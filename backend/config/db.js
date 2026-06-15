const { Pool } = require('pg');

// Supabase Connection String (password URL encoded)
const connectionString = process.env.DATABASE_URL || 'postgresql://postgres.btfhffpxulrfdyjzzgbu:Tharunk%40043@aws-1-ap-south-1.pooler.supabase.com:5432/postgres';

const pool = new Pool({
  connectionString,
  ssl: {
    rejectUnauthorized: false // Required for Supabase connection
  }
});

const initDB = async () => {
  const client = await pool.connect();
  try {
    console.log('Connecting to Supabase PostgreSQL database...');
    
    // 1. Enable PostGIS extension for Geospatial queries
    await client.query('CREATE EXTENSION IF NOT EXISTS postgis;');
    console.log('PostGIS extension verified.');

    // 2. Create Restaurants Table
    await client.query(`
      CREATE TABLE IF NOT EXISTS restaurants (
        id VARCHAR(50) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        cuisine VARCHAR(100) NOT NULL,
        rating REAL DEFAULT 0.0,
        delivery_time INTEGER DEFAULT 30,
        delivery_fee NUMERIC DEFAULT 0.0,
        image_url VARCHAR(255) DEFAULT '',
        address TEXT DEFAULT '',
        latitude DOUBLE PRECISION NOT NULL,
        longitude DOUBLE PRECISION NOT NULL,
        is_veg BOOLEAN DEFAULT FALSE,
        is_promoted BOOLEAN DEFAULT FALSE,
        description TEXT DEFAULT '',
        location GEOMETRY(Point, 4326)
      );
    `);
    
    // Create GIST spatial index for spatial queries
    await client.query('CREATE INDEX IF NOT EXISTS restaurants_location_idx ON restaurants USING GIST (location);');
    console.log('Restaurants table and spatial index verified.');

    // 3. Create Users Table
    await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id VARCHAR(50) PRIMARY KEY,
        address TEXT DEFAULT '',
        latitude DOUBLE PRECISION DEFAULT 0.0,
        longitude DOUBLE PRECISION DEFAULT 0.0,
        updated_at BIGINT NOT NULL
      );
    `);
    console.log('Users table verified.');

    // 4. Create Orders Table
    await client.query(`
      CREATE TABLE IF NOT EXISTS orders (
        id VARCHAR(50) PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL,
        restaurant_id VARCHAR(50) NOT NULL,
        items_json TEXT NOT NULL, -- JSON string representation of items
        total_amount NUMERIC NOT NULL,
        status VARCHAR(50) DEFAULT 'Placed',
        delivery_address TEXT DEFAULT '',
        payment_method VARCHAR(50) DEFAULT '',
        driver_lat DOUBLE PRECISION DEFAULT 0.0,
        driver_lng DOUBLE PRECISION DEFAULT 0.0,
        customer_lat DOUBLE PRECISION DEFAULT 0.0,
        customer_lng DOUBLE PRECISION DEFAULT 0.0,
        restaurant_lat DOUBLE PRECISION DEFAULT 0.0,
        restaurant_lng DOUBLE PRECISION DEFAULT 0.0,
        created_at BIGINT NOT NULL
      );
    `);
    console.log('Orders table verified.');

    // 5. Seed restaurants if empty
    const resCount = await client.query('SELECT COUNT(*) FROM restaurants;');
    const count = parseInt(resCount.rows[0].count);

    if (count === 0) {
      console.log('Supabase Restaurants table is empty. Pre-seeding delicious Andhra cuisine...');
      
      const seedData = [
        ['1', 'Rayalaseema Ruchulu', 'Andhra & Rayalaseema', 4.8, 20, 25.0, 'burger_lab', 'MG Road, Vijayawada, Andhra Pradesh 520010', 16.5062, 80.6480, false, true, 'Authentic spicy Rayalaseema cuisine — ragi sangati, gongura mutton, chepa pulusu & traditional Andhra meals.'],
        ['2', 'Biryani House Guntur', 'Biryani & Mughlai', 4.7, 30, 30.0, 'pizza_slice', 'Brodipet, Guntur, Andhra Pradesh 522002', 16.3067, 80.4365, false, false, 'Legendary Guntur spiced dum biryani, nalli shorba, mirchi bajji and classic Hyderabadi haleem.'],
        ['3', 'Govinda\'s Pure Veg', 'South Indian Vegetarian', 4.6, 18, 20.0, 'ramen_bowl', 'Kondapalli Road, Krishna Dist., Andhra Pradesh', 16.6156, 80.5499, true, true, 'Pure vegetarian South Indian meals — pesarattu, punugulu, gongura pachadi, Andhra thali & filter coffee.'],
        ['4', 'Sweet Bhoomi Sweets', 'Indian Sweets & Snacks', 4.9, 12, 0.0, 'cake_slice', 'Eluru Road, Vijayawada, Andhra Pradesh 520002', 16.5193, 80.6305, true, false, 'Famous Andhra sweets — Tirupati laddu, Ariselu, Pootharekulu, Bobbatlu & fresh Kajjikayalu.'],
        ['5', 'Hyderabad Spice Garden', 'Hyderabadi', 4.7, 25, 35.0, 'burger_lab', 'Banjara Hills, Hyderabad, Telangana 500034', 17.4126, 78.4071, false, true, 'Premium Hyderabadi cuisine — Kacchi dum biryani, double ka meetha, Irani chai & sheer khurma.']
      ];

      for (const row of seedData) {
        await client.query(`
          INSERT INTO restaurants (
            id, name, cuisine, rating, delivery_time, delivery_fee, 
            image_url, address, latitude, longitude, is_veg, is_promoted, description, location
          ) VALUES (
            $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, 
            ST_SetSRID(ST_Point($10, $9), 4326)
          );
        `, row);
      }
      console.log(`Successfully seeded ${seedData.length} restaurants in Supabase PostgreSQL.`);
    } else {
      console.log(`Restaurants table already contains data (${count} records). Skipping seeding.`);
    }

  } catch (error) {
    console.error(`Error during Supabase DB initialization: ${error.message}`);
    throw error;
  } finally {
    client.release();
  }
};

module.exports = {
  pool,
  initDB
};
