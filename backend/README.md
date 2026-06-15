# BiteCraft Spring Boot Backend

Production-ready backend API for the **BiteCraft** food delivery application. It handles user authentication, restaurant retrieval, order orchestration, location updates, and payment processing.

## Tech Stack & Architecture

- **Java 17+ / Spring Boot 3.3.x**
- **MongoDB Atlas** (Primary Datastore)
- **Redis** (Response caching & caching manager)
- **Apache Kafka / Redpanda** (Event Streaming for order fulfillment)
- **Stripe SDK** (Payment processing)

---

## Getting Started

### Prerequisites
- **Java 17** or higher (Java 24 is fully supported)
- **Docker & Docker Compose** (for running local Redis and Kafka brokers)

### Running Infrastructure Locally
To launch Redis and Kafka (Redpanda) locally, navigate to the `backend/` directory and run:
```bash
docker-compose up -d
```

### Running the Backend Application
Run the Spring Boot application using the Gradle wrapper from the root of the workspace:
```bash
$env:JAVA_HOME="C:\Program Files\Java\jdk-24"; .\gradlew.bat -p backend bootRun
```
The application will start on port `8080`.

---

## API Endpoints

### 1. Restaurants (`/api/restaurants`)
- `GET /` – Lists all restaurants (supports pagination and optional filtering by `cuisine`).
- `GET /{id}` – Retrieves a specific restaurant by its ID.
- `GET /nearby` – Geospatial search by latitude, longitude, and radius (kilometers).
- `GET /search` – Full-text search on name and description.
- `POST /` – Creates a new restaurant profile.
- `PUT /{id}` – Updates an existing restaurant profile.
- `DELETE /{id}` – Deletes a restaurant profile.

### 2. Orders (`/api/orders`)
- `POST /` – Places a new order. Emits an `OrderPlaced` event to Kafka.
- `GET /` – Retrieves all orders for the authenticated user (supports `userId` parameter).
- `GET /{id}/track` – Tracks order status and simulates real-time driver movement towards coordinates.
- `PUT /{id}/status` – Updates order status (e.g. `Confirmed`, `Cooking`, `OutForDelivery`, `Delivered`).

### 3. Users (`/api/users`)
- `PUT /location` – Updates the user's current location (latitude, longitude, address) and persists it in MongoDB.

### 4. Payments (`/api/payments`)
- `POST /intent` – Creates a Stripe `PaymentIntent` and returns the client secret.
- `POST /webhook` – Processes incoming Stripe webhook events.

---

## Caching Strategy
- Restaurant list and detail queries are cached using **Redis** with a **10-minute TTL**.
- Caching is managed using Spring Boot's `@Cacheable` and `@CacheEvict` annotations.
- Cache hit/miss status is dynamically populated in the `cached` field of the API responses.

## Event-Driven Architecture
- When an order is created or status is changed, an event is published to the `bitecraft-orders` Kafka topic.
- A fallback error handler ensures the API continues to operate seamlessly even if the Kafka broker is temporarily offline.
