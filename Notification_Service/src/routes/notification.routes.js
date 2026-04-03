const express = require("express");
const NotificationController = require("../controllers/notification.controller");
const validate = require("../middlewares/validate.middleware");
const asyncHandler = require("../utils/async-handler");
const { notificationSchema } = require("../validators/notification.validator");

function createNotificationRouter(notificationService) {
  const router = express.Router();
  const controller = new NotificationController(notificationService);

  router.post("/send", validate(notificationSchema), asyncHandler(controller.sendNotification));

  return router;
}

module.exports = createNotificationRouter;
