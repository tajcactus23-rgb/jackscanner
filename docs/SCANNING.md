# BLE Scanning Guide

## Understanding BLE Scanning

### What is BLE?

BLE (Bluetooth Low Energy) is a wireless personal area network technology designed for short-range communication. Unlike classic Bluetooth, BLE consumes minimal power and is ideal for constant advertising by devices like body cameras.

### OUI (Organizationally Unique Identifier)

The first 3 octets (6 characters) of a MAC address identify the manufacturer:

```
MAC: 00:25:DF:A1:B2:C3
     └──────┘ └───────┘
        OUI      NIC
     (Axon)  (Device ID)
```

### Target OUI: 00:25:DF

This OUI is registered to **Axon Enterprise Inc.** (formerly Taser International).

## Scanning Methods

### Method 1: hcitool (Root Required)

```bash
# Continuous scan
hcitool -i hci0 lescan --duplicates

# One-shot scan
hcitool -i hci0 lecan
```

**Pros:** Most reliable, lower power
**Cons:** Requires root

### Method 2: bleak (No Root)

```python
from bleak import BleakScanner

async def scan():
    devices = await BleakScanner.discover()
    for dev in devices:
        print(f"{dev.address} {dev.name}")
```

**Pros:** No root needed
**Cons:** May miss devices, higher battery use

### Method 3: Android API (Native)

BluetoothLeScanner in Android API 31+

## Signal Interpretation

### RSSI Values

| RSSI | Interpretation |
|-----|----------------|
| > -50 | Adjacent |
| -50 to -60 | Very close |
| -60 to -70 | Close |
| -70 to -80 | Medium range |
| < -80 | Far/In edge of range |

### Tips

- **Wall/Obstacles** reduce signal
- **Body blocking** can cause signal drops
- **Multiple detections** confirm presence

## Troubleshooting

### "No Bluetooth adapter found"

- Enable Bluetooth
- Grant permissions
- Check hardware

### "Scan finds nothing"

- Device not advertising
- Random MAC (Android 10+)
- Signal too weak
- Out of range

### "hcitool: Permission denied"

- Need root access
- Use Magisk to grant Termux root

## Detection Log Format

```
[2024-05-10 14:32:10] 00:25:DF:A1:B2:C3 | AXON Flex 2 | -72 dBm
    │                    │              │           │
    │                    │              │           └── Signal strength
    │                    │              └────────── Device name
    │                    └────────────────────── MAC address
    └────────────────────────────────────── Timestamp
```

## Legal Considerations

- Check local laws before scanning
- Some jurisdictions restrict radio detection
- Use only for personal safety/education
- Do not store data for unlawful purposes