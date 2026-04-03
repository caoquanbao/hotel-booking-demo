module.exports = function buildResetPasswordTemplate({ recipient, payload }) {
  const name = recipient.name || "User";

  return {
    subject: "Reset your password",
    text: `Hello ${name}, reset your password here: ${payload.resetLink}. This link expires in ${payload.expiredInMinutes} minutes.`,
    html: `
      <p>Hello ${name},</p>
      <p>You requested a password reset.</p>
      <p><a href="${payload.resetLink}">Reset password</a></p>
      <p>This link expires in <strong>${payload.expiredInMinutes} minutes</strong>.</p>
    `
  };
};
