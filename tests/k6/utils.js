import { check } from "k6";
import { Counter, Trend } from "k6/metrics";

export const errorTypes = {
  connection_refused: new Counter("error_connection_refused"),
  timeout: new Counter("error_timeout"),
  dns_lookup: new Counter("error_dns_lookup"),
  tls_handshake: new Counter("error_tls_handshake"),
  http_4xx: new Counter("error_http_4xx"),
  http_5xx: new Counter("error_http_5xx"),
  no_response: new Counter("error_no_response"),
  unknown: new Counter("error_unknown"),
};

export const responseTimeTrend = new Trend("response_time_by_status");
export const transferRequests = new Counter("transfer_requests_total");
export const transferFailures = new Counter("transfer_requests_failed");
export const transferDuration = new Trend("transfer_req_duration");

export function loadJson(path) {
  return JSON.parse(open(path));
}

export function trackError(res) {
  if (!res) {
    errorTypes.no_response.add(1);
    return "no_response";
  }

  if (res.error) {
    const errorMsg = res.error.toLowerCase();
    if (errorMsg.includes("connection refused")) {
      errorTypes.connection_refused.add(1);
      return "connection_refused";
    } else if (errorMsg.includes("timeout")) {
      errorTypes.timeout.add(1);
      return "timeout";
    } else if (errorMsg.includes("dns") || errorMsg.includes("lookup")) {
      errorTypes.dns_lookup.add(1);
      return "dns_lookup";
    } else if (errorMsg.includes("tls") || errorMsg.includes("handshake")) {
      errorTypes.tls_handshake.add(1);
      return "tls_handshake";
    } else {
      errorTypes.unknown.add(1);
      return "unknown";
    }
  }

  if (res.status >= 400 && res.status < 500) {
    errorTypes.http_4xx.add(1);
    return `http_4xx_${res.status}`;
  } else if (res.status >= 500) {
    errorTypes.http_5xx.add(1);
    return `http_5xx_${res.status}`;
  }

  return null;
}

export function trackTransferOutcome(res) {
  transferRequests.add(1);

  if (!res || res.error) {
    transferFailures.add(1);
    return;
  }

  if (res.status >= 400) {
    transferFailures.add(1);
  }
}

export function trackResponseTime(res) {
  if (res && res.timings && res.timings.duration !== undefined) {
    const status = res.status || "unknown";
    responseTimeTrend.add(res.timings.duration, { status: status.toString() });
    transferDuration.add(res.timings.duration);
  }
}

function formatThresholds(metric) {
  if (!metric || !metric.thresholds) {
    return [];
  }
  return Object.entries(metric.thresholds).map(([expr, result]) => ({
    threshold: expr,
    ok: result.ok,
  }));
}

function metricValue(metric, key) {
  if (!metric) {
    return undefined;
  }
  if (metric.values && metric.values[key] !== undefined) {
    return metric.values[key];
  }
  return metric[key];
}

function collectErrorCounts(metrics) {
  const errors = {};
  const errorCounters = [
    "error_connection_refused",
    "error_timeout",
    "error_dns_lookup",
    "error_tls_handshake",
    "error_http_4xx",
    "error_http_5xx",
    "error_no_response",
    "error_unknown",
  ];

  errorCounters.forEach((key) => {
    const count = metricValue(metrics[key], "count");
    if (count > 0) {
      errors[key.replace("error_", "")] = count;
    }
  });

  return errors;
}

function collectCheckBreakdown(metrics) {
  const breakdown = [];

  Object.entries(metrics || {}).forEach(([key, metric]) => {
    if (!key.startsWith("checks{")) {
      return;
    }
    const tagStart = key.indexOf("check:");
    if (tagStart === -1) {
      return;
    }
    const nameStart = tagStart + "check:".length;
    const nameEnd = key.lastIndexOf("}");
    const name = key.substring(nameStart, nameEnd > nameStart ? nameEnd : key.length).trim();
    if (!name) {
      return;
    }

    const passed = metricValue(metric, "passes");
    const failed = metricValue(metric, "fails");
    const rate = metricValue(metric, "rate");

    breakdown.push({
      name,
      passed: passed !== undefined ? passed : null,
      failed: failed !== undefined ? failed : null,
      rate: rate !== undefined ? rate : null,
    });
  });

  breakdown.sort((a, b) => {
    const aFails = a.failed || 0;
    const bFails = b.failed || 0;
    if (bFails !== aFails) {
      return bFails - aFails;
    }
    return a.name.localeCompare(b.name);
  });

  return breakdown;
}

