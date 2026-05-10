#!/data/data/com.termlux/files/usr/bin/bash
# Termux Setup Script for Axon/Python Scanners
# Run: bash termux_setup.sh

echo "Installing prerequisites..."

# Install BLE dependencies
pkg update -y
pkg install -y python python3-pip bluetooth bluez-utils grep

# Install Python BLE library
pip install bleak

echo "Setup complete!"
echo ""
echo "Run scanner:"
echo "  python jackscanner.py    # Discrete mode"
echo "  python axonbleeder.py # Direct mode"
echo ""
echo "Note: Root may be required for hcitool"
echo "If not rooted, use bleak (non-root mode)"