const env = require("./env");

module.exports = Object.freeze({
  enabled: env.mailEnabled,
  from: env.mailFrom,
  host: env.mailHost,
  port: env.mailPort,
  secure: env.mailSecure,
  auth: {
    user: env.mailUser,
    pass: env.mailPass
  }
});