export function printConsoleSummary(summary) {
  console.log("\n" + "=".repeat(60));
  console.log(`K6 TEST SUMMARY: ${summary.test.toUpperCase()}`);
  console.log("=".repeat(60));
  console.log(`Target: ${summary.baseUrl}`);
  console.log(`Duration: ${(summary.durationMs / 1000).toFixed(1)}s`);
  console.log(`Verdict: ${summary.verdict}`);
  console.log("-".repeat(60));

  console.log("\n📊 REQUESTS:");
  console.log(`  Total: ${summary.totalRequests}`);
  console.log(`  Iterations: ${summary.totalIterations}`);

  if (summary.totalRequests > 0) {
    const successRate = ((summary.successRate || 0) * 100).toFixed(1);
    const errorRate = ((summary.errorRate || 0) * 100).toFixed(1);
    console.log(`  Success Rate: ${successRate}%`);
    console.log(`  Error Rate: ${errorRate}%`);
    if (summary.successCount !== null && summary.errorCount !== null) {
      console.log(`  Success Count: ${summary.successCount}`);
      console.log(`  Error Count: ${summary.errorCount}`);
    }
  } else {
    console.log(`  ❌ NO REQUESTS EXECUTED - App may be down`);
  }

  console.log("\n⏱️  LATENCY:");
  const latency = summary.latencyMs || {};
  const latencyValues = [latency.avg, latency.p50, latency.p95, latency.p99, latency.max].filter(
    (value) => value !== null && value !== undefined,
  );
  if (latencyValues.length > 0) {
    if (latency.avg !== null && latency.avg !== undefined) {
      console.log(`  Avg: ${latency.avg.toFixed(1)}ms`);
    }
    if (latency.p50 !== null && latency.p50 !== undefined) {
      console.log(`  P50: ${latency.p50.toFixed(1)}ms`);
    }
    if (latency.p95 !== null && latency.p95 !== undefined) {
      console.log(`  P95: ${latency.p95.toFixed(1)}ms`);
    }
    if (latency.p99 !== null && latency.p99 !== undefined) {
      console.log(`  P99: ${latency.p99.toFixed(1)}ms`);
    }
    if (latency.max !== null && latency.max !== undefined) {
      console.log(`  Max: ${latency.max.toFixed(1)}ms`);
    }
  } else {
    console.log(`  ❌ No latency data available`);
  }

  console.log("\n✅ CHECKS (assertions):");
  if (summary.checks.rate !== null) {
    console.log(`  Pass Rate: ${(summary.checks.rate * 100).toFixed(1)}%`);
    console.log(`  Passed: ${summary.checks.passed}`);
    console.log(`  Failed: ${summary.checks.failed}`);
    if (summary.checks.perRequestAvg !== null) {
      console.log(`  Avg per Request: ${summary.checks.perRequestAvg.toFixed(2)}`);
    }
  } else {
    console.log(`  ❌ No check data available`);
  }

  if (summary.checkBreakdown && summary.checkBreakdown.length > 0) {
    console.log("\n✅ CHECKS BREAKDOWN:");
    summary.checkBreakdown.forEach((checkItem) => {
      const passed = checkItem.passed !== null ? checkItem.passed : 0;
      const failed = checkItem.failed !== null ? checkItem.failed : 0;
      const rate = checkItem.rate !== null ? (checkItem.rate * 100).toFixed(1) : "-";
      console.log(`  ${checkItem.name}: ${passed} passed, ${failed} failed (${rate}%)`);
    });
  }

  const errorKeys = Object.keys(summary.errorBreakdown || {});
  if (errorKeys.length > 0) {
    console.log("\n❌ ERROR BREAKDOWN:");
    errorKeys.forEach((key) => {
      console.log(`  ${key}: ${summary.errorBreakdown[key]}`);
    });
  }

  if (summary.failedThresholds.length > 0) {
    console.log("\n⚠️  FAILED THRESHOLDS:");
    summary.failedThresholds.forEach((t) => console.log(`  - ${t}`));
  }

  if (summary.notes.length > 0) {
    console.log("\n📝 NOTES:");
    summary.notes.forEach((note) => console.log(`  - ${note}`));
  }

  console.log("=".repeat(60) + "\n");
}

