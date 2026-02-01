import http from "k6/http";
import { basicChecks, responseSummary } from "./utils.js";

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const userCount = 50;
const initialBalance = 1000000;
const minTransferValue = 1;
const maxTransferValue = 20;

export const options = {
  stages: [
    { duration: "1m", target: 10 },
    { duration: "5m", target: 30 },
    { duration: "1m", target: 0 },
  ],
  thresholds: {
    http_req_failed: [{ threshold: "rate<0.01", abortOnFail: false }],
    http_req_duration: [
      { threshold: "p(95)<500", abortOnFail: false },
      { threshold: "p(99)<900", abortOnFail: false },
    ],
  },
};

function randomValue() {
  const value = Math.random() * (maxTransferValue - minTransferValue) + minTransferValue;
  return Math.round(value * 100) / 100;
}

function pickDistinctUsers(userIds) {
  const payer = userIds[Math.floor(Math.random() * userIds.length)];
  let payee = userIds[Math.floor(Math.random() * userIds.length)];
  while (payee === payer) {
    payee = userIds[Math.floor(Math.random() * userIds.length)];
  }
  return { payer, payee };
}

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
  return { runId, userIds };
}

export default function (data) {
  const { payer, payee } = pickDistinctUsers(data.userIds);
  const value = randomValue();
  const idempotencyKey = `load-${data.runId}-${__VU}-${__ITER}`;
  const payload = JSON.stringify({ value: value, payer: payer, payee: payee });
  const params = {
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": idempotencyKey,
    },
  };
  const res = http.post(`${baseUrl}/transfers`, payload, params);
  basicChecks(res);
}

export function handleSummary(data) {
  const summary = responseSummary(data, "load", baseUrl);
  return {
    "tests/reports/load_summary.json": JSON.stringify(summary, null, 2),
  };
}
