#!/usr/bin/env bash
# Installs the debug APK on the running emulator, lets the idle RPG auto-battle,
# and captures screenshots, logcat and crash diagnostics.
#
#   play.sh <package>
set -uo pipefail

PKG="$1"
OUT="artifacts"
mkdir -p "$OUT/shots"

shot() { adb exec-out screencap -p > "$OUT/shots/$1.png"; echo "  shot $1"; }

echo "== waiting for the device to settle =="
adb wait-for-device
adb shell input keyevent 82 >/dev/null 2>&1 || true

adb shell settings put secure immersive_mode_confirmations confirmed

echo "== installing =="
adb install -r -t app/build/outputs/apk/debug/app-debug.apk

SIZE=$(adb shell wm size | tail -1 | tr -d '\r' | sed 's/.*: *//')
W=${SIZE%x*}
H=${SIZE#*x}
CX=$((W / 2))
echo "== screen ${W}x${H} =="

adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity"
sleep 8
shot 01-launch

echo "== letting auto-battle run =="
sleep 15
shot 02-battling

echo "== tapping UI elements =="
# tap near top-right where settings gear is
adb shell input tap $((W - 50)) 50
sleep 2
shot 03-settings

# dismiss settings by tapping elsewhere
adb shell input tap $CX $((H / 2))
sleep 2

# tap a hero in the tray (bottom of screen)
adb shell input tap $((W / 4)) $((H - 100))
sleep 3
shot 04-hero-panel

# dismiss
adb shell input tap $CX $((H / 4))
sleep 2

echo "== more idle time =="
sleep 20
shot 05-after-idle

echo "== collecting =="
adb logcat -d > "$OUT/logcat.txt"
adb shell dumpsys meminfo "$PKG" > "$OUT/meminfo.txt" 2>&1 || true

echo
echo "== crash check =="
FAIL=0
if grep -qE "FATAL EXCEPTION|AndroidRuntime: .*Exception" "$OUT/logcat.txt"; then
  echo "CRASH DETECTED:"
  grep -A 25 -E "FATAL EXCEPTION" "$OUT/logcat.txt" | head -60
  FAIL=1
fi
if grep -q "ANR in $PKG" "$OUT/logcat.txt"; then
  echo "ANR DETECTED IN THE GAME"
  grep -A 10 "ANR in $PKG" "$OUT/logcat.txt" | head -30
  FAIL=1
fi
if grep -q "ANR in " "$OUT/logcat.txt"; then
  echo "NOTE: system-wide ANR observed:"
  grep "ANR in " "$OUT/logcat.txt" | head -5
fi

PID=$(adb shell pidof "$PKG" | tr -d '\r')
if [ -z "$PID" ]; then
  echo "PROCESS IS GONE - the app died during the run"
  FAIL=1
else
  echo "process alive (pid $PID) after the full session"
fi

grep -iE "$PKG.*(error|failed)" "$OUT/logcat.txt" | head -10 || true

echo
echo "== AdMob =="
grep -iE "Ads|admob|Rewarded" "$OUT/logcat.txt" | grep -viE "^$" | head -25 | tee "$OUT/ads.txt"
if grep -q "Missing application ID" "$OUT/logcat.txt"; then
  echo "ADMOB APP ID MISSING - this crashes on launch"
  FAIL=1
fi

echo
echo "== Hilt =="
grep -iE "hilt|dagger|inject" "$OUT/logcat.txt" | head -10 || true

echo
echo "== Room =="
grep -iE "room|migration|database" "$OUT/logcat.txt" | head -10 || true

exit $FAIL
