#!/usr/bin/env bash
#
# Marks connected Android test devices/emulators as Firebase Analytics "developer traffic".
#
# With debug mode on, the device's events carry debug_mode=1: they show up in Firebase DebugView
# and are dropped from GA4 reports by the "Developer traffic" data filter (Admin → Data filters).
# This keeps the team's test devices out of the country and user numbers.
#
# The property is cleared when the device restarts, so run this again after a reboot.
#
# Usage:
#   scripts/firebase-debug-devices.sh [on|off|status] [-s SERIAL]
#
#   on      (default) turn debug mode on for every connected device
#   off     turn debug mode off
#   status  show the current value on each device
#   -s      only act on the device with this serial (see `adb devices`)

set -euo pipefail

PACKAGE="ltd.grunt.brainwallet"
PROP="debug.firebase.analytics.app"

action="on"
serial=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        on|off|status) action="$1" ;;
        -s) serial="${2:?-s needs a device serial}"; shift ;;
        -h|--help) sed -n '2,18p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) echo "Unknown argument: $1 (try --help)" >&2; exit 1 ;;
    esac
    shift
done

if ! command -v adb >/dev/null 2>&1; then
    echo "adb not found. Add Android SDK platform-tools to your PATH." >&2
    exit 1
fi

if [[ -n "$serial" ]]; then
    devices="$serial"
else
    devices="$(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')"
fi

if [[ -z "$devices" ]]; then
    echo "No authorised devices connected (check \`adb devices\`)." >&2
    exit 1
fi

for device in $devices; do
    case "$action" in
        on)  adb -s "$device" shell setprop "$PROP" "$PACKAGE" ;;
        off) adb -s "$device" shell setprop "$PROP" .none. ;;
    esac
    value="$(adb -s "$device" shell getprop "$PROP" | tr -d '\r')"
    if [[ "$value" == "$PACKAGE" ]]; then
        echo "$device: debug mode ON  (developer traffic)"
    else
        echo "$device: debug mode OFF (${value:-unset})"
    fi
done

if [[ "$action" == "on" ]]; then
    echo "Force-stop and reopen the app on each device for it to take effect."
fi
