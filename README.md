# android-starter

A Claude Code skill that scaffolds a new Android project from a starter kit — multi-module · Clean
layers · MVI · Compose · Koin · Ktor · DataStore, with `build-logic` convention plugins — and makes
the architecture blueprint the project's `CLAUDE.md`. The same skill plans a revamp of an existing app
against that blueprint.

```
SKILL.md            what the agent reads when the skill triggers (/android-starter)
scripts/scaffold.sh copies the kit into the current directory, renames the package, sets URLs
kit/                the starter kit itself — a compilable project (CLAUDE.md = the blueprint)
```

## Install (any machine)

```bash
git clone https://github.com/MhmoudOthman/android-starter.git ~/.claude/skills/android-starter
```

Windows (PowerShell): `git clone https://github.com/MhmoudOthman/android-starter.git "$HOME\.claude\skills\android-starter"`

That is the whole install: Claude Code discovers personal skills under `~/.claude/skills/<name>/SKILL.md`
on the next session. Update with `git pull` in that directory.

To keep a working copy elsewhere (to open the kit in Android Studio) and still have the skill see it,
make the skills entry a junction/symlink to the clone instead of a second clone:

```powershell
cmd /c mklink /J "$HOME\.claude\skills\android-starter" "D:\AndroidStudioProjects\android-starter"
```

## Use

In an empty directory (or one Android Studio's wizard just generated), start Claude Code and say
`/android-starter` — or just "start a new Android project called X with package com.y.z". The skill
asks for the two things it cannot guess (package root, display name), runs `scripts/scaffold.sh`, and
proves the result with `./gradlew test assembleDebug`.

The script can also be run by hand:

```bash
bash ~/.claude/skills/android-starter/scripts/scaffold.sh --package com.company.myapp --name "My App" \
  --debug-url https://staging.host/api/ --release-url https://api.host/api/ --staging-url https://staging.host/api/
```

## The kit

See [`kit/README.md`](kit/README.md) for what is inside and [`kit/CLAUDE.md`](kit/CLAUDE.md) for the
rules. In one breath: `core/model` · `core/common` · `core/network` (client only) · `core/datastore` ·
`core/designsystem` · `core/ui` · `core/testing` · a thin `:app` · `build-logic/` with four convention
plugins. Features are added per the blueprint as `domain:<f>` / `data:<f>` / `presentation:<f>`; the kit
ships none.

## CI

Every push scaffolds a throwaway project from the kit on Ubuntu and runs `./gradlew test assembleDebug`
on it — the kit is only "working" if a fresh scaffold builds somewhere other than the laptop it was
written on.
