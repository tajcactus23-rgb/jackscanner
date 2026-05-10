#!/usr/bin/env python3
"""
BlueMeanie - Police Proximity Detector
Inspired by lookout.py and PoliceDetector
Scans for Axon/Police devices (OUI: 00:25:DF)
"""
import asyncio
import sys
import os
import subprocess
import time
from datetime import datetime

# Target OUI - Axon International (Taser)
TARGET_OUI = "00:25:DF"
TARGET_OUI_LOWER = "00:25:df"

detected = set()

def log_detection(address, name, rssi):
    """Log device detection"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"\n[!!!] POLICE DETECTED!")
    print(f"    Address: {address}")
    print(f"    Device: {name}")
    print(f"    Signal: {rssi} dBm")
    print(f"    Time: {timestamp}")
    print(f"    >>> POLICE MAY BE NEARBY <<<")
    
    # Log to file
    with open("police_detections.log", "a") as f:
        f.write(f"[{timestamp}] {address} | {name} | {rssi}\n")
    
    # ALERT!
    trigger_alert()

def trigger_alert():
    """Alert notifications"""
    try:
        subprocess.run(["termux-notification", "-t", "⚠️ POLICE DETECTED", 
                      "-c", "Axon device in range!"], capture_output=True)
    except: pass
    
    try:
        subprocess.run(["termux-vibrate", "-d", "1000"], capture_output=True)
    except: pass

def check_hcitool():
    """Check hcitool availability"""
    try:
        subprocess.run(["which", "hcitool"], capture_output=True, check=True)
        return True
    except: return False

async def scan_bleak():
    """Bleak scanner"""
    try: import bleak
    except ImportError:
        subprocess.run([sys.executable, "-m", "pip", "install", "bleak"])
        import bleak
    
    scanner = bleak.BleakScanner()
    while True:
        devices = await scanner.discover()
        for addr, dev in devices.items():
            oui = addr.replace("-", ":")[:8].upper()
            if oui in (TARGET_OUI, TARGET_OUI_LOWER):
                name = dev.name or "Axon Device"
                rssi = dev.rssi
                if addr not in detected:
                    detected.add(addr)
                    log_detection(addr, name, rssi)
        await asyncio.sleep(1)

def scan_hcitool():
    """hcitool scanner"""
    print("[*] Using hcitool")
    while True:
        try:
            r = subprocess.run(["hcitool", "lescan"], capture_output=True, text=True, timeout=10)
            for line in r.stdout.split("\n"):
                if TARGET_OUI_LOWER in line.lower():
                    parts = line.split()
                    addr = parts[0] if parts else ""
                    if addr and addr not in detected:
                        detected.add(addr)
                        log_detection(addr, "Axon", 0)
        except: pass
        time.sleep(2)

def main():
    print("=" * 50)
    print(" BlueMeanie - Police Proximity Detector")
    print(" Target: 00:25:DF (Axon International)")
    print("=" * 50)
    
    if check_hcitool():
        try: scan_hcitool()
        except KeyboardInterrupt: pass
    else:
        try: asyncio.run(scan_bleak())
        except KeyboardInterrupt: pass

if __name__ == "__main__":
    main()