#!/usr/bin/env python3
"""Generate release metadata from the APK that will actually be distributed."""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path


def apk_metadata(apk: Path, aapt: str) -> dict:
    badging = subprocess.check_output([aapt, 'dump', 'badging', str(apk)], text=True)
    package = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging)
    sdk = re.search(r"sdkVersion:'(\d+)'", badging)
    if not package or not sdk:
        raise ValueError('Could not read package metadata')
    if package[1] != 'org.zotero.android.debug':
        raise ValueError('Only the development package may be published here')
    return dict(applicationId=package[1], versionCode=int(package[2]), versionName=package[3], minSdk=int(sdk[1]))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--apk', type=Path, required=True)
    parser.add_argument('--aapt', required=True)
    parser.add_argument('--tag', required=True)
    parser.add_argument('--notes', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    metadata = apk_metadata(args.apk, args.aapt)
    if args.tag != f"dev-v{metadata['versionName']}":
        raise ValueError('Tag and APK version do not match')
    sha = hashlib.sha256()
    with args.apk.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            sha.update(chunk)
    metadata.update(channel='devDebug', sizeBytes=args.apk.stat().st_size,
                    sha256=sha.hexdigest(), releaseNotes=args.notes.read_text(),
                    apkUrl=f'https://github.com/nicholashyang/zotero-android/releases/download/{args.tag}/Zotero-dev-debug.apk')
    args.output.write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n')


if __name__ == '__main__':
    main()
