#!/usr/bin/env bash
# Scaffolds a new Android project from the starter kit into the current directory.
#
#   scaffold.sh --package com.company.myapp --name "My App" \
#               [--debug-url URL] [--release-url URL] [--staging-url URL] \
#               [--target DIR] [--kit DIR] [--force]
#
# What it does, in order:
#   1. Refuses to touch a non-empty directory unless --force (then keeps .git, .idea, .claude and
#      local.properties — everything Android Studio generated is replaced by the kit).
#   2. Copies the kit without build outputs.
#   3. Sets rootProject.name from the directory name.
#   4. Runs the kit's rename-package.sh (package root + app_name), then removes the kit's own tooling
#      (rename scripts, kit README) so the project starts clean.
#   5. Optionally rewrites the three BASE_URLs in core/network/build.gradle.kts.
#
# Deterministic on purpose: the agent that calls this should not be hand-editing namespaces.
set -euo pipefail

# The kit ships inside the skill (../kit). ANDROID_STARTER_KIT overrides it.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KIT="${ANDROID_STARTER_KIT:-$SCRIPT_DIR/../kit}"
TARGET="$(pwd)"
PKG=""; APP_NAME=""; FORCE=0
DEBUG_URL=""; RELEASE_URL=""; STAGING_URL=""

while [ $# -gt 0 ]; do
  case "$1" in
    --package) PKG="$2"; shift 2 ;;
    --name) APP_NAME="$2"; shift 2 ;;
    --target) TARGET="$2"; shift 2 ;;
    --kit) KIT="$2"; shift 2 ;;
    --debug-url) DEBUG_URL="$2"; shift 2 ;;
    --release-url) RELEASE_URL="$2"; shift 2 ;;
    --staging-url) STAGING_URL="$2"; shift 2 ;;
    --force) FORCE=1; shift ;;
    *) echo "unknown argument: $1" >&2; exit 1 ;;
  esac
done

[ -n "$PKG" ] || { echo "--package is required (e.g. com.company.myapp)" >&2; exit 1; }
[[ "$PKG" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] || { echo "not a valid package root: $PKG" >&2; exit 1; }
[ -n "$APP_NAME" ] || { echo "--name is required (the display name, e.g. \"My App\")" >&2; exit 1; }
[ -f "$KIT/CLAUDE.md" ] && [ -f "$KIT/rename-package.sh" ] || {
  echo "starter kit not found at $KIT (set ANDROID_STARTER_KIT or pass --kit)" >&2; exit 2; }

mkdir -p "$TARGET"
cd "$TARGET"

# ---- 1. non-empty guard -------------------------------------------------------------------------
KEEP='.git .idea .claude local.properties'
is_kept() { for k in $KEEP; do [ "$1" = "$k" ] && return 0; done; return 1; }
existing=""
for entry in .[!.]* *; do
  [ -e "$entry" ] || continue
  is_kept "$entry" && continue
  existing="$existing $entry"
done
if [ -n "$existing" ] && [ "$FORCE" != 1 ]; then
  echo "target is not empty:$existing" >&2
  echo "re-run with --force to replace them (keeps: $KEEP)" >&2
  exit 3
fi
if [ -n "$existing" ]; then
  for entry in $existing; do rm -rf "$entry"; done
  echo "replaced:$existing"
fi

# ---- 2. copy the kit without build outputs -------------------------------------------------------
(cd "$KIT" && tar --exclude='./.git' --exclude='./.idea' --exclude='./local.properties' \
    --exclude='./.gradle' --exclude='./.kotlin' --exclude='./build' --exclude='*/build' \
    --exclude='./build-logic/build' --exclude='./build-logic/.gradle' -cf - .) | tar -xf -
chmod +x gradlew rename-package.sh 2>/dev/null || true

# ---- 3. root project name ------------------------------------------------------------------------
ROOT_NAME="$(basename "$TARGET")"
sed -i "s/^rootProject.name = .*/rootProject.name = \"$ROOT_NAME\"/" settings.gradle.kts

# ---- 4. package + app name, then drop the kit's own tooling --------------------------------------
bash ./rename-package.sh "$PKG" "$APP_NAME"
rm -f rename-package.sh rename-package.ps1
cat > README.md <<EOF
# $APP_NAME

Scaffolded from the Android starter kit. Architecture rules live in \`CLAUDE.md\` (the file every
AI agent reads); \`docs/ARCHITECTURE_BLUEPRINT.md\` is the same document for humans.

\`\`\`bash
./gradlew test assembleDebug
\`\`\`
EOF

# ---- 5. base URLs (optional) ---------------------------------------------------------------------
set_url() { # $1 = build type, $2 = url
  [ -n "$2" ] || return 0
  sed -i "/getByName(\"$1\")/,/}/ s|buildConfigField(\"String\", \"BASE_URL\", \"\\\\\"[^\"]*\\\\\"\")|buildConfigField(\"String\", \"BASE_URL\", \"\\\\\"$2\\\\\"\")|" core/network/build.gradle.kts
}
set_url debug "$DEBUG_URL"
set_url release "$RELEASE_URL"
set_url staging "$STAGING_URL"

echo
echo "scaffolded '$APP_NAME' ($PKG) into $TARGET"
echo "  rootProject.name = $ROOT_NAME"
grep -n "BASE_URL" core/network/build.gradle.kts | sed 's/^/  /'
echo "  next: ./gradlew test assembleDebug"
