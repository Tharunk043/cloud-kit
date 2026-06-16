const { Pool } = require('pg');

// Supabase Connection String (password URL encoded)
const connectionString = process.env.DATABASE_URL || 'postgresql://postgres.btfhffpxulrfdyjzzgbu:Tharunk%40043@aws-1-ap-south-1.pooler.supabase.com:5432/postgres';

const pool = new Pool({
  connectionString,
  max: 3, // Prevent EMAXCONNSESSION by limiting pool size during rolling deploys
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
        phone VARCHAR(50) UNIQUE,
        name VARCHAR(100) DEFAULT '',
        email VARCHAR(100) DEFAULT '',
        avatar_url VARCHAR(255) DEFAULT '',
        session_token VARCHAR(255) DEFAULT '',
        is_verified BOOLEAN DEFAULT FALSE,
        default_address_id INTEGER DEFAULT -1,
        is_gold_member BOOLEAN DEFAULT FALSE,
        wallet_balance NUMERIC DEFAULT 100.0,
        created_at BIGINT DEFAULT 0,
        last_login_at BIGINT DEFAULT 0,
        address TEXT DEFAULT '',
        latitude DOUBLE PRECISION DEFAULT 0.0,
        longitude DOUBLE PRECISION DEFAULT 0.0,
        updated_at BIGINT DEFAULT 0,
        role VARCHAR(50) DEFAULT 'Customer'
      );
    `);
    // Alter statements to add missing columns if they exist from legacy runs
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(100) DEFAULT \'\';');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100) DEFAULT \'\';');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255) DEFAULT \'\';');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS session_token VARCHAR(255) DEFAULT \'\';');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS default_address_id INTEGER DEFAULT -1;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS is_gold_member BOOLEAN DEFAULT FALSE;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS wallet_balance NUMERIC DEFAULT 100.0;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at BIGINT DEFAULT 0;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at BIGINT DEFAULT 0;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at BIGINT DEFAULT 0;');
    await client.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT \'Customer\';');
    await client.query('ALTER TABLE users ALTER COLUMN updated_at SET DEFAULT 0;');
    await client.query('ALTER TABLE users ALTER COLUMN updated_at DROP NOT NULL;');
    // Add unique constraint on phone if not already present (safe to run multiple times)
    await client.query(`
      DO $$ BEGIN
        IF NOT EXISTS (
          SELECT 1 FROM pg_constraint WHERE conname = 'users_phone_unique'
        ) THEN
          -- Clean up any duplicate phone numbers before adding the unique constraint
          DELETE FROM users WHERE id NOT IN (
            SELECT MIN(id) FROM users GROUP BY phone
          );
          ALTER TABLE users ADD CONSTRAINT users_phone_unique UNIQUE (phone);
        END IF;
      END $$;
    `);

    // Trim existing phone numbers to the last 10 digits to resolve format mismatches
    await client.query(`
      DELETE FROM users WHERE id NOT IN (
        SELECT MIN(id) FROM users GROUP BY SUBSTRING(phone FROM GREATEST(1, LENGTH(phone) - 9))
      );
      UPDATE users SET phone = SUBSTRING(phone FROM GREATEST(1, LENGTH(phone) - 9)) WHERE LENGTH(phone) > 10;
    `);
    console.log('Users table and profile columns verified.');

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
        created_at BIGINT NOT NULL,
        rating_given REAL DEFAULT 0.0,
        review_text TEXT DEFAULT '',
        review_sentiment VARCHAR(50) DEFAULT 'Neutral'
      );
    `);
    // Alter statements for orders rating fields
    await client.query('ALTER TABLE orders ADD COLUMN IF NOT EXISTS rating_given REAL DEFAULT 0.0;');
    await client.query('ALTER TABLE orders ADD COLUMN IF NOT EXISTS review_text TEXT DEFAULT \'\';');
    await client.query('ALTER TABLE orders ADD COLUMN IF NOT EXISTS review_sentiment VARCHAR(50) DEFAULT \'Neutral\';');
    await client.query('ALTER TABLE orders ADD COLUMN IF NOT EXISTS driver_name VARCHAR(100) DEFAULT \'\';');
    await client.query('ALTER TABLE orders ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(50) DEFAULT \'\';');
    console.log('Orders table and rating/driver columns verified.');

    // 4b. Create Wallet Transactions Table
    await client.query(`
      CREATE TABLE IF NOT EXISTS wallet_transactions (
        id VARCHAR(50) PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL,
        type VARCHAR(50) NOT NULL,
        amount NUMERIC NOT NULL,
        description TEXT DEFAULT '',
        timestamp BIGINT NOT NULL,
        member_name VARCHAR(100) DEFAULT ''
      );
    `);
    console.log('Wallet transactions table verified.');

    // 4c. Create Saved Addresses Table
    await client.query(`
      CREATE TABLE IF NOT EXISTS saved_addresses (
        id VARCHAR(50) PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL,
        label VARCHAR(100) NOT NULL,
        full_address TEXT NOT NULL,
        latitude DOUBLE PRECISION NOT NULL,
        longitude DOUBLE PRECISION NOT NULL,
        is_default BOOLEAN DEFAULT FALSE,
        created_at BIGINT NOT NULL
      );
    `);
    console.log('Saved addresses table verified.');

    // 5. Seed restaurants if empty or containing legacy Vijayawada data
    const resCount = await client.query('SELECT COUNT(*) FROM restaurants;');
    const count = parseInt(resCount.rows[0].count);
    const legacyCheck = await client.query("SELECT COUNT(*) FROM restaurants WHERE address LIKE '%Vijayawada%';");
    const isLegacy = parseInt(legacyCheck.rows[0].count) > 0;

    if (count === 0 || isLegacy) {
      console.log('Supabase Restaurants table is empty or has legacy Vijayawada data. Seeding real Kadapa locations...');
      await client.query('DELETE FROM restaurants;');
      
      const seedData = [
        ['1', 'Rayalaseema Ruchulu Kadapa', 'Andhra & Rayalaseema', 4.8, 20, 25.0, 'burger_lab', 'Seven Roads Circle, Kadapa, Andhra Pradesh 516001', 14.4745, 78.8262, false, true, 'Authentic spicy Rayalaseema cuisine — ragi sangati, gongura mutton, chepa pulusu & traditional Kadapa meals.'],
        ['2', 'Hotel Srinivasa Regency', 'Biryani & Mughlai', 4.7, 30, 30.0, 'pizza_slice', 'Trunk Road, Near RTC Bus Stand, Kadapa, Andhra Pradesh 516001', 14.4752, 78.8258, false, false, 'Legendary Rayalaseema spiced dum biryani, nalli shorba, mirchi bajji and traditional kebabs.'],
        ['3', 'Govinda Pure Veg Kadapa', 'South Indian Vegetarian', 4.6, 18, 20.0, 'ramen_bowl', 'Yerramukkapalli, Kadapa, Andhra Pradesh 516004', 14.4645, 78.8152, true, true, 'Pure vegetarian South Indian meals — pesarattu, punugulu, gongura pachadi, special thali & filter coffee.'],
        ['4', 'Haritha Tourism Hotel', 'Indian Sweets & Snacks', 4.9, 12, 0.0, 'cake_slice', 'Near Collectorate, Kadapa, Andhra Pradesh 516001', 14.4784, 78.8192, true, false, 'Famous regional sweets — Tirupati laddu, Ariselu, Pootharekulu, Bobbatlu & traditional snacks.'],
        ['5', 'Hyderabad Biryani House Kadapa', 'Hyderabadi', 4.7, 25, 35.0, 'burger_lab', 'Christian Lane, Kadapa, Andhra Pradesh 516001', 14.4718, 78.8235, false, true, 'Premium Hyderabadi & Rayalaseema fusion cuisine — Kacchi dum biryani, double ka meetha, Irani chai & mutton specialties.']
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
      console.log(`Restaurants table already contains data (${count} records). Skipping seeding.`);
    }

    // 6. Create performance indexes
    console.log('Verifying performance indexes in database...');
    // Enable pg_trgm extension for fast ILIKE text search
    await client.query('CREATE EXTENSION IF NOT EXISTS pg_trgm;');
    
    // GIN Trigram indexes for restaurants
    await client.query('CREATE INDEX IF NOT EXISTS restaurants_cuisine_trgm_idx ON restaurants USING gin (cuisine gin_trgm_ops);');
    await client.query('CREATE INDEX IF NOT EXISTS restaurants_name_trgm_idx ON restaurants USING gin (name gin_trgm_ops);');
    await client.query('CREATE INDEX IF NOT EXISTS restaurants_description_trgm_idx ON restaurants USING gin (description gin_trgm_ops);');
    
    // B-Tree index on rating descending
    await client.query('CREATE INDEX IF NOT EXISTS restaurants_rating_idx ON restaurants (rating DESC);');
    
    // Functional GiST index on location::geography
    await client.query('CREATE INDEX IF NOT EXISTS restaurants_location_geog_idx ON restaurants USING GIST ((location::geography));');
    
    // Composite indexes for orders (timeline & FK lookup)
    await client.query('CREATE INDEX IF NOT EXISTS orders_user_timeline_idx ON orders (user_id, created_at DESC);');
    await client.query('CREATE INDEX IF NOT EXISTS orders_restaurant_id_idx ON orders (restaurant_id);');
    
    // Composite index for wallet transactions
    await client.query('CREATE INDEX IF NOT EXISTS wallet_transactions_user_timestamp_idx ON wallet_transactions (user_id, timestamp DESC);');
    
    // Composite index for saved addresses
    await client.query('CREATE INDEX IF NOT EXISTS saved_addresses_user_lookup_idx ON saved_addresses (user_id, is_default DESC, created_at DESC);');
    
    console.log('Database performance indexes verified successfully.');

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
