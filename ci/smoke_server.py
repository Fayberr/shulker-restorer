#!/usr/bin/env python3
"""CI-only Fabric 26.3 server smoke test. Needs no third-party Python packages."""
from __future__ import annotations

import os
from pathlib import Path
import signal
import socket
import struct
import subprocess
import sys
import time

ROOT = Path(__file__).resolve().parent.parent
RUN = ROOT / "run"
LOG = ROOT / "server-smoke.log"
RCON_PASSWORD = "shulker-restorer-ci-only"


def rcon_packet(sock: socket.socket) -> tuple[int, int, str]:
    header = recv_exact(sock, 4)
    size = struct.unpack("<i", header)[0]
    if not 10 <= size <= 4096:
        raise RuntimeError(f"Invalid RCON packet size: {size}")
    body = recv_exact(sock, size)
    ident, kind = struct.unpack("<ii", body[:8])
    return ident, kind, body[8:-2].decode("utf-8", errors="replace")


def recv_exact(sock: socket.socket, count: int) -> bytes:
    chunks = []
    while count:
        part = sock.recv(count)
        if not part:
            raise ConnectionError("RCON connection closed")
        chunks.append(part)
        count -= len(part)
    return b"".join(chunks)


def rcon_send(sock: socket.socket, ident: int, kind: int, data: str) -> None:
    payload = struct.pack("<ii", ident, kind) + data.encode("utf-8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(payload)) + payload)


def command(sock: socket.socket, ident: int, text: str) -> str:
    rcon_send(sock, ident, 2, text)
    for _ in range(4):
        answer_id, _, response = rcon_packet(sock)
        if answer_id == ident:
            return response
    raise RuntimeError(f"No RCON reply for command: {text}")


def print_log() -> None:
    if LOG.exists():
        print("----- Minecraft server log (last 120 lines) -----")
        print("\n".join(LOG.read_text(errors="replace").splitlines()[-120:]))


def main() -> int:
    RUN.mkdir(exist_ok=True)
    (RUN / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (RUN / "server.properties").write_text(
        "enable-rcon=true\nrcon.port=25575\nrcon.password=" + RCON_PASSWORD +
        "\nonline-mode=false\nlevel-name=world\n",
        encoding="utf-8",
    )
    with LOG.open("wb") as stdout:
        proc = subprocess.Popen(
            ["./gradlew", "runServer", "--console=plain", "--no-configuration-cache"],
            cwd=ROOT,
            stdin=subprocess.DEVNULL,
            stdout=stdout,
            stderr=subprocess.STDOUT,
            start_new_session=True,
        )

    try:
        deadline = time.monotonic() + 240
        while time.monotonic() < deadline:
            if proc.poll() is not None:
                raise RuntimeError(f"Minecraft server quit with exit code {proc.returncode}")
            text = LOG.read_text(errors="replace")
            if "Done (" in text and "For help, type" in text:
                break
            time.sleep(2)
        else:
            raise TimeoutError("Minecraft dedicated server never reached ready state")

        with socket.create_connection(("127.0.0.1", 25575), timeout=8) as sock:
            sock.settimeout(8)
            rcon_send(sock, 1, 3, RCON_PASSWORD)
            for _ in range(4):
                ident, kind, _ = rcon_packet(sock)
                if kind == 2:
                    if ident != 1:
                        raise RuntimeError("RCON authentication failed")
                    break
            else:
                raise RuntimeError("RCON authentication did not complete")

            for ident, minecraft_command in [
                (2, "execute in minecraft:overworld run summon minecraft:shulker 0 100 0"),
                (3, "execute in minecraft:overworld run summon minecraft:shulker_bullet 0 102 2"),
            ]:
                output = command(sock, ident, minecraft_command)
                print(f"{minecraft_command} -> {output}", flush=True)
                if "Summoned new " not in output:
                    raise RuntimeError(f"Summon failed: {output}")

            # Load both mixin target classes and tick the bullet.
            time.sleep(6)
            if proc.poll() is not None:
                raise RuntimeError("Server crashed after summon commands")
            contents = LOG.read_text(errors="replace")
            for marker in ("MixinApplyError", "InvalidInjectionException", "Exception in server tick loop"):
                if marker in contents:
                    raise RuntimeError(f"Minecraft reported {marker}")
            print("Server remained alive after loading Shulker and ShulkerBullet.", flush=True)
            print("Requesting clean shutdown...", flush=True)
            command(sock, 4, "stop")

        proc.wait(timeout=45)
        if proc.returncode != 0:
            raise RuntimeError(f"Server exited nonzero: {proc.returncode}")
        print("Minecraft 26.3 runtime mixin smoke test passed.", flush=True)
        return 0
    except Exception as exc:
        print(f"SMOKE TEST FAILED: {exc}", file=sys.stderr, flush=True)
        print_log()
        return 1
    finally:
        if proc.poll() is None:
            os.killpg(proc.pid, signal.SIGTERM)
            try:
                proc.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(proc.pid, signal.SIGKILL)


if __name__ == "__main__":
    sys.exit(main())
