const DEV_USER = process.env.SPRING_SECURITY_USER_NAME || 'tharun';
const DEV_PASS = process.env.SPRING_SECURITY_USER_PASSWORD || 'Tharunk043';

const authMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    res.setHeader('WWW-Authenticate', 'Basic realm="Secure Area"');
    return res.status(401).json({
      success: false,
      data: null,
      message: 'Unauthorized: Missing credentials',
      cached: false
    });
  }

  const auth = Buffer.from(authHeader.split(' ')[1], 'base64').toString().split(':');
  const user = auth[0];
  const pass = auth[1];

  if (user === DEV_USER && pass === DEV_PASS) {
    req.user = user;
    next();
  } else {
    return res.status(401).json({
      success: false,
      data: null,
      message: 'Unauthorized: Invalid credentials',
      cached: false
    });
  }
};

module.exports = authMiddleware;
