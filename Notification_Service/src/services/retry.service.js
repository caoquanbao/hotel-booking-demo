const env = require("../config/env");
const logger = require("../utils/logger");

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

class RetryService {
  constructor(options = {}) {
    this.maxAttempts = options.maxAttempts || env.retryMaxAttempts;
    this.baseDelayMs = options.baseDelayMs || env.retryBaseDelayMs;
  }

  async execute(task, context = {}) {
    let lastError;

    for (let attempt = 1; attempt <= this.maxAttempts; attempt += 1) {
      try {
        return await task(attempt);
      } catch (error) {
        lastError = error;

        logger.warn("Notification delivery attempt failed", {
          requestId: context.requestId,
          type: context.type,
          attempt,
          maxAttempts: this.maxAttempts,
          errorMessage: error.message
        });

        if (attempt < this.maxAttempts) {
          await wait(this.baseDelayMs * attempt);
        }
      }
    }

    throw lastError;
  }
}

module.exports = RetryService;
