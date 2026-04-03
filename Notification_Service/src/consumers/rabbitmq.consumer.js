class RabbitMqConsumer {
  constructor({ notificationService, enabled }) {
    this.notificationService = notificationService;
    this.enabled = enabled;
  }

  async start() {
    if (!this.enabled) {
      return;
    }
  }
}

module.exports = RabbitMqConsumer;