export function responseSummary(data, name, baseUrl) {
  const metrics = data.metrics;
  const httpReqDuration = metrics.http_req_duration || {};
  const httpReqFailed = metrics.http_req_failed || {};
  const httpReqs = metrics.http_reqs || {};
  const checks = metrics.checks || {};
  const iterations = metrics.iterations || {};
  const transferReqs = metrics.transfer_requests_total || {};
  const transferReqFailed = metrics.transfer_requests_failed || {};
  const transferReqDuration = metrics.transfer_req_duration || {};

  const totalRequests = metricValue(transferReqs, "count") || metricValue(httpReqs, "count") || 0;
  const totalIterations = metricValue(iterations, "count") || 0;

  const errorBreakdown = collectErrorCounts(metrics);
  const transportErrorKeys = [
    "connection_refused",
    "timeout",
    "dns_lookup",
    "tls_handshake",
    "no_response",
    "unknown",
  ];
  const transportErrors = transportErrorKeys.reduce(
    (sum, key) => sum + (errorBreakdown[key] || 0),
    0,
  );

  // Handle case where requests failed (error rate calculation)
  let errorRate = 0;
  let successRate = 0;
  let errorCount = null;
  let successCount = null;

  if (totalRequests > 0) {
    const failedCount = metricValue(transferReqFailed, "count");
    if (failedCount !== undefined) {
      errorRate = failedCount / totalRequests;
      successRate = 1 - errorRate;
      errorCount = failedCount;
      successCount = Math.max(totalRequests - failedCount, 0);
    } else {
      errorRate = transportErrors / totalRequests;
      successRate = 1 - errorRate;
      errorCount = Math.round(transportErrors);
      successCount = Math.max(totalRequests - errorCount, 0);
    }
  } else {
    // No requests were made - 100% error rate
    errorRate = 1;
    successRate = 0;
    errorCount = 0;
    successCount = 0;
  }

  const failedThresholds = [];
  const thresholds = {
    http_req_failed: formatThresholds(httpReqFailed),
    http_req_duration: formatThresholds(httpReqDuration),
    checks: formatThresholds(checks),
  };

  Object.entries(thresholds).forEach(([metric, rules]) => {
    rules.forEach((rule) => {
      if (!rule.ok) {
        failedThresholds.push(`${metric}: ${rule.threshold}`);
      }
    });
  });

  const notes = [];
  if (totalRequests === 0) {
    notes.push("No requests executed. Check BASE_URL or app availability.");
  } else if (errorRate >= 0.5) {
    notes.push(`High error rate detected: ${(errorRate * 100).toFixed(1)}%`);
  }
  if ((errorBreakdown.http_4xx || 0) > 0 || (errorBreakdown.http_5xx || 0) > 0) {
    const http4xx = errorBreakdown.http_4xx || 0;
    const http5xx = errorBreakdown.http_5xx || 0;
    notes.push(`HTTP error responses: 4xx=${http4xx}, 5xx=${http5xx}`);
  }
  if (
    checks.passes !== undefined &&
    checks.fails !== undefined &&
    checks.passes + checks.fails > totalRequests
  ) {
    notes.push("Checks are per-assertion; counts can exceed request totals.");
  }
  if (failedThresholds.length > 0) {
    notes.push("Some thresholds failed.");
  }

  // Determine verdict - threshold failures don't block when all requests fail
  // (app crash scenario should show FAIL but still generate report)
  const hasAppCrash = totalRequests === 0 && totalIterations > 0;
  const hasHighErrorRate = errorRate >= 0.5;
  const verdict =
    failedThresholds.length === 0 && !hasAppCrash && !hasHighErrorRate
      ? "PASS"
      : "FAIL";

  const checkBreakdown = collectCheckBreakdown(metrics);

  const summary = {
    test: name,
    baseUrl: baseUrl,
    timestamp: new Date().toISOString(),
    durationMs: data.state.testRunDurationMs,
    totalRequests: totalRequests,
    totalIterations: totalIterations,
    successRate: successRate,
    errorRate: errorRate,
    successCount: successCount,
    errorCount: errorCount,
    latencyMs: {
      avg:
        metricValue(transferReqDuration, "avg") !== undefined
          ? metricValue(transferReqDuration, "avg")
          : metricValue(httpReqDuration, "avg") !== undefined
          ? metricValue(httpReqDuration, "avg")
          : null,
      p50:
        metricValue(transferReqDuration, "p(50)") !== undefined
          ? metricValue(transferReqDuration, "p(50)")
          : metricValue(httpReqDuration, "p(50)") !== undefined
          ? metricValue(httpReqDuration, "p(50)")
          : httpReqDuration.percentiles
          ? httpReqDuration.percentiles["50.0"]
          : null,
      p95:
        metricValue(transferReqDuration, "p(95)") !== undefined
          ? metricValue(transferReqDuration, "p(95)")
          : metricValue(httpReqDuration, "p(95)") !== undefined
          ? metricValue(httpReqDuration, "p(95)")
          : httpReqDuration.percentiles
          ? httpReqDuration.percentiles["95.0"]
          : null,
      p99:
        metricValue(transferReqDuration, "p(99)") !== undefined
          ? metricValue(transferReqDuration, "p(99)")
          : metricValue(httpReqDuration, "p(99)") !== undefined
          ? metricValue(httpReqDuration, "p(99)")
          : httpReqDuration.percentiles
          ? httpReqDuration.percentiles["99.0"]
          : null,
      max:
        metricValue(transferReqDuration, "max") !== undefined
          ? metricValue(transferReqDuration, "max")
          : metricValue(httpReqDuration, "max") !== undefined
          ? metricValue(httpReqDuration, "max")
          : null,
    },
    checks: {
      rate: metricValue(checks, "rate") !== undefined ? metricValue(checks, "rate") : null,
      passed: metricValue(checks, "passes") !== undefined ? metricValue(checks, "passes") : null,
      failed: metricValue(checks, "fails") !== undefined ? metricValue(checks, "fails") : null,
      perRequestAvg:
        totalRequests > 0 &&
        metricValue(checks, "passes") !== undefined &&
        metricValue(checks, "fails") !== undefined
          ? (metricValue(checks, "passes") + metricValue(checks, "fails")) / totalRequests
          : null,
      breakdown: checkBreakdown,
    },
    checkBreakdown: checkBreakdown,
    thresholds: thresholds,
    failedThresholds: failedThresholds,
    verdict: verdict,
    notes: notes,
    errorBreakdown: errorBreakdown,
  };

  // Print to console
  printConsoleSummary(summary);

  return summary;
}

export function basicChecks(res) {
  // Track error type and response time
  trackTransferOutcome(res);
  trackError(res);
  trackResponseTime(res);

  check(res, {
    "status is 2xx": (r) => r && r.status >= 200 && r.status < 300,
    "has response wrapper": (r) => {
      if (!r || !r.body) {
        return false;
      }
      const contentType = r.headers && r.headers["Content-Type"] ? r.headers["Content-Type"] : "";
      if (contentType.indexOf("application/json") === -1) {
        return false;
      }
      try {
        return r.json("success") !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
}
