# Android Starter Kit

A compilable skeleton for a multi-module Android app — Clean layers · MVI · Compose · Koin · Ktor ·
DataStore — extracted from a shipped production app and tightened in five places it paid for.
`CLAUDE.md` at the root is the architecture blueprint an AI agent (Claude Code, Codex, Cursor…) reads
on every session; this README is for the human copying the kit.

## What is in the box

| Path | What it is |
|---|---|
| `CLAUDE.md` | The blueprint: layering rules, convention plugins, MVI contract, feature recipe, testing conventions, release flow. **Read by the agent automatically.** |
| `build-logic/` | The four convention plugins (`app.jvm.library`, `app.android.library`, `app.android.library.compose`, `app.android.application`). Every module's build file is its plugin id + namespace + own deps. |
| `gradle.properties` | `app.applicationId`, `app.versionName`, `app.versionCode` — the only place the version lives. |
| `core/model` | `Result<T>` (Success / Error; Loading for streams only) + `SessionState` |
| `core/common` | `AppException`, `AppCodes`, string/format extensions |
| `core/network` | Ktor `HttpClientFactory`, `ApiResponse`/`ApiError`, `apiCall` / `apiCallNoContent` (suspend) + `apiCallFlow` / `saveFile` (streams), `ConnectivityChecker`. **No endpoints** — a feature's API + DTOs live in its `data` module. |
| `core/datastore` | `AppDataStore` interface + impl, `SessionGuard`, retry helpers — with their JVM tests |
| `core/designsystem` | Material 3 theme (`AppTheme`), extended colours, fonts, launcher icons, `ProvideAppConfiguration`, `Int.sdp`/`Int.ssp`, and **only the strings the shared components use** |
| `core/ui` | `BaseMviViewModel` + `MviContract`, and the shared component library (buttons, cards, dialogs, text fields, loading, top bar) — with its Lottie keypath tests |
| `core/testing` | `MainDispatcherRule`, pinned Robolectric SDK |
| `app` | `AppApplication` (Koin), thin `MainActivity`, `AppModules`, typed `Screen` routes, `AppNavHost` with a placeholder Home, and **`AppModulesTest`** (`Module.verify()` — every Koin definition proven resolvable at test time) |
| `.github/workflows/ci.yml` | test → assembleDebug → lint |
| `rename-package.sh` / `.ps1` | Renames the package root and moves the directories |

Not included on purpose (they are app-specific, and the blueprint tells the agent how to build them):
`core/database` (Room), `domain/*`, `data/*`, `presentation/*`, and the shell (drawer/top bar).

## Start a new project

```bash
cp -r AndroidStarterKit MyApp && cd MyApp
./rename-package.sh com.company.myapp "My App"     # Windows: .\rename-package.ps1 -NewPackage com.company.myapp -AppName "My App"
```

Then:

1. `settings.gradle.kts` → `rootProject.name`. `gradle.properties` → the `app.*` keys are already renamed by the script.
2. `core/network/build.gradle.kts` → the three `BASE_URL`s. If staging is plain HTTP, add its host to
   `app/src/debug/res/xml/network_security_config.xml` **and** `app/src/staging/...` — never to `main`.
3. `core/designsystem` → replace `res/drawable/logo*.xml` and `res/mipmap-*` with the app's own. Its
   `strings.xml` is already down to the shared strings; each feature brings its own.
4. `local.properties` → `signing.*` keys (see `app/build.gradle.kts`) before the first release build.
5. Open in Android Studio, sync, run. The app shows a placeholder Home screen.

## First message to the agent

```
Read CLAUDE.md. This project is scaffolded from it: the core modules and build-logic exist, no features yet.
Build the first feature vertical for "<feature>" following §5 and the checklist in §12.
Backend contract: <paste endpoints + JSON shapes>. Ask before deviating from any rule.
```

## Verify the kit still builds

```bash
./gradlew test assembleDebug
```

Every test in the kit runs on the JVM (no device). On a Windows machine behind a corporate proxy
the convention plugins already point the test JVM at the `Windows-ROOT` trust store so Robolectric
can download its platform jar.
