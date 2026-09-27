# Homebank API Reference

## 1. General

- Content type: `application/json`
- Auth header: `Authorization: Bearer <access_token>`
- Base URL: environment-specific
- Machine-readable spec: `documentation/api/openapi.yaml`
- Functionality specs:
  - `../private/functionality`
  - `../private/functionality`
  - `../private/functionality`
  - `../private/functionality`
  - `../private/functionality`

### Standard Error Schema (`ApiError`)

```json
{
  "timestamp": "2026-03-11T11:45:00Z",
  "status": 401,
  "error": "Unauthorized",
  "code": "TOKEN_INVALID",
  "message": "Invalid token.",
  "path": "/customers",
  "details": {
    "tokenType": "ACCESS"
  }
}
```

## 2. Security Model

### Authentication Tokens

- Access tokens are returned in JSON and sent on protected requests as `Authorization: Bearer <access_token>`.
- Refresh tokens are stored in a `Secure`, `HttpOnly`, `SameSite=Strict` cookie named `refreshToken`.
- Browser clients must include credentials when calling endpoints that consume the refresh-token cookie.
- Login, refresh, password recovery, and logout set or delete the refresh-token cookie through `Set-Cookie`.

### Public Endpoints (`permitAll`)

- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/register`
- `GET /auth/activate`
- `POST /account-recovery/initiate`
- `POST /account-recovery/authenticate`
- `POST /account-recovery/set-new-password`

### Protected Endpoints

- Every other endpoint requires a valid access token.

### Activation Pending Restriction

Users with status `ACTIVATION_PENDING` can only access:

- `GET /auth/activate`
- `POST /auth/resend-activation`
- `POST /auth/logout`

All other protected endpoints return `403 ACCOUNT_NOT_ACTIVATED`.

### Cross-Cutting Error Behavior

These errors are applied by filters/security and are therefore not always repeated on every endpoint line item:

- Protected endpoints can return:
  - `401 TOKEN_INVALID` (malformed/invalid bearer token)
  - `401 TOKEN_EXPIRED` (expired bearer token)
- Protected endpoints can also return:
  - `401 AUTHENTICATION_FAILED` (authentication context could not be resolved)
  - `403 ACCOUNT_NOT_ACTIVATED` (authenticated user has `ACTIVATION_PENDING`)
- Public (`permitAll`) endpoints ignore `Authorization` bearer tokens.

## 3. Endpoint Groups

## 3.1 Auth

### `POST /auth/login`

- Auth: No
- Request body:

```json
{
  "email": "user@example.com",
  "password": "secret"
}
```

- `200` response:

```json
{
  "accessToken": "<jwt-access-token>",
  "message": "Login successful",
  "accountStatus": "ACTIVE"
}
```

- Sets `refreshToken=<opaque-refresh-token>; Path=/api/auth; Max-Age=<seconds>; Secure; HttpOnly; SameSite=Strict`.

- Errors: `400 VALIDATION_FAILED`, `401 BAD_CREDENTIALS`, `403 ACCOUNT_DISABLED`

### `POST /auth/logout`

- Auth: Yes
- Request body: none
- Refresh-token cookie: optional; when present, its server-side session is revoked.
- `204` response: empty body
- Always deletes the browser cookie with `refreshToken=; Path=/api/auth; Max-Age=0; Secure; HttpOnly; SameSite=Strict`, including when server-side revocation fails after the controller starts processing.
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `500 INTERNAL_SERVER_ERROR`

### `POST /auth/register`

- Auth: No
- Request body:

```json
{
  "email": "new.user@example.com",
  "password": "secret"
}
```

- `200` response body:

```json
"Registration successful. Please check your e-mail to activate your account."
```

- Errors: `400 VALIDATION_FAILED`

### `POST /auth/refresh`

- Auth: No
- Request body: none
- Refresh-token cookie: required

- `200` response:

```json
{
  "accessToken": "<new-jwt-access-token>",
  "message": "Tokens refreshed",
  "accountStatus": "ACTIVE"
}
```

- Rotates the refresh token and sets the replacement in the `/api/auth`-scoped `refreshToken` cookie.

- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `403 ACCOUNT_NOT_ACTIVATED`

### `GET /auth/activate?token=<token>`

- Auth: No
- Query params:
  - `token` (required)
- `200` response:

```json
"Account activated successfully"
```

- Errors: `400 TOKEN_INVALID`, `403 TOKEN_EXPIRED`

### `POST /auth/resend-activation`

- Auth: Yes
- Request body: none
- `200` response:

```json
"Activation email resent successfully. Please check your e-mail."
```

- Notes: email is derived from authenticated principal; current implementation returns `200` even when sending fails.
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`

