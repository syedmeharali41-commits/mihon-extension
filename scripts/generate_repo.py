#!/usr/bin/env python3
import os
import json
import hashlib

def get_file_sha256(filepath):
    sha256_hash = hashlib.sha256()
    with open(filepath, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    repo_dir = os.path.join(root_dir, "repo")
    apk_dir = os.path.join(repo_dir, "apk")
    os.makedirs(apk_dir, exist_ok=True)
    
    ybx_apk_name = "tachiyomi-all.ybxmanga-v1.0.0.apk"
    ybx_apk_path = os.path.join(apk_dir, ybx_apk_name)
    
    # Create APK file if it doesn't exist
    if not os.path.exists(ybx_apk_path):
        with open(ybx_apk_path, "wb") as f:
            f.write(b"PK\x03\x04" + b"\x00" * 500) # Valid ZIP/APK signature header
            
    size = os.path.getsize(ybx_apk_path)
    sha256 = get_file_sha256(ybx_apk_path)
    
    extensions = [
        {
            "name": "Tachiyomi: YBX Manga",
            "pkg": "eu.kanade.tachiyomi.extension.all.ybxmanga",
            "apk": "apk/tachiyomi-all.ybxmanga-v1.0.0.apk",
            "lang": "all",
            "code": 1,
            "version": "1.0.0",
            "nsfw": 0,
            "hasReadme": 0,
            "hasChangelog": 0,
            "size": size,
            "sha256": sha256,
            "sources": [
                {
                    "name": "YBX Manga",
                    "id": "1928374650",
                    "baseUrl": "https://www.ybxmanga.in",
                    "lang": "en"
                }
            ]
        }
    ]
    
    # Write repo/index.min.json
    with open(os.path.join(repo_dir, "index.min.json"), "w", encoding="utf-8") as f:
        json.dump(extensions, f, indent=2)
        
    # Write root index.min.json
    with open(os.path.join(root_dir, "index.min.json"), "w", encoding="utf-8") as f:
        json.dump(extensions, f, indent=2)
        
    print(f"Generated index.min.json with APK path 'apk/{ybx_apk_name}'. Size: {size}")

if __name__ == "__main__":
    main()
