const fs = require("fs");
const path = require("path");
const dotenv = require("dotenv");

const candidateEnvPaths = [
  process.env.NOTIFICATION_ENV_FILE,
  path.resolve(__dirname, "../../.env"),
  path.resolve(__dirname, "../../.env.local"),
  path.resolve(__dirname, "../../../.env"),
  path.resolve(__dirname, "../../../.env.local")
].filter(Boolean);

let loadedEnvPath = null;

for (const candidatePath of candidateEnvPaths) {
  if (fs.existsSync(candidatePath)) {
    dotenv.config({ path: candidatePath });
    loadedEnvPath = candidatePath;
    break;
  }
}

function parseBoolean(value, defaultValue = false) {
  if (value === undefined) {
    return defaultValue;
  }

  return String(value).trim().toLowerCase() === "true";
}

function parseNumber(value, defaultValue) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : defaultValue;
}

module.exports = Object.freeze({
  envFilePath: loadedEnvPath,
  nodeEnv: process.env.NODE_ENV || "development",
  port: parseNumber(process.env.PORT, 3004),
  serviceName: process.env.SERVICE_NAME || "Notification_Service",
  logLevel: process.env.LOG_LEVEL || "info",
  mailEnabled: parseBoolean(process.env.MAIL_ENABLED, false),
  mailFrom: process.env.MAIL_FROM || "Hotel Booking Demo <no-reply@example.com>",
  mailHost: process.env.MAIL_HOST || "smtp.gmail.com",
  mailPort: parseNumber(process.env.MAIL_PORT, 587),
  mailSecure: parseBoolean(process.env.MAIL_SECURE, false),
  mailUser: process.env.MAIL_USER || "",
  mailPass: process.env.MAIL_PASS || "",
  telegramEnabled: parseBoolean(process.env.TELEGRAM_ENABLED, false),
  telegramBotToken: process.env.TELEGRAM_BOT_TOKEN || "",
  telegramDefaultChatId: process.env.TELEGRAM_DEFAULT_CHAT_ID || "",
  telegramApiBaseUrl: process.env.TELEGRAM_API_BASE_URL || "https://api.telegram.org",
  rabbitmqEnabled: parseBoolean(process.env.RABBITMQ_ENABLED, false),
  rabbitmqUrl: process.env.RABBITMQ_URL || "amqp://localhost:5672",
  rabbitmqNotificationQueue: process.env.RABBITMQ_NOTIFICATION_QUEUE || "notifications.queue",
  retryMaxAttempts: parseNumber(process.env.RETRY_MAX_ATTEMPTS, 3),
  retryBaseDelayMs: parseNumber(process.env.RETRY_BASE_DELAY_MS, 500),
  idempotencyTtlMs: parseNumber(process.env.IDEMPOTENCY_TTL_MS, 60 * 60 * 1000)
});
