const logger = require("../utils/logger");
const { error } = require("../utils/api-response");

function errorMiddleware(err, req, res, next) {
  const statusCode = err.statusCode || 500;

  logger.error(err.message, {
    requestId: req.requestId,
    path: req.originalUrl,
    method: req.method,
    details: err.details || null,
    stack: err.stack
  });

  res.status(statusCode).json(
    error(err.message || "Internal server error", {
      requestId: req.requestId,
      ...(err.details ? { details: err.details } : {})
    })
  );
}

module.exports = errorMiddleware;
