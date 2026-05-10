#!/usr/bin/env python3
"""
BlueMeanie - BLE Scanner for Axon/TASER Devices
Scans for Axon devices using multiple detection fingerprints:
- MAC OUI: 00:25:DF, FC:A9:E8
- Device name patterns: AXON_CAMERA, TASER, AXON_BODY
- FCC Grantee: X4G

Inspired by community reports from lookout.py, PoliceDetector
Reference: FCC ID X4GS01506, X4GS00947
"""
import asyncio
import sys
import os
import subprocess
import time
from datetime import datetime

# Known Axon OUIs from IEEE/FCC
TARGET_OUIS = [
    "00:25:DF",  # Primary Axon OUI
    "FC:A9:E8",  # Additional observed
    "X4G",      # FCC Grantee prefix
]

# Known Axon device name patterns
AXON_PATTERNS = [
    "AXON",
    "TASER", 
    "AXON_CAMERA",
    "AXON_BODY",
    "AXON_SIGNAL",
    "AXON_SIDEARM",
]

detected = set()
alert_play = False

def is_axon_device(address, name, rssi):
    """Check if device is Axon using MAC OUI + name patterns"""
    if not address:
        return False
    
    addr_upper = address.upper().replace("-", ":").replace("_", ":")
    prefix = addr_upper[:8].replace(":", "") if len(addr_upper) >= 8 else ""
    
    # Check MAC OUI
    for oui in TARGET_OUIS:
        if prefix.startswith(oui.replace(":", "")):
            return True
    
    # Check device name patterns
    if name:
        name_upper = name.upper()
        for pattern in AXON_PATTERNS:
            if pattern in name_upper:
                return True
    
    return False

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
            
            if is_axon_device(address, name, rssi):
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
            # Start BLE scan
            scan_proc = subprocess.Popen(
                ["hcitool", "-i", "hci0", "lescan", "--duplicates"],
                stdout=subprocess.DEVNULL, 
                stderr=subprocess.DEVNULL
            )
            
            time.sleep(10)  # Scan duration
            
            # Stop scan
            scan_proc.terminate()
            scan_proc.wait()
            
            # Try to parse from btmon
            try:
                result = subprocess.run(
                    ["timeout", "3", "btmon", "--raw"],
                    capture_output=True, text=True
                )
                # Parse btmon output for Axon devices
                for line in result.stdout.split('\n'):
                    # Simple parsing - look for Axon OUI in MAC
                    if '00:25:DF' in line.upper() or 'FC:A9:E8' in line.upper():
                        # Extract address
                        parts = line.split()
                        for p in parts:
                            if len(p.replace(':', '')) == 12:  # MAC
                                addr = p.upper()
                                if addr not in detected:
                                    detected.add(addr)
                                    log_detection(addr, "Unknown (hcitool)", 0)
            except:
                pass
                
        except Exception as e:
            print(f"Error: {e}")
        
        time.sleep(2)

def main():
    global alert_play
    
    print("=" * 50)
    print(" BlueMeanie - BLE Device Scanner")
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