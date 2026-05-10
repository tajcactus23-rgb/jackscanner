#!/usr/bin/env python3
"""
JackScanner - BLE Scanner for Android (Termux/Root)
Inspired by lookout.py and PoliceDetector
Scans for Axon devices (OUI: 00:25:DF)
"""
import asyncio
import sys
import os
import subprocess
import time
from datetime import datetime

# Target OUI - Axon International
TARGET_OUI = "00:25:DF"
TARGET_OUI_LOWER = "00:25:df"

detected = set()
alert_play = False

def log_detection(address, name, rssi):
    """Log device detection"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"\n[!] TARGET DETECTED!")
    print(f"    Address: {address}")
    print(f"    Name: {name}")
    print(f"    RSSI: {rssi} dBm")
    print(f"    Time: {timestamp}")
    
    # Log to file
    with open("detections.log", "a") as f:
        f.write(f"[{timestamp}] {address} | {name} | {rssi} dBm\n")
    
    # Play alert
    trigger_alert()

def trigger_alert():
    """Play alert sound"""
    try:
        # Try various alert methods
        subprocess.run(["termux-notification", "-t", "TARGET DETECTED", "-c", "Axon device in range!"], 
                    capture_output=True)
    except:
        pass
    
    try:
        # Vibrate
        subprocess.run(["termux-vibrate", "-d", "500"], capture_output=True)
    except:
        pass
    
    try:
        # Play sound
        subprocess.run(["play", "-q", "/system/media/audio/ui/Notification.ogg"], capture_output=True)
    except:
        pass

def check_hcitool():
    """Check if hcitool is available"""
    try:
        subprocess.run(["which", "hcitool"], capture_output=True, check=True)
        return True
    except:
        return False

async def scan_bleak():
    """Scan using bleak (non-root)"""
    try:
        import bleak
    except ImportError:
        print("Installing bleak...")
        subprocess.run([sys.executable, "-m", "pip", "install", "bleak"], check=True)
        import bleak
    
    scanner = bleak.BleakScanner()
    
    while True:
        devices = await scanner.discover()
        current_time = datetime.now().strftime("%H:%M:%S")
        
        for address, device in devices.items():
            # Check OUI
            addr_upper = address.upper().replace("-", ":")
            oui = addr_upper[:8] if len(addr_upper) >= 8 else addr_upper
            
            if oui in (TARGET_OUI, TARGET_OUI_LOWER):
                name = device.name or "Unknown"
                rssi = device.rssi
                
                if address not in detected:
                    detected.add(address)
                    log_detection(address, name, rssi)
                else:
                    print(f"\r[{current_time}] {address[:17]} {name} {rssi} dBm", end="")
            else:
                print(f"\r[{current_time}] Scanning... ({len(devices)} devices)", end="")
        
        await asyncio.sleep(2)

def scan_hcitool():
    """Scan using hcitool (root required)"""
    print("[*] Using hcitool (root mode)")
    
    while True:
        try:
            result = subprocess.run(
                ["hcitool", "-i", "hci0", "lescan", "--duplicates"],
                capture_output=True, text=True, timeout=10
            )
            
            lines = result.stdout.split("\n")
            for line in lines:
                if TARGET_OUI_LOWER in line.lower() or TARGET_OUI in line.upper():
                    parts = line.split()
                    if len(parts) >= 2:
                        address = parts[0]
                        name = parts[1] if len(parts) > 1 else "Unknown"
                        
                        if address not in detected:
                            detected.add(address)
                            log_detection(address, name, 0)
        except Exception as e:
            print(f"Error: {e}")
        
        time.sleep(2)

def main():
    global alert_play
    
    print("=" * 50)
    print(" JackScanner - BLE Device Scanner")
    print(" Target OUI: 00:25:DF (Axon)")
    print("=" * 50)
    print()
    
    # Check for root/hcitool
    if check_hcitool():
        print("[*] hcitool available - using root mode")
        try:
            scan_hcitool()
        except KeyboardInterrupt:
            print("\nStopping...")
            subprocess.run(["killall", "hcitool"], capture_output=True)
    else:
        print("[*] Using bleak (non-root mode)")
        try:
            asyncio.run(scan_bleak())
        except KeyboardInterrupt:
            print("\nStopping...")
        except ImportError:
            print("Error: Install bleak with: pip install bleak")
            sys.exit(1)

if __name__ == "__main__":
    main()