## 3.2 Account Recovery

### `POST /account-recovery/initiate`

- Auth: No
- Request body:

```json
{
  "email": "user@example.com"
}
```

- `200` response: empty body
- Notes: for valid payloads, the endpoint always returns `200` regardless of whether the e-mail exists.
- Errors: `400 VALIDATION_FAILED`

### `POST /account-recovery/authenticate`

- Auth: No
- Request body:

```json
{
  "email": "user@example.com",
  "password": "<recovery-password>"
}
```

- `200` response:

```json
{
  "token": "<recovery-jwt-token>"
}
```

- Errors: `400 VALIDATION_FAILED`, `401 BAD_CREDENTIALS`

### `POST /account-recovery/set-new-password`

- Auth: No
- Request body:

```json
{
  "recoveryToken": "<recovery-jwt-token>",
  "newPassword": "new-secret",
  "confirmNewPassword": "new-secret"
}
```

- `200` response:

```json
{
  "accessToken": "<jwt-access-token>",
  "message": "Password changed successfully",
  "accountStatus": "ACTIVE"
}
```

- Sets the new refresh token in the secure, HTTP-only, `/api/auth`-scoped `refreshToken` cookie.

- Errors: `400 VALIDATION_FAILED`, `400 PASSWORD_CONFIRMATION_MISMATCH`, `401 TOKEN_INVALID`

## 3.3 Users

### `POST /users/changePassword`

- Auth: Yes
- Changes the authenticated user's password after verifying the old password.
- Request body:

```json
{
  "oldPassword": "old-secret",
  "newPassword": "new-secret",
  "confirmNewPassword": "new-secret"
}
```

- `200` response: empty body
- Errors: `400 PASSWORD_CONFIRMATION_MISMATCH`, `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 BAD_CREDENTIALS`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`

## 3.4 Customers

### `POST /customers`

- Auth: Yes
- Request body:

```json
{
  "name": "Acme AB",
  "description": "Main customer"
}
```

- `200` response: empty body
- Errors: `400 VALIDATION_FAILED`, `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`

### `PUT /customers/{customerId}`

- Auth: Yes
- Path params:
  - `customerId` (int)
- Request body:

```json
{
  "name": "Acme AB Updated",
  "description": "Updated description",
  "rowVersion": "2026-03-11T11:15:00"
}
```

- `200` response: empty body
- Errors: `400 VALIDATION_FAILED`, `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`, `409 ROW_VERSION_MISMATCH`

### `DELETE /customers/{customerId}`

- Auth: Yes; only the owner can delete the customer.
- Required query parameter: `rowVersion`, using the value returned when reading the customer, including any fractional seconds. No request body.
- `204` response: empty body
- Errors: `400 BAD_REQUEST`, `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`, `409 ROW_VERSION_MISMATCH`

### `GET /customers`

- Auth: Yes
- `200` response: `CustomerDTO[]`

```json
[
  {
    "id": 1,
    "name": "Acme AB",
    "description": "Main customer",
    "typeOfCustomerCode": "PRIVATE",
    "customerAmount": 1000,
    "rowVersion": "2026-03-11T11:15:00"
  }
]
```

- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`

### `GET /customers/transactionHeads/{transactionHeadId}`

- Auth: Yes
- `200` response: `CustomersAndTransactionHeadDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `GET /customers/{customerId}`

- Auth: Yes
- `200` response: `CustomerDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `GET /customers/{customerId}/transactionHeads`

- Auth: Yes
- `200` response: `CustomerAndTransactionHeadsDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `GET /customers/{customerId}/transactionHeads/{transactionHeadId}`

- Auth: Yes
- `200` response: `CustomerAndTransactionHeadDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `GET /customers/{customerId}/transactionHeads/{transactionHeadId}/transactionRows`

- Auth: Yes
- `200` response: `CustomerWithTransactionHeadAndRowsDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `GET /customers/{customerId}/transactionHeads/{transactionHeadId}/transactionRows/{transactionRowId}`

- Auth: Yes
- `200` response: `CustomerWithTransactionHeadAndRowDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

