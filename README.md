# 🎯 Karat Interview Mock Services

Deployed on Railway. Use these APIs in your OrderService interview question.

---

## Base URL
```
https://YOUR-RAILWAY-URL.railway.app
```

---

## 📦 Inventory Service

### Check Stock
```bash
# In Stock
curl GET /api/inventory/PROD-IN-STOCK

# Out of Stock
curl GET /api/inventory/PROD-OUT-STOCK

# Race Condition (alternates true/false each call)
curl GET /api/inventory/PROD-RACE

# Low Stock (only 1 left)
curl GET /api/inventory/PROD-LOW-STOCK
```

**Responses:**
```json
// Available
{
  "productId": "PROD-IN-STOCK",
  "available": true,
  "quantity": 50,
  "warehouseId": "WH-BLR-01"
}

// Out of Stock
{
  "productId": "PROD-OUT-STOCK",
  "available": false,
  "quantity": 0,
  "warehouseId": "WH-BLR-01",
  "reservedUntil": "2099-01-15T10:30:00Z"
}
```

### Deduct Stock
```bash
curl -X POST /api/inventory/deduct \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-IN-STOCK", "quantity": 2}'
```

### Reserve Stock (Proper Pattern)
```bash
curl -X POST /api/inventory/reserve \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-IN-STOCK", "quantity": 2, "reservationId": "RES-UUID-001"}'
```

### Release Reservation
```bash
curl -X DELETE /api/inventory/reserve/RES-UUID-001
```

---

## 💳 Payment Service

### Charge
```bash
# SUCCESS (amount < 100)
curl -X POST /api/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "amount": 50.00, "idempotencyKey": "ORDER-001"}'

# PENDING — ambiguous! (amount 100-500)
curl -X POST /api/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "amount": 250.00, "idempotencyKey": "ORDER-002"}'

# TIMEOUT — 5s delay then PENDING (amount > 500)
curl -X POST /api/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "amount": 750.00, "idempotencyKey": "ORDER-003"}'

# INSUFFICIENT FUNDS (amount == 999)
curl -X POST /api/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "amount": 999, "idempotencyKey": "ORDER-004"}'

# SERVICE UNAVAILABLE HTTP 503 (amount == 503)
curl -X POST /api/payments/charge \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "amount": 503, "idempotencyKey": "ORDER-005"}'
```

**Responses:**
```json
// SUCCESS
{
  "transactionId": "TXN-A1B2C3D4",
  "status": "SUCCESS",
  "amount": 50.00,
  "processedAt": "2024-01-15T10:30:00Z"
}

// PENDING (ambiguous — did it go through?!)
{
  "transactionId": "TXN-E5F6G7H8",
  "status": "PENDING",
  "amount": 250.00,
  "processedAt": null,
  "message": "Payment is being processed. Poll /status/{transactionId} for updates."
}

// FAILED
{
  "transactionId": "TXN-I9J0K1L2",
  "status": "FAILED",
  "amount": 999.0,
  "failureReason": "INSUFFICIENT_FUNDS"
}
```

### Poll Payment Status
```bash
# Resolves SUCCESS after 3 calls
curl GET /api/payments/status/TXN-E5F6G7H8

# Always SUCCESS
curl GET /api/payments/status/TXN-SETTLED

# Always FAILED
curl GET /api/payments/status/TXN-FAILED

# Always PENDING
curl GET /api/payments/status/TXN-PENDING
```

### Refund (Saga Compensation)
```bash
curl -X POST /api/payments/refund \
  -H "Content-Type: application/json" \
  -d '{"transactionId": "TXN-A1B2C3D4", "amount": 50.00, "reason": "ORDER_CANCELLED"}'
```

---

## 🔔 Notification Service

### Send Notification
```bash
# EMAIL — QUEUED (normal)
curl -X POST /api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "orderId": "ORD-001", "channel": "EMAIL"}'

# SMS — QUEUED but slow (2s delay)
curl -X POST /api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "orderId": "ORD-001", "channel": "SMS"}'

# PUSH — randomly QUEUED or DROPPED
curl -X POST /api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId": "USR-001", "orderId": "ORD-001", "channel": "PUSH"}'
```

**Response (Always HTTP 200!):**
```json
{
  "notificationId": "NOTIF-A1B2C3D4",
  "status": "QUEUED",
  "channel": "EMAIL",
  "queuedAt": "2024-01-15T10:30:00Z",
  "warning": "Delivery not guaranteed. Use /status/{notificationId} to track."
}
```

---

## 🔥 Tricky Scenarios Summary

| Scenario | How to trigger |
|---|---|
| Payment ambiguity | amount between 100-500 |
| Payment timeout | amount > 500 |
| Race condition inventory | productId = PROD-RACE |
| Double charge test | same idempotencyKey twice |
| Silent notification drop | channel = PUSH |
| Service unavailable | amount = 503 |
| Insufficient funds | amount = 999 |
| Slow inventory | productId = PROD-SLOW |

---

## Health Check
```bash
curl GET /actuator/health
```

---

## Railway GitHub Actions Deployment

This repository deploys automatically to Railway with GitHub Actions.

Required GitHub repository secret:

```text
RAILWAY_TOKEN=<your Railway project token>
```

Optional GitHub repository secret, only needed when the Railway project has multiple services:

```text
RAILWAY_SERVICE_NAME=<your Railway service name>
```

The workflow runs on pushes to `main` or `master`, and can also be started manually from the GitHub Actions tab.
