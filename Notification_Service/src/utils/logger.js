const { createLogger, format, transports } = require("winston");
const env = require("../config/env");

const logger = createLogger({
  level: env.logLevel,
  format: format.combine(
    format.timestamp(),
    format.errors({ stack: true }),
    format.printf(({ level, message, timestamp, requestId, ...meta }) =>
      JSON.stringify({
        timestamp,
        level,
        message,
        ...(requestId ? { requestId } : {}),
        ...meta
      })
    )
  ),
  transports: [new transports.Console()]
});

module.exports = logger;
