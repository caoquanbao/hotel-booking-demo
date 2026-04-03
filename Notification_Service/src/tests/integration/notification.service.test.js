const NotificationService = require("../../services/notification.service");
const RetryService = require("../../services/retry.service");
const IdempotencyService = require("../../services/idempotency.service");
const TemplateService = require("../../services/template.service");
const { NOTIFICATION_TYPES } = require("../../constants/notification-types");

describe("NotificationService", () => {
  test("sends verify email notification successfully", async () => {
    const emailService = {
      send: jest.fn().mockResolvedValue({ provider: "gmail", dryRun: true })
    };

    const telegramService = {
      send: jest.fn()
    };

    const service = new NotificationService({
      emailService,
      telegramService,
      templateService: new TemplateService(),
      retryService: new RetryService({ maxAttempts: 1, baseDelayMs: 1 }),
      idempotencyService: new IdempotencyService({ ttlMs: 1000 })
    });

    const result = await service.send(
      {
        type: NOTIFICATION_TYPES.VERIFY_EMAIL,
        recipient: {
          email: "user@example.com",
          name: "Tester"
        },
        payload: {
          verificationLink: "http://localhost:8080/verify?token=abc123",
          expiredInMinutes: 15
        },
        metadata: {
          idempotencyKey: "verify-tester-1"
        }
      },
      "request-123"
    );

    expect(result.status).toBe("SENT");
    expect(result.channel).toBe("EMAIL");
    expect(emailService.send).toHaveBeenCalledTimes(1);
    expect(telegramService.send).not.toHaveBeenCalled();
  });
});
