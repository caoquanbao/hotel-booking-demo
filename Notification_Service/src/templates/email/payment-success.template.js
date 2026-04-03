module.exports = function buildPaymentSuccessTemplate({ recipient, payload }) {
  const name = recipient.name || "Customer";

  return {
    subject: `Payment successful: ${payload.paymentCode}`,
    text: `Hello ${name}, your payment ${payload.paymentCode} for booking ${payload.bookingCode} was successful.`,
    html: `
      <p>Hello ${name},</p>
      <p>Your payment was successful.</p>
      <p>Payment code: <strong>${payload.paymentCode}</strong></p>
      <p>Booking code: <strong>${payload.bookingCode}</strong></p>
      <p>Amount: ${payload.amount}</p>
    `
  };
};
