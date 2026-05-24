import requests
import sys

# Read the README.md as a sample file to upload (APK needs to be built first)
file_path = "README.md"
try:
    with open(file_path, 'rb') as f:
        files = {'file': (file_path, f)}
        response = requests.post('https://file.io', files=files, timeout=30)
        result = response.json()
        if result.get('success'):
            print(f"✅ Upload successful!")
            print(f"Link: {result.get('link')}")
            print(f"Expires: {result.get('expires')}")
        else:
            print(f"❌ Upload failed: {result}")
except Exception as e:
    print(f"Error: {e}")
