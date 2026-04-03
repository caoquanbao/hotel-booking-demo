# AUTH DEV TESTING

This document is for the DEV profile only.
The seed runner creates safe local test data so a blank database can be used to exercise the auth flow from Postman.

## Seeded users

When the app starts with the `dev` profile, these users are created or refreshed:

- `normal@test.com` / `123456`
- `unverified@test.com` / `123456`
- `otp@test.com` / `123456`

Their intended states are:

- `normal@test.com`: `email_verified = true`, `auth_status = NORMAL`
- `unverified@test.com`: `email_verified = false`, `auth_status = NORMAL`
- `otp@test.com`: `email_verified = true`, `auth_status = OTP_REQUIRED`

## Seeded raw token and OTP values

The runner generates fresh DEV tokens at startup and prints the raw values to the application log:

- `DEV EMAIL VERIFY TOKEN (raw): ...`
- `DEV OTP (raw): ...`
- `DEV RESET TOKEN (raw): ...`

Use those raw values directly in Postman.
Only the hashes are stored in the database.

## Run with the dev profile

Example commands:

```powershell
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

```powershell
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Or run the jar:

```powershell
java -jar target/auth-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## Postman flow guide

Base URL:

```text
http://localhost:8080
```

### 1. Login success

Request:

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "normal@test.com",
  "password": "123456"
}
```

Expected:

- `200 OK`
- response contains `accessToken` and `refreshToken`

### 2. Login blocked because email is not verified

Request:

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "unverified@test.com",
  "password": "123456"
}
```

Expected:

- `403`
- body contains `EMAIL_NOT_VERIFIED`

### 3. Verify email

Take the raw token from the startup log: `DEV EMAIL VERIFY TOKEN (raw): ...`

Request:

```http
GET /api/auth/verify-email?token=<RAW_VERIFY_TOKEN>
```

Expected:

- `200 OK`
- response body is `Email verified`

After that, retry the normal login request for `unverified@test.com`.

### 4. Login user in OTP_REQUIRED state

Request:

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "otp@test.com",
  "password": "123456"
}
```

Expected:

- `403`
- body contains `OTP_REQUIRED`

### 5. Verify OTP

Take the raw OTP from the startup log: `DEV OTP (raw): ...`

Request:

```http
POST /api/auth/verify-otp
Content-Type: application/json
```

Body:

```json
{
  "email": "otp@test.com",
  "otp": "123456"
}
```

Replace the sample OTP above with the raw value from the log.

Expected:

- `200 OK`
- response contains `accessToken` and `refreshToken`

### 6. Resend OTP

Use this when the OTP flow is active and not in cooldown.

Request:

```http
POST /api/auth/resend-otp
Content-Type: application/json
```

Body:

```json
{
  "email": "otp@test.com"
}
```

Expected:

- `200 OK`
- body contains `OTP_SENT`

Note:
The current implementation seeds a startup OTP, but the resend endpoint does not expose the raw OTP in the API response.

### 7. Refresh token

Use the `refreshToken` returned by a successful login or OTP verification.

Request:

```http
POST /api/auth/refresh
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "<REFRESH_TOKEN_FROM_LOGIN>"
}
```

Expected:

- `200 OK`
- response contains a new `accessToken` and a new `refreshToken`
- the old refresh token is revoked

### 8. Logout

Request:

```http
POST /api/auth/logout?email=normal@test.com
```

Expected:

- `200 OK`
- body contains `LOGOUT_SUCCESS`

Note:
This revokes all refresh tokens for the user.

### 9. Forgot password

Request:

```http
POST /api/auth/forgot-password
Content-Type: application/json
```

Body:

```json
{
  "email": "normal@test.com"
}
```

Expected:

- `200 OK`

Note:
In DEV, a startup reset token is already seeded and printed to the log.
This is useful even if mail delivery is not configured.

### 10. Reset password

Take the raw token from the startup log: `DEV RESET TOKEN (raw): ...`

Request:

```http
POST /api/auth/reset-password
Content-Type: application/json
```

Body:

```json
{
  "token": "<RAW_RESET_TOKEN>",
  "newPassword": "NewPassword@123"
}
```

Expected:

- `200 OK`
- all refresh tokens for that user are revoked

### 11. Change password

This endpoint expects an authenticated principal.
The current codebase already has the endpoint, but whether it works through Postman depends on the broader JWT authentication wiring in the app.

Request:

```http
POST /api/users/change-password
Content-Type: application/json
Authorization: Bearer <ACCESS_TOKEN>
```

Body:

```json
{
  "oldPassword": "123456",
  "newPassword": "NewPassword@123"
}
```

## Notes

- It does not run in production unless the `dev` profile is explicitly enabled.
- It reuses the existing `PasswordEncoder`, `SecureToken`, `TokenHash`, `OtpUtil`, entities, and repositories.
- Existing DEV users are updated instead of duplicated.
- Fresh DEV tokens are generated at startup so Postman testing is possible even with an empty database.
