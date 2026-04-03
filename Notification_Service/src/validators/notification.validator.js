const { z } = require("zod");
const env = require("../config/env");
const { NOTIFICATION_TYPES } = require("../constants/notification-types");
const { CHANNELS, resolveChannelByType } = require("../constants/channels");

const typeEnum = z.nativeEnum(NOTIFICATION_TYPES);

const recipientSchema = z
  .object({
    email: z.string().email().optional(),
    name: z.string().min(1).optional(),
    telegramChatId: z.string().min(1).optional()
  })
  .default({});

const metadataSchema = z
  .object({
    requestId: z.string().min(1).optional(),
    idempotencyKey: z.string().min(1).optional()
  })
  .optional()
  .default({});

const payloadByType = {
  [NOTIFICATION_TYPES.VERIFY_EMAIL]: z.object({
    verificationLink: z.string().url(),
    expiredInMinutes: z.number().int().positive()
  }),
  [NOTIFICATION_TYPES.RESET_PASSWORD]: z.object({
    resetLink: z.string().url(),
    expiredInMinutes: z.number().int().positive()
  }),
  [NOTIFICATION_TYPES.BOOKING_CONFIRMATION]: z.object({
    bookingCode: z.string().min(1),
    hotelName: z.string().min(1),
    checkInDate: z.string().min(1),
    checkOutDate: z.string().min(1),
    totalAmount: z.union([z.string(), z.number()])
  }),
  [NOTIFICATION_TYPES.PAYMENT_SUCCESS]: z.object({
    paymentCode: z.string().min(1),
    bookingCode: z.string().min(1),
    amount: z.union([z.string(), z.number()])
  }),
  [NOTIFICATION_TYPES.BOOKING_CANCELLED]: z.object({
    bookingCode: z.string().min(1),
    reason: z.string().optional()
  }),
  [NOTIFICATION_TYPES.PAYMENT_FAILED]: z.object({
    bookingCode: z.string().min(1),
    paymentCode: z.string().min(1),
    amount: z.union([z.string(), z.number()]),
    reason: z.string().optional()
  }),
  [NOTIFICATION_TYPES.SYSTEM_ERROR]: z.object({
    serviceName: z.string().min(1),
    errorCode: z.string().optional(),
    errorMessage: z.string().min(1),
    occurredAt: z.string().optional()
  }),
  [NOTIFICATION_TYPES.RENTER_VERIFICATION_RESULT]: z.object({
    renterId: z.union([z.string(), z.number()]),
    renterName: z.string().min(1),
    status: z.string().min(1),
    reason: z.string().optional()
  }),
  [NOTIFICATION_TYPES.INVENTORY_ABNORMAL]: z.object({
    hotelId: z.union([z.string(), z.number()]),
    roomTypeId: z.union([z.string(), z.number()]),
    date: z.string().min(1),
    availableRooms: z.number(),
    requestedRooms: z.number(),
    message: z.string().optional()
  })
};

const notificationSchema = z
  .object({
    type: typeEnum,
    recipient: recipientSchema,
    payload: z.record(z.any()),
    metadata: metadataSchema
  })
  .superRefine((data, ctx) => {
    const payloadSchema = payloadByType[data.type];
    const payloadResult = payloadSchema.safeParse(data.payload);

    if (!payloadResult.success) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Payload does not match notification type requirements",
        path: ["payload"]
      });
    }

    const channel = resolveChannelByType(data.type);

    if (channel === CHANNELS.EMAIL && !data.recipient.email) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Recipient email is required for email notifications",
        path: ["recipient", "email"]
      });
    }

    if (
      channel === CHANNELS.TELEGRAM &&
      !data.recipient.telegramChatId &&
      !env.telegramDefaultChatId
    ) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "telegramChatId is required when TELEGRAM_DEFAULT_CHAT_ID is not configured",
        path: ["recipient", "telegramChatId"]
      });
    }
  });

module.exports = {
  notificationSchema
};
