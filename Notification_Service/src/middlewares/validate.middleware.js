const { ValidationError } = require("../utils/errors");

function validate(schema) {
  return function validationMiddleware(req, res, next) {
    const result = schema.safeParse(req.body);

    if (!result.success) {
      return next(
        new ValidationError("Request validation failed", result.error.flatten())
      );
    }

    req.validatedBody = result.data;
    return next();
  };
}

module.exports = validate;
