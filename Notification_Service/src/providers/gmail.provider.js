const nodemailer = require("nodemailer");
const mailConfig = require("../config/mail.config");
const logger = require("../utils/logger");
const { ProviderError } = require("../utils/errors");

class GmailProvider {
  constructor() {
    this.enabled = mailConfig.enabled;
    this.transporter = this.enabled
      ? nodemailer.createTransport({
          host: mailConfig.host,
          port: mailConfig.port,
          secure: mailConfig.secure,
          auth: mailConfig.auth
        })
      : null;
  }

  async send({ to, subject, text, html, requestId }) {
    if (!this.enabled) {
      logger.info("Email provider is disabled. Dry run completed.", {
        requestId,
        to,
        subject
      });

      return {
        provider: "gmail",
        dryRun: true,
        accepted: [to]
      };
    }

    try {
      const info = await this.transporter.sendMail({
        from: mailConfig.from,
        to,
        subject,
        text,
        html
      });

      return {
        provider: "gmail",
        messageId: info.messageId,
        accepted: info.accepted
      };
    } catch (error) {
      throw new ProviderError("Failed to send email via Gmail provider", {
        originalMessage: error.message
      });
    }
  }
}

module.exports = GmailProvider;
