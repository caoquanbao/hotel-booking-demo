const appConfig = require("../config/app.config");
const { success } = require("../utils/api-response");

function healthController(req, res) {
  res.json(
    success({
      serviceName: appConfig.serviceName,
      status: "UP",
      timestamp: new Date().toISOString(),
      uptime: process.uptime()
    })
  );
}

module.exports = {
  healthController
};
