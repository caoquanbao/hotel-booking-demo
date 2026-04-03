const http = require("http");
const createApp = require("./app");
const env = require("./config/env");
const logger = require("./utils/logger");

const GmailProvider = require("./providers/gmail.provider");
const TelegramProvider = require("./providers/telegram.provider");

const EmailService = require("./services/email.service");
const TelegramService = require("./services/telegram.service");
const TemplateService = require("./services/template.service");
const RetryService = require("./services/retry.service");
const IdempotencyService = require("./services/idempotency.service");
const NotificationService = require("./services/notification.service");

const RabbitMqConsumer = require("./consumers/rabbitmq.consumer");

const gmailProvider = new GmailProvider();
const telegramProvider = new TelegramProvider();

const emailService = new EmailService(gmailProvider);
const telegramService = new TelegramService(telegramProvider);
const templateService = new TemplateService();
const retryService = new RetryService();
const idempotencyService = new IdempotencyService();

const notificationService = new NotificationService({
  emailService,
  telegramService,
  templateService,
  retryService,
  idempotencyService
});

const rabbitMqConsumer = new RabbitMqConsumer({
  notificationService,
  enabled: env.rabbitmqEnabled
});

const app = createApp({ notificationService });
const server = http.createServer(app);

async function startServer() {
  await rabbitMqConsumer.start();

  server.listen(env.port, () => {
    console.log(`Server is running on port ${env.port}`);
  });
}

startServer().catch((error) => {
  logger.error("Failed to start notification service", {
    errorMessage: error.message,
    stack: error.stack
  });
  process.exit(1);
});

module.exports = {
  app,
  server,
  notificationService
};
