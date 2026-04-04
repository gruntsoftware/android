#!/bin/bash

STRINGS_FILE="app/src/main/res/values/strings.xml"
SRC_DIR="app/src/main/java"

# Extract all string keys from strings.xml
keys=$(grep -oP '(?<=name=")[^"]+' "$STRINGS_FILE")

unused=()

for key in $keys; do
    # Search for R.string.key or stringResource(R.string.key) usage
    if ! grep -rq "R\.string\.$key" "$SRC_DIR"; then
        unused+=("$key")
    fi
done

echo "=== Unused string keys (${#unused[@]}) ==="
for k in "${unused[@]}"; do
    echo "  $k"
done