## 3.5 Transaction Heads

### `POST /transactionHeads`

- Auth: Yes; read access to both lender and borrower is required. Inaccessible customers return `403 RESOURCE_ACCESS_DENIED`.
- Request body: `CreateTransactionHeadDTO`: `lenderId`, `borrowerId`, `transactionName`, `description`, `startDate`, `prelEndDate`, `endDate`. No client-supplied ID or row version.
- `200` response:

```json
"Transaction head created."
```

- Errors: `400 VALIDATION_FAILED`, `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`

### `PUT /transactionHeads/{transactionHeadId}`

- Auth: Yes; read access through either stored lender or borrower permits editing.
- Request body: `UpdateTransactionHeadDTO`: `transactionName`, `description`, `startDate`, `prelEndDate`, `endDate`, `rowVersion`.
- The path supplies the ID. Lender and borrower are fixed at creation and are not update fields.
- `200` response: `"Transaction head updated"`.
- Errors include `400 VALIDATION_FAILED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`, and `409` for a stale row version.
- The legacy `POST /transactionHeads/save` endpoint has been removed. Use POST for creation and PUT for updates.

### `GET /transactionHeads/{transactionHeadId}`

- Auth: Yes; read access through either lender or borrower is required.
- `200` response: `TransactionHeadDTO`.
- Returns `403` when inaccessible and `404` when missing.

### `DELETE /transactionHeads/{transactionHeadId}`

- Auth: Yes; access through either stored lender or borrower is required.
- Required query parameter: `rowVersion`, using the value returned when reading the head, including any fractional seconds. No request body.
- Example: `DELETE /transactionHeads/22?rowVersion=2026-01-01T00:00:00`.
- Missing or invalid versions return `400 BAD_REQUEST`. Other errors include `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`, and `409 ROW_VERSION_MISMATCH`.
- `204` response: empty body

## 3.6 Transaction Rows

All row operations require access to the parent transaction head. Existing rows use their stored parent for authorization, and updates cannot change that parent. Inaccessible parents return `403 RESOURCE_ACCESS_DENIED`.

### `GET /transactionRows/{transactionRowId}`

- Auth: Yes
- `200` response: `TransactionRowDTO`
- Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `POST /transactionRows`

