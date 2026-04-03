const telegramConfig = require("../config/telegram.config");

class TelegramService {
  constructor(telegramProvider) {
    this.telegramProvider = telegramProvider;
  }

  async send({ recipient, template, requestId }) {
    const chatId = recipient.telegramChatId || telegramConfig.defaultChatId;

    return this.telegramProvider.send({
      chatId,
      text: template.text,
      requestId
    });
  }
}

module.exports = TelegramService;
