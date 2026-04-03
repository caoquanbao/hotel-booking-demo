module.exports = function buildSystemErrorTemplate({ payload }) {
  return {
    text: [
      "[CRITICAL] System error",
      `Service: ${payload.serviceName}`,
      `Error code: ${payload.errorCode || "N/A"}`,
      `Message: ${payload.errorMessage}`,
      `Occurred at: ${payload.occurredAt || new Date().toISOString()}`
    ].join("\n")
  };
};
