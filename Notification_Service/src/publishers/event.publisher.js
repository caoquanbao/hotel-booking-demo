const logger = require("../utils/logger");

class EventPublisher {
  async publish(eventName, payload) {
    logger.info("Event publisher placeholder invoked", {
      eventName,
      payload
    });
  }
}

module.exports = EventPublisher;
