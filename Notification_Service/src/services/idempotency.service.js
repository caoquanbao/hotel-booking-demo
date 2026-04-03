const env = require("../config/env");

class IdempotencyService {
  constructor(options = {}) {
    this.ttlMs = options.ttlMs || env.idempotencyTtlMs;
    this.store = new Map();
  }

  cleanup() {
    const now = Date.now();

    for (const [key, value] of this.store.entries()) {
      if (value.expiresAt <= now) {
        this.store.delete(key);
      }
    }
  }

  get(key) {
    if (!key) {
      return null;
    }

    this.cleanup();
    return this.store.get(key) || null;
  }

  begin(key) {
    if (!key) {
      return { skipped: true };
    }

    this.cleanup();
    const existing = this.store.get(key);

    if (existing) {
      return { duplicated: true, record: existing };
    }

    const record = {
      status: "PROCESSING",
      createdAt: new Date().toISOString(),
      expiresAt: Date.now() + this.ttlMs,
      result: null
    };

    this.store.set(key, record);
    return { duplicated: false, record };
  }

  complete(key, result) {
    if (!key) {
      return;
    }

    this.store.set(key, {
      status: "COMPLETED",
      createdAt: new Date().toISOString(),
      expiresAt: Date.now() + this.ttlMs,
      result
    });
  }

  fail(key) {
    if (!key) {
      return;
    }

    this.store.delete(key);
  }
}

module.exports = IdempotencyService;
