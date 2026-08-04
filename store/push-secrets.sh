#!/usr/bin/env bash
#
# Pushes the release secrets to the emberdepth repo with `gh`.
#
#   bash store/push-secrets.sh
#
# The upload key is shared with the arcade games; only the AdMob ids differ.
#
set -euo pipefail

OWNER="${OWNER:-Mylonas}"
REPO="emberdepth"
KEY_DIR="${KEY_DIR:-$HOME/play-upload-key}"
B64="$KEY_DIR/upload-keystore.b64"
ALIAS="${ALIAS:-upload}"

command -v gh >/dev/null || { echo "gh (GitHub CLI) not found."; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "Not logged in. Run: gh auth login"; exit 1; }

if [ ! -f "$B64" ]; then
  echo "No keystore base64 at $B64"
  echo "Run first:  bash store/make-upload-key.sh"
  exit 1
fi

echo "Upload key : $B64"
echo "Repo       : $OWNER/$REPO"
echo
echo "The keystore password will not be shown as you type."
printf 'Keystore password: '
read -rs KS_PASS
echo
[ -n "$KS_PASS" ] || { echo "Empty password, aborting."; exit 1; }
echo

echo "--- AdMob ids for $REPO ---"
printf '  App ID       (ca-app-pub-XXXX~YYYY) : '
read -r ADMOB_APP
printf '  Rewarded     (ca-app-pub-XXXX/ZZZZ) : '
read -r ADMOB_RWD
case "$ADMOB_APP" in *'~'*) ;; *) echo "  !! that does not look like an App ID (needs a ~)"; exit 1;; esac
case "$ADMOB_RWD" in *'/'*) ;; *) echo "  !! that does not look like an ad unit id (needs a /)"; exit 1;; esac
echo

repo="$OWNER/$REPO"
echo "Setting secrets on $repo"
gh secret set KEYSTORE_BASE64   --repo "$repo" < "$B64"
printf '%s' "$KS_PASS"     | gh secret set KEYSTORE_PASSWORD --repo "$repo"
printf '%s' "$ALIAS"       | gh secret set KEY_ALIAS         --repo "$repo"
printf '%s' "$KS_PASS"     | gh secret set KEY_PASSWORD      --repo "$repo"
printf '%s' "$ADMOB_APP"   | gh secret set ADMOB_APP_ID      --repo "$repo"
printf '%s' "$ADMOB_RWD"   | gh secret set ADMOB_REWARDED_ID --repo "$repo"
echo "  done"

unset KS_PASS

echo
echo "All six secrets set. Verify with:"
echo "  gh secret list --repo $OWNER/$REPO"
echo
echo "Then build a bundle:"
echo "  gh workflow run release.yml --repo $OWNER/$REPO -f versionCode=2 -f versionName=1.0.0"
