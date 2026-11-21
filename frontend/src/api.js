const base = import.meta.env?.VITE_API_BASE_URL || "";
let csrf = null;
const unavailable = "The workspace service is not connected here. If you opened the hosted sample preview, use the local Paisa app to sign in or create a workspace.";
async function fetchApi(path, options) {
  try {
    return await fetch(`${base}/api${path}`, { ...options, credentials: "include" });
  } catch {
    throw new Error("Could not reach the workspace service. Check your connection and try again.");
  }
}
async function readJson(response) {
  const type = response.headers.get("content-type") || "";
  if (!/^application\/(?:[\w.+-]+\+)?json\b/i.test(type)) throw new Error(unavailable);
  try {
    return await response.json();
  } catch {
    throw new Error("The workspace service returned an invalid response. Refresh and try again.");
  }
}
async function csrfToken() {
  const response = await fetchApi("/auth/csrf");
  if (!response.ok) throw new Error(unavailable);
  const token = await readJson(response);
  if (!token || typeof token.token !== "string" || !token.token ||
      typeof token.headerName !== "string" || !/^[A-Za-z0-9-]+$/.test(token.headerName))
    throw new Error("The workspace session could not be initialized. Refresh and try again.");
  csrf = token;
  return token;
}
export async function api(path, options = {}) {
  const method = options.method || "GET";
  const headers = { Accept: "application/json", ...options.headers };
  if (method !== "GET") {
    const token = csrf || (await csrfToken());
    headers[token.headerName] = token.token;
  }
  if (options.body) headers["Content-Type"] = "application/json";
  const response = await fetchApi(path, {
    ...options,
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  });
  if (!response.ok) {
    if (response.status === 403) csrf = null;
    const error = await readJson(response);
    const message = path.startsWith("/ai/") && response.status === 503
      ? "AI is not connected or the provider is unavailable. Manual tracking and budgets are still available."
      : error?.message;
    const failure = new Error(typeof message === "string" ? message : "Something went wrong. Please try again.");
    failure.verificationRequired = error?.verificationRequired === true;
    throw failure;
  }
  if (response.status === 204) return null;
  if (options.download) {
    if (!/^text\/csv\b/i.test(response.headers.get("content-type") || "")) throw new Error(unavailable);
    return response.blob();
  }
  return readJson(response);
}
export const resetCsrf = () => { csrf = null; };
