#!/usr/bin/env python3
"""Private, loopback-only credential entry for Paisa. Never logs submitted values."""
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from html import escape
from urllib.parse import parse_qs
import json
import os
import re
import secrets
import shlex
import tempfile
from run_backend import read_env

ROOT = Path(__file__).resolve().parents[1]
HOST, PORT = "127.0.0.1", 8766
ORIGIN = f"http://{HOST}:{PORT}"
NONCE = secrets.token_urlsafe(32)


def save_values(values):
    merged = {**read_env(ROOT / ".env"), **values}
    with tempfile.NamedTemporaryFile(mode="w", dir=ROOT, prefix=".env.", delete=False) as output:
        temporary = Path(output.name)
        os.fchmod(output.fileno(), 0o600)
        output.write("# Paisa private configuration. Never commit or share.\n")
        for key, value in merged.items():
            output.write(f"{key}={shlex.quote(value)}\n")
    os.replace(temporary, ROOT / ".env")


def page(message="", error=False):
    message_html = f'<p class="message {"error" if error else ""}">{escape(message)}</p>' if message else ""
    return f"""<!doctype html><html lang="en"><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>Paisa · Private local setup</title>
<style>
*{{box-sizing:border-box}}body{{margin:0;background:#f4f8f6;color:#18382f;font:16px/1.6 system-ui,sans-serif}}
main{{max-width:690px;margin:40px auto;padding:28px}}h1{{font-size:34px;letter-spacing:-1px;margin:0}}
.badge{{font-size:14px;color:#456b5d}}section{{background:white;padding:24px;border:1px solid #d8e5de;border-radius:16px;margin:22px 0}}
h2{{font-size:22px;margin:0 0 8px}}label{{display:block;margin:16px 0;font-weight:600}}
input{{display:block;width:100%;padding:12px;border:1px solid #bbcfc2;border-radius:8px;font:inherit;margin-top:6px}}
input:focus{{outline:3px solid #93c9b3}}button{{background:#147c5c;color:white;border:0;border-radius:8px;padding:13px 20px;font:inherit;cursor:pointer}}
small{{font-size:14px;color:#536b60}}.message{{background:#e1f5e9;padding:16px;border-radius:10px}}.error{{background:#ffe8e8;color:#8e2929}}
a{{color:#116d50}}.foot{{font-size:14px;color:#536b60}}
</style><main><span class="badge">PAISA · PRIVATE LOCAL SETUP</span><h1>Connect your workspace.</h1>
<p>Enter credentials here on your computer. They are saved only in the project's private <code>.env</code> file and are never printed in chat.</p>
{message_html}<section><h2>Connect Gemini AI</h2><p>Use an API key from Google AI Studio. Paisa sends expense descriptions for categorization and calculated monthly totals for insights to Google when you request AI results. Use fictional demo records for the hackathon.</p>
<form method="post" action="/save"><input type="hidden" name="nonce" value="{NONCE}"><input type="hidden" name="kind" value="ai">
<label>Gemini API key<input type="password" name="key" required autocomplete="off" maxlength="4096" placeholder="Paste your key privately"></label>
<label>Gemini model<input name="model" value="gemini-3.5-flash-lite" required pattern="[a-zA-Z0-9._-]+" maxlength="100"></label>
<button type="submit">Save AI connection</button><p><small>Your key stays on the backend. Saving does not start generation or change billing.</small></p></form></section>
<section><h2>Connect MySQL</h2><p>Use credentials for a database account authorized for Paisa. You can finish this separately from AI setup.</p>
<form method="post" action="/save"><input type="hidden" name="nonce" value="{NONCE}"><input type="hidden" name="kind" value="database">
<label>JDBC connection URL<input name="url" value="jdbc:mysql://127.0.0.1:3306/paisa" required maxlength="500" autocomplete="off"></label>
<label>Database application username<input name="username" required maxlength="100" autocomplete="off" placeholder="paisa_app"></label>
<label>Database application password<input type="password" name="password" required maxlength="4096" autocomplete="off"></label>
<button type="submit">Save database connection</button></form></section>
<section><h2>Connect Gmail verification emails</h2><p>Use a Gmail app password created for Paisa. Never enter your regular Google password. Paisa will use this account to send requested verification codes.</p>
<form method="post" action="/save"><input type="hidden" name="nonce" value="{NONCE}"><input type="hidden" name="kind" value="email">
<label>Sender Gmail address<input type="email" name="sender" required maxlength="180" autocomplete="off"></label>
<label>Gmail app password<input type="password" name="app_password" required maxlength="32" autocomplete="off" placeholder="Enter the app password privately"></label>
<button type="submit">Save email connection</button><p><small>Uses smtp.gmail.com:587 with encrypted STARTTLS. Saving alone does not send an email. Restart Paisa afterward.</small></p></form></section>
<p class="foot">This page is available only on 127.0.0.1. After saving, return to the chat and say “saved” so the backend can restart with the new settings. Leave the tab open until setup is finished.</p></main></html>"""


