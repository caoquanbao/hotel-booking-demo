const env = require("./env");

module.exports = Object.freeze({
  enabled: env.telegramEnabled,
  botToken: env.telegramBotToken,
  defaultChatId: env.telegramDefaultChatId,
  apiBaseUrl: env.telegramApiBaseUrl
});
