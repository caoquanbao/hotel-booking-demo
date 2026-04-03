const express = require("express");
const requestIdMiddleware = require("./middlewares/request-id.middleware");
const errorMiddleware = require("./middlewares/error.middleware");
const healthRoutes = require("./routes/health.routes");
const createNotificationRouter = require("./routes/notification.routes");

function createApp({ notificationService }) {
  const app = express();

  app.use(express.json());
  app.use(requestIdMiddleware);

  app.use("/health", healthRoutes);
  app.use("/api/notifications", createNotificationRouter(notificationService));

  app.use(errorMiddleware);

  return app;
}

module.exports = createApp;
