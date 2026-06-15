const { Kafka } = require('kafkajs');

const KAFKA_BOOTSTRAP_SERVERS = process.env.KAFKA_BOOTSTRAP_SERVERS || 'localhost:9092';
const KAFKA_SECURITY_PROTOCOL = process.env.KAFKA_SECURITY_PROTOCOL || 'PLAINTEXT';
const KAFKA_SASL_MECHANISM = process.env.KAFKA_SASL_MECHANISM || 'PLAIN';
const KAFKA_USERNAME = process.env.KAFKA_USERNAME || '';
const KAFKA_PASSWORD = process.env.KAFKA_PASSWORD || '';

let kafka = null;
let producer = null;
let isConnected = false;

// Only initialize if we're not running tests
if (process.env.NODE_ENV !== 'test') {
  try {
    const kafkaConfig = {
      clientId: 'bitecraft-backend',
      brokers: KAFKA_BOOTSTRAP_SERVERS.split(','),
      retry: {
        initialRetryTime: 300,
        retries: 3 // Max 3 retries so it doesn't block server startup
      }
    };

    // Configure SASL if credentials are provided
    if (KAFKA_SECURITY_PROTOCOL === 'SASL_SSL' || KAFKA_USERNAME) {
      kafkaConfig.ssl = true;
      kafkaConfig.sasl = {
        mechanism: KAFKA_SASL_MECHANISM.toLowerCase() === 'scram-sha-256' ? 'scram-sha-256' : 'plain',
        username: KAFKA_USERNAME,
        password: KAFKA_PASSWORD
      };
    }

    kafka = new Kafka(kafkaConfig);
    producer = kafka.producer();

    producer.connect()
      .then(() => {
        console.log('Kafka Producer Connected Successfully');
        isConnected = true;
      })
      .catch(err => {
        console.warn(`Kafka Connection Failed: ${err.message}. Running in fallback mode.`);
        isConnected = false;
      });

  } catch (error) {
    console.warn(`Kafka client initialization error: ${error.message}. Running in fallback mode.`);
  }
}

const publishEvent = async (topic, key, payload) => {
  if (!isConnected || !producer) {
    console.info(`[Kafka Fallback Log] Event published to topic ${topic} (Key: ${key}):`, JSON.stringify(payload));
    return false;
  }
  try {
    await producer.send({
      topic,
      messages: [
        { key: String(key), value: JSON.stringify(payload) }
      ],
    });
    return true;
  } catch (error) {
    console.warn(`Failed to send event to Kafka topic ${topic}: ${error.message}`);
    return false;
  }
};

module.exports = {
  publishEvent,
  isConnected: () => isConnected
};