class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def respond(self, body, status=200, content_type="text/html; charset=utf-8"):
        payload = body.encode()
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(payload)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("Referrer-Policy", "no-referrer")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.send_header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'")
        self.end_headers()
        self.wfile.write(payload)

    def host_valid(self):
        return self.headers.get("Host") == f"{HOST}:{PORT}"

    def do_GET(self):
        if not self.host_valid():
            return self.respond("Invalid host", 403)
        if self.path == "/":
            return self.respond(page())
        if self.path == "/save":
            self.send_response(303)
            self.send_header("Location", "/")
            self.end_headers()
            return
        if self.path == "/health":
            values = read_env(ROOT / ".env")
            return self.respond(json.dumps({"aiConfigured": bool(values.get("GEMINI_API_KEY")), "databaseConfigured": all(values.get(k) for k in ["SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD"])}), content_type="application/json")
        return self.respond("Not found", 404)

    def do_POST(self):
        # Some embedded browsers omit Origin or use an opaque (null) origin for
        # native form submissions. The unpredictable form nonce remains mandatory;
        # loopback binding and the exact Host check also prevent DNS rebinding.
        origin = self.headers.get("Origin")
        if not self.host_valid() or origin not in (None, "null", ORIGIN) or self.path != "/save":
            return self.respond("Request not allowed", 403)
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if not 0 < length <= 16000:
                raise ValueError()
            fields = parse_qs(self.rfile.read(length).decode(), max_num_fields=12)
            value = lambda key: fields.get(key, [""])[0]
            if not secrets.compare_digest(value("nonce"), NONCE):
                return self.respond("Invalid setup token", 403)
            if any("\n" in item or "\r" in item or "\x00" in item for items in fields.values() for item in items):
                raise ValueError()
            if value("kind") == "ai":
                key, model = value("key").strip(), value("model").strip()
                if not key or len(key) > 4096 or not re.fullmatch(r"[a-zA-Z0-9._-]{1,100}", model):
                    raise ValueError()
                save_values({"GEMINI_API_KEY": key, "GEMINI_MODEL": model})
            elif value("kind") == "database":
                url, username, password = value("url").strip(), value("username").strip(), value("password")
                if not url.startswith("jdbc:mysql://") or len(url) > 500 or not username or len(username) > 100 or not password or len(password) > 4096:
                    raise ValueError()
                save_values({"SPRING_DATASOURCE_URL": url, "SPRING_DATASOURCE_USERNAME": username, "SPRING_DATASOURCE_PASSWORD": password})
            elif value("kind") == "email":
                sender = value("sender").strip()
                app_password = value("app_password").replace(" ", "")
                if not re.fullmatch(r"[^\s@]+@(?:gmail\.com|googlemail\.com)", sender, re.I) or len(sender) > 180 or not re.fullmatch(r"[a-zA-Z0-9]{16}", app_password):
                    raise ValueError()
                save_values({"SMTP_HOST": "smtp.gmail.com", "SMTP_PORT": "587", "SMTP_USERNAME": sender,
                    "SMTP_PASSWORD": app_password, "MAIL_FROM": sender, "SMTP_AUTH": "true", "SMTP_STARTTLS": "true", "SMTP_SSL": "false"})
            else:
                raise ValueError()
            return self.respond(page("Saved privately. Return to the chat and say ‘saved’ so I can restart and verify the connection."))
        except Exception:
            return self.respond(page("Could not save these settings. Check the fields and try again. No credential values are displayed.", True), 400)


if __name__ == "__main__":
    print(f"Paisa private local setup: {ORIGIN}", flush=True)
    HTTPServer((HOST, PORT), Handler).serve_forever()