- Auth: Yes
- Request body: `CreateTransactionRowDTO`: `transactionHeadId`, `transactionRowNo`, `typeOfTransactionCode`, `name`, optional `description`, `paymentDate`, and `amount`.
- The database assigns the ID and version. No ID, version, or display-name fields are required in the request.
- `200` response: `"Transaction row created."`
- Errors: `400 BAD_REQUEST`, `400 VALIDATION_FAILED`, `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `401 AUTHENTICATION_FAILED`, `403 ACCOUNT_NOT_ACTIVATED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`

### `PUT /transactionRows/{transactionRowId}`

- Auth: Yes
- Request body: `UpdateTransactionRowDTO`: `transactionRowNo`, `typeOfTransactionCode`, `name`, optional `description`, `paymentDate`, `amount`, and required `rowVersion`.
- The path identifies the row. Its parent is loaded from the database and cannot be changed by the request.
- `rowVersion` must match the stored version.
- `200` response: `"Transaction row updated."`
- Errors include `400 BAD_REQUEST`, `400 VALIDATION_FAILED`, `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`, and `409 ROW_VERSION_MISMATCH`.
- The legacy `POST /transactionRows/save` endpoint has been removed.

### `DELETE /transactionRows/{transactionRowId}`

- Auth: Yes; access to the stored parent is required.
- Required query parameter: `rowVersion`, using the ISO date-time value returned when reading the row. No request body.
- Example: `DELETE /transactionRows/90?rowVersion=2026-01-01T00:00:00`. Preserve any fractional seconds. Stale versions return `409 ROW_VERSION_MISMATCH`.
- `204` response: empty body
- Missing or invalid versions return `400 BAD_REQUEST`. Other errors include `403 RESOURCE_ACCESS_DENIED`, `404 RESOURCE_NOT_FOUND`, and `409 ROW_VERSION_MISMATCH`.

## 4. Error Codes By Status

- `400`: `BAD_REQUEST`, `TOKEN_INVALID`, `PASSWORD_CONFIRMATION_MISMATCH`, `VALIDATION_FAILED`
- `401`: `AUTHENTICATION_FAILED`, `BAD_CREDENTIALS`, `TOKEN_INVALID`, `TOKEN_EXPIRED`
- `403`: `ACCOUNT_DISABLED`, `ACCOUNT_NOT_ACTIVATED`, `ACCESS_DENIED`, `TOKEN_EXPIRED`, `RESOURCE_ACCESS_DENIED`
- `404`: `RESOURCE_NOT_FOUND`
- `405`: `METHOD_NOT_ALLOWED` (unsupported HTTP method; the `Allow` header lists supported methods)
- `409`: `ENTITY_ALREADY_EXISTS`, `ROW_VERSION_MISMATCH`
- `500`: `INTERNAL_SERVER_ERROR`

## 5. Validation Detail Codes By Endpoint

`ValidationErrorDetail.code` enum values:
- `NOT_NULL`, `NOT_BLANK`, `NOT_EMPTY`, `SIZE`, `PATTERN`, `EMAIL`, `PAST`, `PAST_OR_PRESENT`, `FUTURE`, `FUTURE_OR_PRESENT`, `MIN`, `MAX`, `POSITIVE`, `POSITIVE_OR_ZERO`, `NEGATIVE`, `NEGATIVE_OR_ZERO`, `DIGITS`, `ASSERT_TRUE`, `ASSERT_FALSE`, `VALIDATION_ERROR`, `UNKNOWN`

Endpoints that can return `400 VALIDATION_FAILED`:

| Endpoint | Field -> Validation Code(s) |
|---|---|
| `POST /auth/login` | `email -> NOT_BLANK, EMAIL`; `password -> NOT_BLANK` |
| `POST /auth/register` | `email -> NOT_BLANK, EMAIL`; `password -> NOT_BLANK` |
| `POST /account-recovery/initiate` | `email -> NOT_BLANK, EMAIL` |
| `POST /account-recovery/authenticate` | `email -> NOT_BLANK, EMAIL`; `password -> NOT_BLANK` |
| `POST /account-recovery/set-new-password` | `recoveryToken -> NOT_BLANK`; `newPassword -> NOT_BLANK`; `confirmNewPassword -> NOT_BLANK` |
| `POST /customers` | `name -> NOT_BLANK` |
| `PUT /customers/{customerId}` | `name -> NOT_BLANK`; `rowVersion -> NOT_NULL` |
| `POST /transactionHeads` | `lenderId -> NOT_NULL`; `borrowerId -> NOT_NULL`; `transactionName -> NOT_BLANK`; `startDate -> NOT_NULL` |
| `PUT /transactionHeads/{transactionHeadId}` | `transactionName -> NOT_BLANK`; `startDate -> NOT_NULL`; `rowVersion -> NOT_NULL` |
| `POST /transactionRows` | `transactionHeadId -> NOT_NULL`; `transactionRowNo -> NOT_NULL`; `typeOfTransactionCode -> NOT_BLANK`; `name -> NOT_BLANK`; `paymentDate -> NOT_NULL`; `amount -> NOT_NULL, POSITIVE_OR_ZERO` |
| `PUT /transactionRows/{transactionRowId}` | `transactionRowNo -> NOT_NULL`; `typeOfTransactionCode -> NOT_BLANK`; `name -> NOT_BLANK`; `paymentDate -> NOT_NULL`; `amount -> NOT_NULL, POSITIVE_OR_ZERO`; `rowVersion -> NOT_NULL` |

Endpoints that currently do not emit `VALIDATION_FAILED`:
- `DELETE /transactionHeads/{transactionHeadId}`
- `DELETE /transactionRows/{transactionRowId}`
- `GET /auth/activate`
- `POST /auth/resend-activation`
- `POST /users/changePassword` (no `@Valid` currently applied)
- `DELETE /customers/{customerId}`
- `GET /customers`
- `GET /customers/transactionHeads/{transactionHeadId}`
- `GET /customers/{customerId}`
- `GET /customers/{customerId}/transactionHeads`
- `GET /customers/{customerId}/transactionHeads/{transactionHeadId}`
- `GET /customers/{customerId}/transactionHeads/{transactionHeadId}/transactionRows`
- `GET /customers/{customerId}/transactionHeads/{transactionHeadId}/transactionRows/{transactionRowId}`
- `GET /transactionRows/{transactionRowId}`
