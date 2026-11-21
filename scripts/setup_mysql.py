#!/usr/bin/env python3
"""Prepare a Paisa-only MySQL account, then verify it without reading saved root passwords."""
import argparse
import os
from pathlib import Path
import secrets
import shlex
import shutil
import subprocess
import tempfile
from run_backend import read_env
from local_setup import save_values

ROOT = Path(__file__).resolve().parents[1]
LOCAL = ROOT / ".local"
SETTINGS = LOCAL / "mysql-connection.env"
SQL = LOCAL / "mysql-setup.sql"


def private_write(path, content):
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    os.fchmod(descriptor, 0o600)
    with os.fdopen(descriptor, "w") as output:
        output.write(content)


def prepare():
    LOCAL.mkdir(exist_ok=True)
    if SETTINGS.exists() or SQL.exists():
        print(f"Existing private setup retained: {SQL}")
        return
    username = "paisa_app_" + secrets.token_hex(3)
    password = secrets.token_urlsafe(32)
    values = {"SPRING_DATASOURCE_URL": "jdbc:mysql://127.0.0.1:3306/paisa",
              "SPRING_DATASOURCE_USERNAME": username, "SPRING_DATASOURCE_PASSWORD": password}
    private_write(SETTINGS, "".join(f"{key}={shlex.quote(value)}\n" for key, value in values.items()))
    private_write(SQL, f"""-- Private Paisa setup. Includes a generated application password: do not share.
-- Review and execute in your existing authenticated local MySQL Workbench connection.
-- No existing account passwords are changed. Access is limited to paisa.* on localhost.
CREATE DATABASE IF NOT EXISTS paisa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '{username}'@'localhost' IDENTIFIED BY '{password}';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
ON paisa.* TO '{username}'@'localhost';
SELECT 'Paisa database account is ready' AS status;
""")
    print(f"Prepared private SQL script: {SQL}")
    print("Execute it in your authenticated local MySQL Workbench, then run this script with --verify.")
    print("No saved root password was read and no database changes have run yet.")


def verify():
    values = read_env(SETTINGS)
    if not values:
        raise SystemExit("Run without --verify to prepare the private script first.")
    mysql = shutil.which("mysql") or "/usr/local/mysql/bin/mysql"
    config = f"[client]\nhost=127.0.0.1\nport=3306\nuser={values['SPRING_DATASOURCE_USERNAME']}\npassword={values['SPRING_DATASOURCE_PASSWORD']}\ndatabase=paisa\n"
    with tempfile.NamedTemporaryFile(mode="w", dir=LOCAL, prefix="mysql-client-", delete=False) as output:
        path = Path(output.name)
        os.fchmod(output.fileno(), 0o600)
        output.write(config)
    try:
        result = subprocess.run([mysql, f"--defaults-extra-file={path}", "--connect-timeout=5", "--batch", "--skip-column-names", "-e", "SELECT DATABASE(), VERSION();"], capture_output=True, text=True, timeout=10)
        if result.returncode:
            raise SystemExit("MySQL connection is not ready. Execute the prepared SQL script in Workbench, then try --verify again.")
        save_values(values)
        print("MySQL application login verified. Private backend settings saved.")
        print("Database and server version: " + result.stdout.strip())
        print("Restart the backend without --demo to use MySQL.")
    finally:
        path.unlink(missing_ok=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--verify", action="store_true")
    args = parser.parse_args()
    verify() if args.verify else prepare()
