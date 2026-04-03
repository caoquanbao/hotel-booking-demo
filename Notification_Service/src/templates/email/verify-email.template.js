module.exports = function buildVerifyEmailTemplate({ recipient, payload }) {
  const name = recipient.name || "User";

  return {
    subject: "Verify your email address",
    text: `Hello ${name}, please verify your email using this link: ${payload.verificationLink}. This link expires in ${payload.expiredInMinutes} minutes.`,
    html: `
      <p>Hello ${name},</p>
      <p>Please verify your email address by clicking the link below:</p>
      <p><a href="${payload.verificationLink}">${payload.verificationLink}</a></p>
      <p>This link expires in <strong>${payload.expiredInMinutes} minutes</strong>.</p>
    `
  };
};
