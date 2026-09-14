---
name: android-starter
description: Scaffolds a new Android project from Mahmoud's starter kit (multi-module · Clean layers · MVI · Compose · Koin · Ktor · DataStore, with build-logic convention plugins) and makes the architecture blueprint the project's CLAUDE.md, or applies that same blueprint to an existing Android project being revamped. Use this whenever the user starts a new Android app, says "new project", "scaffold", "starter kit", "بروجيكت جديد", "ابدأ مشروع", asks to set up modules/architecture for an Android app, or wants an existing Android app restructured to the GourmetGo-style architecture — even if they never say "skill" or "kit". Do not use it for adding a feature inside a project that already carries the blueprint's CLAUDE.md; that file already says how.
---

# Android starter

Two modes. Pick by what is in the current directory.

| Directory | Mode |
|---|---|
| Empty, or only what Android Studio's "New Project" wizard generated | **Scaffold** — copy the kit in, rename, build |
| A real app with its own code | **Revamp** — no copying; read the blueprint and plan the restructuring |

The kit ships inside this skill, at `kit/` next to this file (`~/.claude/skills/android-starter/kit`);
`ANDROID_STARTER_KIT` overrides the location. If `kit/` is missing, the skill was installed without
it — say so and point at the repo (`git clone https://github.com/MhmoudOthman/android-starter.git
~/.claude/skills/android-starter`); do not reconstruct the kit from memory.

## Scaffold

### 1. Get the two answers the script cannot guess

- **Package root** (`com.company.app`) and **display name** ("My App"). Ask if the user has not
  said; do not invent a company name. Everything else (root project name from the directory,
  namespaces, `applicationId`) is derived from these.
- **Base URLs** are optional. Take them if given; otherwise the kit's `example.com` placeholders
  stay and are listed at the end so nobody forgets.

### 2. Run the script — never hand-edit namespaces

```bash
bash ~/.claude/skills/android-starter/scripts/scaffold.sh --package com.company.app --name "My App" \
  [--debug-url https://staging.host/api/] [--release-url https://api.host/api/] [--staging-url https://staging.host/api/]
```

It refuses a non-empty directory. The common case is a project Android Studio just generated (an
`app/` module, `build.gradle.kts`, `gradle/`): tell the user those files are about to be replaced
by the kit — `.git`, `.idea`, `.claude` and `local.properties` are kept — and re-run with `--force`
only after they agree. `local.properties` matters: it carries `sdk.dir`, which the build needs.

The script copies the kit, sets `rootProject.name`, runs the kit's `rename-package.sh` (moves every
`com/starter/app` directory and rewrites every reference), writes a short project README, and
removes the kit's own tooling. Read its final summary rather than re-deriving it.

### 3. Prove it builds before saying it is done

```bash
./gradlew test assembleDebug
```

Green means: the four convention plugins configured every module, the kit's 28 JVM tests passed
(one is skipped on Windows by design), and `AppModulesTest` proved the Koin graph resolves. If the
build fails on `SDK location not found`, the directory has no `local.properties` — write one with
`sdk.dir=D:/Android/Sdk` (forward slashes; a Windows path with single backslashes is *unescaped*
by the properties parser into `D:AndroidSdk` and fails with "filename, directory name, or volume
label syntax is incorrect") rather than editing build files. On this machine the shell
needs `JAVA_HOME`, `ANDROID_HOME` and the proxy trust-store settings from the project memory
(`gradle-cli-workarounds`).

### 4. Hand over

Report, in this order: what was scaffolded (package, name, path), the build/test result, and the
things the user still has to do themselves — the three `BASE_URL`s if they were not given, the
logos/launcher icons in `core/designsystem`, `signing.*` in `local.properties` before any release
build, and cleartext hosts in `app/src/debug|staging/res/xml/network_security_config.xml` if
staging is plain HTTP (never in `main`).

Then stop. The project now carries `CLAUDE.md`; the next request ("build the login feature") is
answered by following that file's §5 and §12, not by this skill.

## Revamp

Nothing is copied — an existing app has code the kit would overwrite. Instead:

1. Read `kit/CLAUDE.md` (next to this file) in full. It is the target.
2. Map the current project against §2 (module map) and §0 (the 13 rules): which layers exist,
   what depends on what, where DI is aggregated, how repos return results, where strings live.
3. Write the plan as ordered, independently mergeable steps — build-logic first (it changes every
   module's build file and nothing else), then one feature vertical end to end as the template,
   then the rest. Put the plan in front of the user before changing anything; a revamp that starts
   with a rename across 200 files is not something to discover in a diff.
4. Copy the blueprint in as the project's `CLAUDE.md` only once the user agrees to the target,
   so the agents that follow work from the same rules.

Two things worth carrying over from the kit even in a revamp, because they are self-contained:
`build-logic/` (drop it in, then delete the repeated `android {}` blocks), and
`app/src/test/.../AppModulesTest.kt` (a Koin `verify()` test that fails on a missing definition —
proven, not assumed).

## What the kit is, in one breath

`core/model` (`Result`, `SessionState`) · `core/common` (`AppException`, `AppCodes`) ·
`core/network` (Ktor client, `apiCall`/`apiCallNoContent` suspend helpers, no endpoints) ·
`core/datastore` (`AppDataStore` + `SessionGuard`, tested) · `core/designsystem` (`AppTheme`,
shared strings only, `Int.sdp`) · `core/ui` (`BaseMviViewModel`, component library) ·
`core/testing` · a thin `:app` · `build-logic/` with `app.jvm.library`, `app.android.library`,
`app.android.library.compose`, `app.android.application`. Features are added as
`domain:<f>` / `data:<f>` / `presentation:<f>` per the blueprint — the kit ships none.
