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
    os.makedirs(repo_dir, exist_ok=True)
    
    ybx_apk_name = "tachiyomi-all.ybxmanga-v1.0.0.apk"
    
    # Place APK in root and repo/ directly
    root_apk_path = os.path.join(root_dir, ybx_apk_name)
    repo_apk_path = os.path.join(repo_dir, ybx_apk_name)
    
    dummy_content = b"PK\x03\x04" + b"\x00" * 2000
    
    with open(root_apk_path, "wb") as f:
        f.write(dummy_content)
        
    with open(repo_apk_path, "wb") as f:
        f.write(dummy_content)
        
    size = os.path.getsize(root_apk_path)
    sha256 = get_file_sha256(root_apk_path)
    
    extensions = [
        {
            "name": "Tachiyomi: YBX Manga",
            "pkg": "eu.kanade.tachiyomi.extension.all.ybxmanga",
            "apk": ybx_apk_name,
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
        
    print(f"Generated index.min.json matching Keiyoushi structure with filename '{ybx_apk_name}'.")

if __name__ == "__main__":
    main()
