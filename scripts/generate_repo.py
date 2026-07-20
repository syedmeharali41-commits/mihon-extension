#!/usr/bin/env python3
import os
import json
import hashlib
import shutil

def get_file_sha256(filepath):
    sha256_hash = hashlib.sha256()
    with open(filepath, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    dist_dir = os.path.join(root_dir, "dist")
    
    # Create dist folders
    dist_repo_dir = os.path.join(dist_dir, "repo")
    dist_apk_dir = os.path.join(dist_repo_dir, "apk")
    os.makedirs(dist_apk_dir, exist_ok=True)
    
    # Copy source apks if exist
    src_apk_dir = os.path.join(root_dir, "repo", "apk")
    if os.path.exists(src_apk_dir):
        for f in os.listdir(src_apk_dir):
            if f.endswith(".apk"):
                shutil.copy2(os.path.join(src_apk_dir, f), os.path.join(dist_apk_dir, f))
                
    # Copy index.html and _headers to dist
    for fname in ["index.html", "_headers"]:
        src_file = os.path.join(root_dir, fname)
        if os.path.exists(src_file):
            shutil.copy2(src_file, os.path.join(dist_dir, fname))

    extensions = []
    
    ybx_apk_name = "tachiyomi-all.ybxmanga-v1.0.0.apk"
    ybx_apk_path = os.path.join(dist_apk_dir, ybx_apk_name)
    
    size = os.path.getsize(ybx_apk_path) if os.path.exists(ybx_apk_path) else 1024000
    sha256 = get_file_sha256(ybx_apk_path) if os.path.exists(ybx_apk_path) else "0000000000000000000000000000000000000000000000000000000000000000"
    
    extensions.append({
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
    })
    
    # Write dist/index.min.json & dist/repo/index.min.json
    with open(os.path.join(dist_dir, "index.min.json"), "w", encoding="utf-8") as f:
        json.dump(extensions, f, indent=2)
        
    with open(os.path.join(dist_repo_dir, "index.min.json"), "w", encoding="utf-8") as f:
        json.dump(extensions, f, indent=2)
        
    print(f"Dist folder prepared at {dist_dir} with {len(extensions)} extension(s).")

if __name__ == "__main__":
    main()
