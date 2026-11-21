#!/usr/bin/env python3
"""Read-only checks for Paisa's combined frontend/API deployment. No credentials needed."""
import argparse
import http.cookiejar
import json
from urllib.error import HTTPError
from urllib.parse import urlparse
from urllib.request import build_opener, HTTPCookieProcessor, Request

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("url", help="HTTPS deployment origin, or a loopback HTTP origin")
args = parser.parse_args()
origin = args.url.rstrip("/")
target = urlparse(origin)
if target.username or target.password or target.query or target.fragment or target.path not in ("", "/"):
    parser.error("Use an origin without credentials, query, fragment, or path.")
if target.scheme != "https" and not (target.scheme == "http" and target.hostname in ("localhost", "127.0.0.1", "::1")):
    parser.error("Remote deployments must use HTTPS.")
cookies = http.cookiejar.CookieJar()
client = build_opener(HTTPCookieProcessor(cookies))

def read(path, expected=200):
    try:
        response = client.open(Request(origin + path), timeout=20)
    except HTTPError as error:
        response = error
    with response:
        assert response.status == expected, f"{path}: expected {expected}, got {response.status}"
        assert urlparse(response.url).netloc == target.netloc, "Unexpected redirect to another origin"
        return response.headers.get("Content-Type", ""), response.read()

kind, page = read("/")
assert "text/html" in kind and b'Paisa' in page, "Frontend HTML missing"
kind, body = read("/api/status")
assert "application/json" in kind, "API returned HTML instead of JSON"
status = json.loads(body)
assert status.get("service") == "Paisa", "Wrong service"
assert status.get("storage") == "MySQL", "Production must use MySQL"
kind, body = read("/api/auth/csrf")
assert "application/json" in kind and json.loads(body).get("token"), "CSRF token unavailable"
if target.scheme == "https":
    assert all(cookie.secure for cookie in cookies), "An HTTPS cookie is missing Secure"
kind, body = read("/api/workspace", 401)
assert "application/json" in kind, "Unauthenticated API must return JSON"
print("PASS: frontend, MySQL configuration, JSON API, CSRF, and protected workspace")
print("AI configured:", bool(status.get("aiConfigured")))
print("Email configured:", bool(status.get("emailVerificationConfigured")))
print("Still verify registration email, sign-in, persistence, AI, and password reset in the browser.")
