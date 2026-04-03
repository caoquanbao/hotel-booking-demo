const { CHANNELS, resolveChannelByType } = require("../constants/channels");
const logger = require("../utils/logger");
const { ConflictError, ValidationError } = require("../utils/errors");

class NotificationService {
  constructor({
    emailService,
    telegramService,
    templateService,
    retryService,
    idempotencyService
  }) {
    this.emailService = emailService;
    this.telegramService = telegramService;
    this.templateService = templateService;
    this.retryService = retryService;
    this.idempotencyService = idempotencyService;
  }

  async send(notification, requestId) {
    const channel = resolveChannelByType(notification.type);

    if (!channel) {
      throw new ValidationError(`No channel mapping found for notification type ${notification.type}`);
    }

    const idempotencyKey = notification.metadata && notification.metadata.idempotencyKey;
    const idempotencyCheck = this.idempotencyService.begin(idempotencyKey);

    if (idempotencyCheck.duplicated) {
      logger.info("Duplicate notification request detected", {
        requestId,
        type: notification.type,
        idempotencyKey
      });

      if (idempotencyCheck.record.status === "PROCESSING") {
        throw new ConflictError("Notification with this idempotency key is already being processed", {
          idempotencyKey
        });
      }

      return {
        channel,
        duplicated: true,
        type: notification.type,
        idempotencyKey,
        cachedResult: idempotencyCheck.record.result
      };
    }

    const template = this.templateService.render(notification);

    try {
      const providerResult = await this.retryService.execute(
        async () => {
          if (channel === CHANNELS.EMAIL) {
            return this.emailService.send({
              recipient: notification.recipient,
              template,
              requestId
            });
          }

          return this.telegramService.send({
            recipient: notification.recipient,
            template,
            requestId
          });
        },
        {
          requestId,
          type: notification.type
        }
      );

      const result = {
        status: "SENT",
        type: notification.type,
        channel,
        recipient: notification.recipient,
        idempotencyKey: idempotencyKey || null,
        providerResult,
        sentAt: new Date().toISOString()
      };

      this.idempotencyService.complete(idempotencyKey, result);

      logger.info("Notification sent successfully", {
        requestId,
        type: notification.type,
        channel
      });

      return result;
    } catch (error) {
      this.idempotencyService.fail(idempotencyKey);
      throw error;
    }
  }
}

module.exports = NotificationService;
