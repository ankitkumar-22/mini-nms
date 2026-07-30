#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
import sys

API_URL = "http://localhost:8080/api/devices"
SEED_FILE = "/home/ankit/IdeaProjects/mini-nms/demo/seed/seed_50plus_devices.json"

def main():
    try:
        with open(SEED_FILE, "r") as f:
            devices = json.load(f)
    except Exception as e:
        print(f"Failed to read seed file: {e}")
        sys.exit(1)

    print("============================================================")
    print("          Mini-NMS 50+ Device Seeding Automation")
    print("============================================================")
    print(f"Targeting API Endpoint: {API_URL}")
    print(f"Total devices to seed: {len(devices)}\n")

    success = 0
    skipped = 0
    failed = 0

    for idx, dev in enumerate(devices, 1):
        name = dev.get("name")
        ip = dev.get("ipAddress")
        payload = json.dumps({"name": name, "ipAddress": ip}).encode("utf-8")
        req = urllib.request.Request(
            API_URL,
            data=payload,
            headers={"Content-Type": "application/json"},
            method="POST"
        )

        try:
            with urllib.request.urlopen(req) as resp:
                if resp.status == 201:
                    print(f"[{idx:02d}/{len(devices)}] SUCCESS (201 Created)   -> '{name}' ({ip})")
                    success += 1
                else:
                    print(f"[{idx:02d}/{len(devices)}] HTTP {resp.status}          -> '{name}' ({ip})")
                    success += 1
        except urllib.error.HTTPError as e:
            if e.code == 409:
                print(f"[{idx:02d}/{len(devices)}] SKIPPED (409 Duplicate) -> '{name}' ({ip})")
                skipped += 1
            else:
                body = e.read().decode('utf-8', errors='ignore')
                print(f"[{idx:02d}/{len(devices)}] FAILED (HTTP {e.code})   -> '{name}' ({ip}): {body}")
                failed += 1
        except Exception as ex:
            print(f"[{idx:02d}/{len(devices)}] ERROR                  -> '{name}' ({ip}): {ex}")
            failed += 1

    print("\n============================================================")
    print(f"Seeding finished: {success} created, {skipped} skipped, {failed} failed.")
    print("============================================================")

if __name__ == "__main__":
    main()
