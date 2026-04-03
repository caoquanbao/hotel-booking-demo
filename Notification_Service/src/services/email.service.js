class EmailService {
  constructor(gmailProvider) {
    this.gmailProvider = gmailProvider;
  }

  async send({ recipient, template, requestId }) {
    return this.gmailProvider.send({
      to: recipient.email,
      subject: template.subject,
      text: template.text,
      html: template.html,
      requestId
    });
  }
}

module.exports = EmailService;
