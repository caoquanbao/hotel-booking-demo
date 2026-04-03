const { v4: uuidv4 } = require("uuid");

function requestIdMiddleware(req, res, next) {
  const headerRequestId = req.headers["x-request-id"];
  const bodyRequestId = req.body && req.body.metadata ? req.body.metadata.requestId : undefined;
  const requestId = headerRequestId || bodyRequestId || uuidv4();

  req.requestId = requestId;
  res.setHeader("x-request-id", requestId);
  next();
}

module.exports = requestIdMiddleware;
