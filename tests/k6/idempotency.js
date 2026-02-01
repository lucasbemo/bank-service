import http from "k6/http";
import { check } from "k6";
import { responseSummary, trackError, trackResponseTime, trackTransferOutcome } from "./utils.js";

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const userCount = 50;
const initialBalance = 1000000;
const seen = {};

export const options = {
  vus: 5,
  duration: "2m",
  thresholds: {
    http_req_failed: [{ threshold: "rate<0.005", abortOnFail: false }],
    http_req_duration: [{ threshold: "p(95)<300", abortOnFail: false }],
    checks: [{ threshold: "rate>0.99", abortOnFail: false }],
  },
};

export function setup() {
  const runId = Date.now();
  const userIds = [];
  for (let i = 0; i < userCount; i += 1) {
    const payload = JSON.stringify({
      fullName: `User ${runId}-${i}`,
      document: `${runId}${i}`,
      email: `user-${runId}-${i}@example.com`,
      password: "secret",
      type: "COMMON",
      initialBalance: initialBalance,
    });
    const res = http.post(`${baseUrl}/users`, payload, {
      headers: { "Content-Type": "application/json" },
    });
    if (res.status === 201) {
      const id = res.json("data.id");
      if (id) {
        userIds.push(id);
      }
    }
  }
  if (userIds.length < 2) {
    throw new Error(`Expected at least 2 users, got ${userIds.length}`);
  }
  const transfers = [
    { payer: userIds[0], payee: userIds[1], value: 7.0, idempotencyKey: `idem-${runId}-1` },
    { payer: userIds[0], payee: userIds[1], value: 7.0, idempotencyKey: `idem-${runId}-1` },
    { payer: userIds[1], payee: userIds[0], value: 4.0, idempotencyKey: `idem-${runId}-2` },
    { payer: userIds[1], payee: userIds[0], value: 4.0, idempotencyKey: `idem-${runId}-2` },
  ];
  return { transfers };
}

export default function (data) {
  const item = data.transfers[__ITER % data.transfers.length];
  const payload = JSON.stringify({ value: item.value, payer: item.payer, payee: item.payee });
  const params = {
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": item.idempotencyKey,
    },
  };
  const res = http.post(`${baseUrl}/transfers`, payload, params);

  // Track errors and response time
  trackTransferOutcome(res);
  trackError(res);
  trackResponseTime(res);

  const transferId = res.json("data.id");
  if (seen[item.idempotencyKey]) {
    check(res, {
      "same transfer id": (r) => r.json("data.id") === seen[item.idempotencyKey],
    });
  } else if (transferId) {
    seen[item.idempotencyKey] = transferId;
  }
  check(res, {
    "status is 2xx": (r) => r.status >= 200 && r.status < 300,
    "has response wrapper": (r) => r.json("success") !== undefined,
  });
}

export function handleSummary(data) {
  const summary = responseSummary(data, "idempotency", baseUrl);
  return {
    "tests/reports/idempotency_summary.json": JSON.stringify(summary, null, 2),
  };
}
