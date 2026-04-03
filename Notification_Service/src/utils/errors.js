class AppError extends Error {
  constructor(message, statusCode = 500, details = null) {
    super(message);
    this.name = this.constructor.name;
    this.statusCode = statusCode;
    this.details = details;
    Error.captureStackTrace(this, this.constructor);
  }
}

class ValidationError extends AppError {
  constructor(message, details = null) {
    super(message, 400, details);
  }
}

class ConflictError extends AppError {
  constructor(message, details = null) {
    super(message, 409, details);
  }
}

class ProviderError extends AppError {
  constructor(message, details = null) {
    super(message, 502, details);
  }
}

module.exports = {
  AppError,
  ValidationError,
  ConflictError,
  ProviderError
};
