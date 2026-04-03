const IdempotencyService = require("../../services/idempotency.service");

describe("IdempotencyService", () => {
  test("returns duplicated state for an existing key", () => {
    const service = new IdempotencyService({ ttlMs: 1000 });

    const first = service.begin("sample-key");
    const second = service.begin("sample-key");

    expect(first.duplicated).toBe(false);
    expect(second.duplicated).toBe(true);
  });

  test("stores completed result", () => {
    const service = new IdempotencyService({ ttlMs: 1000 });
    const result = { status: "SENT" };

    service.begin("sample-key");
    service.complete("sample-key", result);

    expect(service.get("sample-key").result).toEqual(result);
    expect(service.get("sample-key").status).toBe("COMPLETED");
  });
});
