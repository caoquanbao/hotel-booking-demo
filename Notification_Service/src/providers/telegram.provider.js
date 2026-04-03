const axios = require("axios");
const telegramConfig = require("../config/telegram.config");
const logger = require("../utils/logger");
const { ProviderError } = require("../utils/errors");

class TelegramProvider {
  constructor() {
    this.enabled = telegramConfig.enabled;
  }

  async send({ chatId, text, requestId }) {
    if (!this.enabled) {
      logger.info("Telegram provider is disabled. Dry run completed.", {
        requestId,
        chatId,
        text
      });

      return {
        provider: "telegram",
        dryRun: true,
        chatId
      };
    }

    try {
      const url = `${telegramConfig.apiBaseUrl}/bot${telegramConfig.botToken}/sendMessage`;
      const response = await axios.post(url, {
        chat_id: chatId,
        text
      });

      return {
        provider: "telegram",
        messageId: response.data && response.data.result ? response.data.result.message_id : null,
        ok: response.data ? response.data.ok : true
      };
    } catch (error) {
      throw new ProviderError("Failed to send telegram message", {
        originalMessage: error.message
      });
    }
  }
}

module.exports = TelegramProvider;
