#!/usr/bin/env bash
# Renames the kit's package root and moves the source directories to match.
#
#   ./rename-package.sh com.company.myapp "My App"
#
# Arg 1: new package root (required). Arg 2: display name for app_name (optional).
# Idempotent: run it again with a different package and it renames from whatever is current.
set -euo pipefail

NEW_PKG="${1:?usage: $0 <new.package.root> [\"App Name\"]}"
APP_NAME="${2:-}"
cd "$(dirname "$0")"

OLD_PKG=$(grep -E "^app.applicationId=" gradle.properties | cut -d= -f2- | tr -d "[:space:]")
[ -n "$OLD_PKG" ] || { echo "could not read app.applicationId from gradle.properties" >&2; exit 1; }
[ "$OLD_PKG" != "$NEW_PKG" ] || { echo "already $NEW_PKG"; exit 0; }

OLD_PATH=${OLD_PKG//./\/}
NEW_PATH=${NEW_PKG//./\/}

echo "$OLD_PKG -> $NEW_PKG"

# 1. Text replacement in every source/build/config file (sed keeps UTF-8 bytes untouched).
grep -rlF "$OLD_PKG" \
  --include='*.kt' --include='*.kts' --include='*.xml' --include='*.pro' --include='*.md' \
  --include='*.properties' --include='*.yml' --include='*.json' \
  --exclude-dir=build --exclude-dir=.gradle --exclude-dir=.git --exclude-dir=.idea . \
  | while read -r f; do sed -i "s/$OLD_PKG/$NEW_PKG/g" "$f"; done

# 2. Move every com/old/pkg directory to com/new/pkg, deepest first.
find . -type d -path "*/$OLD_PATH" -not -path "*/build/*" -not -path "./.git/*" \
  | awk '{ print length, $0 }' | sort -rn | cut -d' ' -f2- \
  | while read -r dir; do
      base="${dir%/$OLD_PATH}"
      mkdir -p "$base/$NEW_PATH"
      cp -r "$dir"/. "$base/$NEW_PATH"/
      rm -rf "$dir"
      # prune now-empty parents of the old path
      p="$(dirname "$dir")"
      while [ "$p" != "$base" ] && [ -d "$p" ] && [ -z "$(ls -A "$p")" ]; do rmdir "$p"; p="$(dirname "$p")"; done
    done

# 3. Optional display name.
if [ -n "$APP_NAME" ]; then
  sed -i "s|<string name=\"app_name\" translatable=\"false\">[^<]*</string>|<string name=\"app_name\" translatable=\"false\">$APP_NAME</string>|" \
    core/designsystem/src/main/res/values/strings.xml
fi

echo "done. Remaining references to the old package:"
grep -rnF "$OLD_PKG" --exclude-dir=build --exclude-dir=.gradle --exclude-dir=.git . || echo "  none"
