#!/usr/bin/env python3
"""Enter existing application credentials locally; never paste them into chat."""
from getpass import getpass
import os
from pathlib import Path
import shlex
from run_backend import read_env

ROOT = Path(__file__).resolve().parents[1]


def main():
    values = read_env(ROOT / ".env")
    print("Paisa local setup. Passwords and keys are hidden while you type.")
    print("Leave a field blank to keep its existing value.")
    url = input("MySQL JDBC URL [jdbc:mysql://127.0.0.1:3306/paisa]: ").strip()
    username = input("MySQL application username [paisa_app]: ").strip()
    password = getpass("MySQL application password (not your macOS password): ")
    key = getpass("Gemini API key (optional): ")
    values.setdefault("SPRING_DATASOURCE_URL", "jdbc:mysql://127.0.0.1:3306/paisa")
    values.setdefault("SPRING_DATASOURCE_USERNAME", "paisa_app")
    for name, value in [("SPRING_DATASOURCE_URL", url), ("SPRING_DATASOURCE_USERNAME", username),
                        ("SPRING_DATASOURCE_PASSWORD", password), ("GEMINI_API_KEY", key)]:
        if value:
            values[name] = value
    values.setdefault("GEMINI_MODEL", "gemini-3.5-flash-lite")
    values.setdefault("SECURE_COOKIES", "false")
    path = ROOT / ".env"
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    os.fchmod(descriptor, 0o600)
    with os.fdopen(descriptor, "w") as output:
        output.write("# Private local configuration. Do not commit or share.\n")
        for name, value in values.items():
            output.write(f"{name}={shlex.quote(value)}\n")
    print("Saved private .env. Restart the backend to apply it. No credentials were printed.")


if __name__ == "__main__":
    main()
