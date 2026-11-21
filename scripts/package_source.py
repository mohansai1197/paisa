#!/usr/bin/env python3
"""Package Git-visible source files, excluding private configuration and generated output."""
from pathlib import Path
import subprocess
import zipfile
from run_backend import read_env

ROOT = Path(__file__).resolve().parents[1]


if __name__ == "__main__":
    listing = subprocess.check_output(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"], cwd=ROOT)
    files = sorted({Path(name.decode()) for name in listing.split(b"\0") if name})
    values = read_env(ROOT / ".env")
    secrets = [values.get(key, "").encode() for key in
               ["GEMINI_API_KEY", "SMTP_PASSWORD", "SPRING_DATASOURCE_PASSWORD"]]
    for relative in files:
        path = ROOT / relative
        if path.is_symlink() or not path.is_file():
            raise SystemExit("Review non-regular source files before packaging.")
        content = path.read_bytes()
        if any(secret and secret in content for secret in secrets):
            raise SystemExit("A private credential was found in source files; packaging stopped.")
    output = ROOT / "output/paisa-source.zip"
    output.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for relative in files:
            archive.write(ROOT / relative, Path("paisa") / relative)
    print(f"Prepared {output} ({len(files)} source files; private configuration excluded).")
