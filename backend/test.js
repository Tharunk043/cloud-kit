// We set NODE_ENV to test BEFORE requiring any app code so that external configs are not initialized
process.env.NODE_ENV = 'test';

const test = require('node:test');
const assert = require('node:assert');
const app = require('./app');

test('Health Check Endpoint /actuator/health returns 200 and UP status', async (t) => {
  const server = app.listen(0);
  const { port } = server.address();

  try {
    const res = await fetch(`http://localhost:${port}/actuator/health`);
    const body = await res.json();

    assert.strictEqual(res.status, 200);
    assert.strictEqual(body.status, 'UP');
  } finally {
    server.close();
  }
});

test('Auth block - /api/restaurants returns 401 when unauthenticated', async (t) => {
  const server = app.listen(0);
  const { port } = server.address();

  try {
    const res = await fetch(`http://localhost:${port}/api/restaurants`);
    const body = await res.json();

    assert.strictEqual(res.status, 401);
    assert.strictEqual(body.success, false);
    assert.strictEqual(body.message.includes('Unauthorized'), true);
  } finally {
    server.close();
  }
});
