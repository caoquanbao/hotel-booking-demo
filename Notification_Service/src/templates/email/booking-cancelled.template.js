module.exports = function buildBookingCancelledTemplate({ recipient, payload }) {
  const name = recipient.name || "Customer";

  return {
    subject: `Booking cancelled: ${payload.bookingCode}`,
    text: `Hello ${name}, your booking ${payload.bookingCode} has been cancelled. Reason: ${payload.reason || "N/A"}.`,
    html: `
      <p>Hello ${name},</p>
      <p>Your booking <strong>${payload.bookingCode}</strong> has been cancelled.</p>
      <p>Reason: ${payload.reason || "N/A"}</p>
      <p>If needed, please contact support for more details.</p>
    `
  };
};
