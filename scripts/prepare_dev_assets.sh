#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
if git submodule status --recursive | grep -q '^-'; then
    echo "Initialize submodules first: git submodule update --init --recursive" >&2
    exit 1
fi

for bundle in translators translation pdf-worker citation_proc csl_locales styles; do
    python3 "scripts/bundle_${bundle}.py"
done
bash scripts/bundle_utilities.sh
bash scripts/bundle_reader.sh
