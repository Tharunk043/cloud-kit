const errorHandler = (err, req, res, next) => {
  console.error('Unhandled Error:', err);

  const statusCode = res.statusCode === 200 ? 500 : res.statusCode;
  
  res.status(statusCode).json({
    success: false,
    data: null,
    message: err.message || 'An internal server error occurred',
    cached: false
  });
};

module.exports = errorHandler;
