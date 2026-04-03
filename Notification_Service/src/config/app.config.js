const env = require("./env");

module.exports = Object.freeze({
  serviceName: env.serviceName,
  port: env.port,
  nodeEnv: env.nodeEnv
});
