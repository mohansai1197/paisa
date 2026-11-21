import test from "node:test";
import assert from "node:assert/strict";
import { api, resetCsrf } from "./api.js";

const json = (value, status = 200) => new Response(JSON.stringify(value), {
  status, headers: { "Content-Type": "application/json" },
});
const html = () => new Response("<!doctype html><html>Sample preview</html>", {
  headers: { "Content-Type": "text/html" },
});
const originalFetch = globalThis.fetch;
test.afterEach(() => { globalThis.fetch = originalFetch; resetCsrf(); });

test("HTML at the CSRF endpoint stops registration before credentials are sent", async () => {
  const requests = [];
  globalThis.fetch = async (url, options) => { requests.push({ url, options }); return html(); };
  await assert.rejects(api("/auth/register", { method: "POST", body: { password: "test-only" } }), /workspace service is not connected/);
  assert.equal(requests.length, 1);
  assert.equal(requests[0].url, "/api/auth/csrf");
  assert.equal(requests[0].options.body, undefined);
});

test("HTML fallback on a successful API request has a helpful message", async () => {
  globalThis.fetch = async () => html();
  await assert.rejects(api("/status"), /hosted sample preview/);
});

test("invalid JSON never exposes a raw parsing error", async () => {
  globalThis.fetch = async () => new Response("broken", { headers: { "Content-Type": "application/json" } });
  await assert.rejects(api("/status"), /returned an invalid response/);
});

test("invalid CSRF payload cannot authorize a registration request", async () => {
  let calls = 0;
  globalThis.fetch = async () => { calls++; return json({ token: "missing-header" }); };
  await assert.rejects(api("/auth/register", { method: "POST", body: {} }), /session could not be initialized/);
  assert.equal(calls, 1);
});

test("connected registration sends cookies and the server CSRF header", async () => {
  const requests = [];
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });
    return url.endsWith("/csrf") ? json({ token: "test-token", headerName: "X-XSRF-TOKEN" }) : json({ id: 1 });
  };
  assert.deepEqual(await api("/auth/register", { method: "POST", body: { name: "Test" } }), { id: 1 });
  assert.equal(requests[1].options.credentials, "include");
  assert.equal(requests[1].options.headers["X-XSRF-TOKEN"], "test-token");
});

test("server validation errors retain their useful messages", async () => {
  globalThis.fetch = async () => json({ message: "That email is already registered." }, 409);
  await assert.rejects(api("/workspace"), /already registered/);
});

test("connection failures are explained during session initialization", async () => {
  globalThis.fetch = async () => { throw new TypeError("Failed to fetch"); };
  await assert.rejects(api("/auth/login", { method: "POST", body: {} }), /Could not reach the workspace service/);
});

test("a report cannot download the HTML preview as CSV", async () => {
  globalThis.fetch = async () => html();
  await assert.rejects(api("/reports?month=2026-11", { download: true }), /not connected/);
});

test("unverified login exposes a verification action without claiming a session", async () => {
  globalThis.fetch = async (url) => url.endsWith("/csrf")
    ? json({ token: "test-token", headerName: "X-XSRF-TOKEN" })
    : json({ message: "Verify your email before signing in.", verificationRequired: true }, 403);
  await assert.rejects(api("/auth/login", { method: "POST", body: {} }),
    (error) => error.verificationRequired === true && /Verify your email/.test(error.message));
});
