module.exports = function buildPaymentFailedTemplate({ payload }) {
  return {
    text: [
      "[ALERT] Payment failed",
      `Booking: ${payload.bookingCode}`,
      `Payment: ${payload.paymentCode}`,
      `Amount: ${payload.amount}`,
      `Reason: ${payload.reason || "Unknown"}`
    ].join("\n")
  };
};
