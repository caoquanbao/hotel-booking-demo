const { CHANNELS, resolveChannelByType } = require("../../constants/channels");
const { NOTIFICATION_TYPES } = require("../../constants/notification-types");

describe("resolveChannelByType", () => {
  test("maps user-facing notifications to EMAIL", () => {
    expect(resolveChannelByType(NOTIFICATION_TYPES.VERIFY_EMAIL)).toBe(CHANNELS.EMAIL);
    expect(resolveChannelByType(NOTIFICATION_TYPES.RESET_PASSWORD)).toBe(CHANNELS.EMAIL);
    expect(resolveChannelByType(NOTIFICATION_TYPES.BOOKING_CONFIRMATION)).toBe(CHANNELS.EMAIL);
  });

  test("maps internal notifications to TELEGRAM", () => {
    expect(resolveChannelByType(NOTIFICATION_TYPES.PAYMENT_FAILED)).toBe(CHANNELS.TELEGRAM);
    expect(resolveChannelByType(NOTIFICATION_TYPES.SYSTEM_ERROR)).toBe(CHANNELS.TELEGRAM);
    expect(resolveChannelByType(NOTIFICATION_TYPES.INVENTORY_ABNORMAL)).toBe(CHANNELS.TELEGRAM);
  });
});
