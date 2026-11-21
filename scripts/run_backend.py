#!/usr/bin/env python3
"""Start Paisa with local configuration, without printing credentials."""
import argparse
import os
from pathlib import Path
import shlex
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def read_env(path):
    values = {}
    if path.exists():
        for line in path.read_text().splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            if key.replace("_", "").isalnum():
                parts = shlex.split(value, comments=False)
                values[key] = parts[0] if parts else ""
    return values


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--demo", action="store_true", help="Use the labeled H2 local demo database")
    args = parser.parse_args()
    environment = {**os.environ, **read_env(ROOT / ".env")}
    backend = ROOT / "backend"
    jar = backend / "target/paisa-1.0.0.jar"
    if not jar.exists():
        subprocess.run(["mvn", "package"], cwd=backend, check=True, env=environment)
    # A separate copy keeps a later Maven build from replacing an open runtime jar.
    runtime = backend / ".local/runtime"
    runtime.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="paisa-", dir=runtime) as temporary:
        runtime_jar = Path(temporary) / "paisa.jar"
        shutil.copy2(jar, runtime_jar)
        command = ["java", "-jar", str(runtime_jar)]
        if args.demo:
            command.append("--spring.profiles.active=demo")
        try:
            raise SystemExit(subprocess.call(command, cwd=backend, env=environment))
        except KeyboardInterrupt:
            raise SystemExit(130)
