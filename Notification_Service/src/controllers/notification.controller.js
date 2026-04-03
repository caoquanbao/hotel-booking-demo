const { success } = require("../utils/api-response");

class NotificationController {
  constructor(notificationService) {
    this.notificationService = notificationService;
  }

  sendNotification = async (req, res) => {
    const result = await this.notificationService.send(req.validatedBody, req.requestId);

    const message = result.duplicated
      ? "Duplicate idempotency key detected. Returning cached notification result."
      : "Notification processed successfully";

    res.status(200).json(success(result, message));
  };
}

module.exports = NotificationController;
