const { NOTIFICATION_TYPES } = require("../constants/notification-types");
const { ValidationError } = require("../utils/errors");

const verifyEmailTemplate = require("../templates/email/verify-email.template");
const resetPasswordTemplate = require("../templates/email/reset-password.template");
const bookingConfirmationTemplate = require("../templates/email/booking-confirmation.template");
const paymentSuccessTemplate = require("../templates/email/payment-success.template");
const bookingCancelledTemplate = require("../templates/email/booking-cancelled.template");

const paymentFailedTemplate = require("../templates/telegram/payment-failed.template");
const systemErrorTemplate = require("../templates/telegram/system-error.template");
const renterVerificationTemplate = require("../templates/telegram/renter-verification.template");
const oversellWarningTemplate = require("../templates/telegram/oversell-warning.template");

const templateMap = Object.freeze({
  [NOTIFICATION_TYPES.VERIFY_EMAIL]: verifyEmailTemplate,
  [NOTIFICATION_TYPES.RESET_PASSWORD]: resetPasswordTemplate,
  [NOTIFICATION_TYPES.BOOKING_CONFIRMATION]: bookingConfirmationTemplate,
  [NOTIFICATION_TYPES.PAYMENT_SUCCESS]: paymentSuccessTemplate,
  [NOTIFICATION_TYPES.BOOKING_CANCELLED]: bookingCancelledTemplate,
  [NOTIFICATION_TYPES.PAYMENT_FAILED]: paymentFailedTemplate,
  [NOTIFICATION_TYPES.SYSTEM_ERROR]: systemErrorTemplate,
  [NOTIFICATION_TYPES.RENTER_VERIFICATION_RESULT]: renterVerificationTemplate,
  [NOTIFICATION_TYPES.INVENTORY_ABNORMAL]: oversellWarningTemplate
});

class TemplateService {
  render(notification) {
    const templateBuilder = templateMap[notification.type];

    if (!templateBuilder) {
      throw new ValidationError(`Unsupported notification type: ${notification.type}`);
    }

    return templateBuilder(notification);
  }
}

module.exports = TemplateService;
