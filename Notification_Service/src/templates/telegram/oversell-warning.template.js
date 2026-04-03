module.exports = function buildOversellWarningTemplate({ payload }) {
  return {
    text: [
      "[WARNING] Inventory abnormal",
      `Hotel ID: ${payload.hotelId}`,
      `Room type ID: ${payload.roomTypeId}`,
      `Date: ${payload.date}`,
      `Available: ${payload.availableRooms}`,
      `Requested: ${payload.requestedRooms}`,
      `Message: ${payload.message || "Potential oversell detected"}`
    ].join("\n")
  };
};
