module.exports = function buildBookingConfirmationTemplate({ recipient, payload }) {
  const name = recipient.name || "Customer";

  return {
    subject: `Booking confirmed: ${payload.bookingCode}`,
    text: `Hello ${name}, your booking ${payload.bookingCode} at ${payload.hotelName} is confirmed from ${payload.checkInDate} to ${payload.checkOutDate}.`,
    html: `
      <p>Hello ${name},</p>
      <p>Your booking <strong>${payload.bookingCode}</strong> has been confirmed.</p>
      <p>Hotel: ${payload.hotelName}</p>
      <p>Stay: ${payload.checkInDate} to ${payload.checkOutDate}</p>
      <p>Total amount: ${payload.totalAmount}</p>
    `
  };
};
