# Android Architecture Blueprint — Multi-module · Clean layers · MVI · Compose · Koin

> **How to use this file.** Paste it into the first message of any AI agent (Claude, Codex, Cursor…)
> when starting a new Android project or revamping an old one, and say:
> *"Build/refactor the project following this blueprint exactly. Ask before deviating."*
> Everything below is a **rule** unless marked *(reference)*. `<app>` is the app's short name,
> `com.company.app` its package root, and `<feature>` any vertical (orders, auth, profile…).
> This blueprint was extracted from a shipped production app and then tightened
> in five places that app paid for (convention plugins, API placement, feature strings, one-shot
> results, DI verification). A compilable starter kit that already follows it exists; if you have
> it, start from the kit instead of an empty project.

---

## 0. The rules in one screen

1. **Four layers, one direction:** `presentation → domain ← data`, all standing on `core`. Presentation never sees `data`; `domain` never sees Android.
2. **A feature is a vertical of up to three modules:** `:domain:<feature>`, `:data:<feature>`, `:presentation:<feature>`. Not every feature needs all three (§3.5).
3. **Gradle config lives in `build-logic/` convention plugins, once.** A module's build file is its plugin id, its namespace and its own dependencies — nothing else. No root `subprojects { }` block, no `allprojects`.
4. **No central DI module.** Every module owns a Koin `module { }` in its own `di` package; `:app` aggregates them in one list, and **one test proves the whole graph resolves** (`Module.verify()`).
5. **Every ViewModel is MVI** on a shared `BaseMviViewModel<Intent, State, Effect>` with a required `SavedStateHandle`.
6. **Route/Screen split:** `XxxRoute` talks to the ViewModel; `XxxScreen` is stateless and takes `(state, onIntent)`.
7. **A one-shot call is `suspend fun x(): Result<T>`** — `Success` or `Error`, no `Loading`. The ViewModel owns `isLoading`. A `Flow<Result<T>>` with a `Loading` emission is only for genuine streams (a Room observation, a download's progress).
8. **`core:network` is the client, not the endpoints.** A feature's `XxxAPI` + `XxxAPIImpl` + DTOs live in `:data:<feature>`, beside the repo that uses them.
9. **A feature's strings live in its presentation module.** `core:designsystem` holds only what the shared components reference.
10. **One NavHost for the whole app**, typed `@Serializable` routes, nested *graphs* not nested hosts.
11. **Business rules live in use cases, not in screens or ViewModels** (a rule a screen decides is a rule a screen can be talked out of).
12. **Tests fake the repo and build the real use case.** ViewModel + Compose screen tests run on the JVM (Robolectric), never on a device.
13. **Secrets never enter a committed file**; environment-specific URLs are `buildConfigField`s per build type.

---

## 1. Stack *(reference — pin in `gradle/libs.versions.toml`)*

| Concern | Choice |
|---|---|
| Language / build | Kotlin 2.4 · AGP 9.2 · KSP · Gradle version catalog · `TYPESAFE_PROJECT_ACCESSORS` · `build-logic` included build with convention plugins |
| UI | Jetpack Compose (BOM) · Material 3 · Navigation Compose (typed routes) |
| DI | Koin (`koin-core`, `koin-android`, `koin-androidx-compose`; `koin-test` for `verify()`) |
| Network | Ktor client (OkHttp engine) + kotlinx.serialization JSON |
| Local storage | Room (KSP) · DataStore Preferences |
| Async | Coroutines + Flow |
| Testing | JUnit4 · `kotlinx-coroutines-test` · Robolectric · `compose-ui-test-junit4` |
| Sizing | Intuit sdp/ssp dimen tables read through the project's own cached `Int.sdp` / `Int.ssp` accessors |

---

## 2. Module map

```
<root>
├── build-logic/               Included build: the four convention plugins (§3.4)
├── app/                       Android application — entry point + wiring only
├── core/
│   ├── model/                 Kotlin JVM   shared domain types: Result<T>, SessionState
│   ├── common/                Kotlin JVM   AppException, AppCodes, String/Format extensions
│   ├── network/               Android lib  Ktor HttpClientFactory, ApiResponse/ApiError, apiCall helpers, ConnectivityChecker — NO endpoints, NO DTOs
│   ├── database/              Android lib  Room AppDatabase, entities, DAOs, exported schemas/ (one @Database, so tables are central)
│   ├── datastore/             Android lib  AppDataStore (interface) + Impl over DataStore Preferences, session guard
│   ├── designsystem/          Android lib  Theme, Color, Type, fonts, launcher icons, the SHARED strings only, sdp accessors
│   ├── ui/                    Android lib  MVI base classes + shared Compose components (buttons, cards, dialogs, text fields, loading)
│   └── testing/               Kotlin JVM   MainDispatcherRule + robolectric.properties (test-only helpers)
├── domain/
│   ├── common/                Kotlin JVM   cross-feature contracts: connectivity, location, preferences (interfaces + use cases)
│   └── <feature>/             Kotlin JVM   <Feature>Repo interface, models, use cases, domain<Feature>Module
├── data/
│   ├── common/                Android lib  impls of domain:common contracts
│   └── <feature>/             Android lib  <Feature>API + Impl + DTOs, <Feature>Mapping, <Feature>RepoImpl, data<Feature>Module
└── presentation/
    ├── shell/                 Android lib  app chrome (drawer/top bar) + ShellViewModel — NO navigation dependency
    └── <feature>/             Android lib  <Feature>Contract, ViewModel, Route/Screen, components, its own res/values/strings.xml, presentation<Feature>Module
```

### 2.1 Module types → convention plugin

| Type | Plugin id | Used for |
|---|---|---|
| **Kotlin JVM library** | `app.jvm.library` | `core:model`, `core:common`, `core:testing`, every `domain:*` |
| **Android library** | `app.android.library` | `core:network/database/datastore`, every `data:*` |
| **Android library + Compose** | `app.android.library.compose` | `core:ui`, `core:designsystem`, every `presentation:*` |
| **Android application** | `app.android.application` | `:app` only |

**`domain` modules are pure JVM on purpose.** That is what makes every business rule unit-testable
without Robolectric. If a domain module needs something Android-shaped (location, connectivity),
declare an **interface** in `domain:common` and implement it in `data:common`.

### 2.2 Allowed dependency directions

```
app ──────────► everything (only place that sees all modules)
presentation:X ► core:ui, core:common, core:designsystem, domain:X (+ domain:common when needed)
domain:X ──────► core:model (api), core:common, domain:common
data:X ────────► domain:X, core:network (client only), core:database, core:datastore, core:common, core:model
core:ui ───────► core:model (api), core:designsystem (api)
core:* ────────► other core:* only, never domain/data/presentation
```

Forbidden: `presentation → data`, `domain → data`, `domain → Android`, `core → feature`,
`presentation:shell → navigation`, `data:X → data:Y` (share through `domain` or `core`). Use
`api(...)` only when a type crosses the module's public signature.

### 2.3 Package layout inside a module

```
com.company.app.core.<name>            core modules
com.company.app.domain.<feature>/
    di/          domain<Feature>Module
    models/      <Thing>Model (plain data classes, no annotations)
    repo/        <Feature>Repo (interface)
    usecases/    <Verb><Thing>UseCase (one class, one `operator fun invoke`)
com.company.app.data.<feature>/
    api/         <Feature>API (interface) + <Feature>APIImpl (Ktor calls through apiCall)
    dto/         @Serializable DTOs, field names = the backend's JSON keys
    mapping/     <Feature>Mapping.kt — DTO ↔ Entity ↔ Model extension functions
    repo/        <Feature>RepoImpl
    di/          data<Feature>Module (binds the API and the repo)
com.company.app.presentation.<feature>/
    contract/    <Feature>Contract.kt — State, Intent, Effect in ONE file
    viewmodel/   <Feature>ViewModel
    screen/      <Feature>Screen.kt — holds BOTH <Feature>Route and <Feature>Screen
    component/   feature-local composables (cards, rows, sheets)
    di/          presentation<Feature>Module
    res/values/strings.xml (+ values-<lang>/)   this feature's strings
```

Tests mirror the same path under `src/test/kotlin`.

---

## 3. Gradle conventions

### 3.1 `settings.gradle.kts`

```kotlin
pluginManagement {
    includeBuild("build-logic")                          // the convention plugins
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")   // projects.core.model instead of project(":core:model")
rootProject.name = "<App>"
include(":app", ":core:common", ":core:model", ":core:network", ":core:database",
        ":core:datastore", ":core:designsystem", ":core:ui", ":core:testing")
include(":domain:common", ":data:common")
include(":domain:<feature>", ":data:<feature>", ":presentation:<feature>")
```

### 3.2 Root `build.gradle.kts` and `gradle.properties`

The root build file is **only** the `plugins { … apply false }` block that keeps AGP, Kotlin, KSP,
serialization and the Compose compiler on the classpath for the convention plugins. No `subprojects`,
no `allprojects`, no `extra`.

App identity and version are **Gradle properties** — reviewable in a diff, readable from a plugin
without reaching into another project's state:

```properties
# gradle.properties
app.applicationId=com.company.app
app.versionName=1.0.0        # raise BOTH on every release
app.versionCode=1
```

Build-type suffixes (`-Dev` / `-Release` / `-Staging`) are constants in `build-logic`. Keep any
suffix **behind the first `-`**: version comparisons use `substringBefore('-')`.

**Signing** is read in `app/build.gradle.kts` from `local.properties` (gitignored) with an
environment-variable fallback for CI (`signing.storeFile` / `SIGNING_STORE_FILE`, and the same for
`storePassword`, `keyAlias`, `keyPassword`). Never a literal in a committed file. `:app` fails
configuration with a message naming the four keys when a `release`/`staging` task is requested
without them — never let AGP silently produce an unsigned APK.

### 3.3 Module build files (this is all a module gets to say)

```kotlin
// domain:<feature>/build.gradle.kts
plugins { id("app.jvm.library") }
dependencies {
    api(projects.core.model)              // api only when a model crosses this module's signature
    implementation(projects.core.common)
    implementation(projects.domain.common)
}

// data:<feature>/build.gradle.kts
plugins { id("app.android.library"); alias(libs.plugins.kotlin.serialization) }   // serialization: the DTOs live here
android { namespace = "com.company.app.data.<feature>" }
dependencies {
    implementation(projects.domain.<feature>)
    implementation(projects.core.network); implementation(projects.core.database)
    implementation(projects.core.datastore); implementation(projects.core.common); implementation(projects.core.model)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.coroutines.test)
}

// presentation:<feature>/build.gradle.kts
plugins { id("app.android.library.compose") }
android { namespace = "com.company.app.presentation.<feature>" }
dependencies {
    implementation(projects.core.ui); implementation(projects.core.common); implementation(projects.domain.<feature>)
    testImplementation(projects.domain.common)   // only if the tests build a use case that needs it
}
```

Everything else — `compileSdk`, `minSdk`, JVM level, `buildConfig`, the `staging` build type, the
`BuildConfig.VERSION_NAME` mirroring, Compose, the Robolectric test flags, and the dependencies every
module of that type needs (coroutines, Koin, Compose BOM, lifecycle, the test stack) — comes from the
plugin. A module that repeats any of it is wrong.

### 3.4 `build-logic/` — the four convention plugins

```
build-logic/
├── settings.gradle.kts        versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
├── build.gradle.kts           `kotlin-dsl`; compileOnly(libs.android.gradlePlugin / kotlin.gradlePlugin / compose.gradlePlugin);
│                              gradlePlugin { register(...) } for the four ids below
└── src/main/kotlin/
    ├── ProjectExt.kt          libs catalog accessor · COMPILE_SDK/MIN_SDK/TARGET_SDK · JAVA_VERSION ·
    │                          appId/appVersionName/appVersionCode from gradle.properties · suffix constants ·
    │                          configureTestTrustStore() (Windows-ROOT for Robolectric behind a proxy)
    ├── JvmLibraryConventionPlugin.kt            app.jvm.library
    ├── AndroidLibraryConventionPlugin.kt        app.android.library
    ├── AndroidLibraryComposeConventionPlugin.kt app.android.library.compose  (applies app.android.library first)
    └── AndroidApplicationConventionPlugin.kt    app.android.application
```

| Plugin | Applies | Configures | Adds |
|---|---|---|---|
| `app.jvm.library` | `java-library`, `kotlin.jvm` | Java/Kotlin 11 | coroutines-core, koin-core; test: junit |
| `app.android.library` | `com.android.library` | compileSdk/minSdk, Java 11, `buildConfig`, `APPLICATION_ID`/`VERSION_CODE`/`VERSION_NAME`(+suffix) fields, `maybeCreate("staging")` | core-ktx, coroutines-android, koin-core, koin-android; test: junit |
| `app.android.library.compose` | `app.android.library`, `kotlin.plugin.compose` | `compose = true`, `isReturnDefaultValues`, `isIncludeAndroidResources` | Compose BOM/ui/material3/tooling, activity-compose, lifecycle viewmodel+runtime compose, koin-compose; test: `:core:testing`, robolectric, ui-test-junit4; **debugImplementation** ui-test-manifest |
| `app.android.application` | `com.android.application`, `kotlin.plugin.compose` | namespace/applicationId/version from properties, SDKs, Compose, the three build types with suffixes, test flags | same Compose/Koin set; test stack |

Three things about the plugins that are easy to get wrong:

- **`maybeCreate("staging")` in the library plugin, `getByName("staging")` in a module.** Every library must declare the build type, or `:app`'s staging variant falls back to the library's *release* variant — which resolves `core:network` to production and silently defeats the variant.
- **The plugins `compileOnly` AGP/KGP; the root `plugins { … apply false }` block is what puts them on the runtime classpath.** Remove that block and every `pluginManager.apply("com.android.library")` fails.
- **`ui-test-manifest` is `debugImplementation`.** Unit tests run against the debug variant's merged manifest, and that is what declares the activity the Compose rule launches into.

### 3.5 Which modules a feature actually needs

| Feature shape | Modules |
|---|---|
| Full vertical (own data + own screen) | `domain:X` + `data:X` + `presentation:X` |
| Second screen over the same aggregate (e.g. list + details) | only `presentation:Y`, reusing `domain:X`/`data:X` — **never duplicate a model to split it** |
| Screen that owns no data (e.g. settings borrowing auth + prefs) | only `presentation:X`, depending on several `domain:*` |
| Data with no screen of its own (e.g. profile shown in the drawer) | `domain:X` + `data:X`, consumed by `presentation:shell` |

### 3.6 Environments as build types, never as code

```kotlin
// core/network/build.gradle.kts
android.buildTypes {
    getByName("debug")   { buildConfigField("String", "BASE_URL", "\"https://staging.host/api/\"") }
    getByName("release") { buildConfigField("String", "BASE_URL", "\"https://api.host/api/\"") }
    getByName("staging") { buildConfigField("String", "BASE_URL", "\"https://staging.host/api/\"") }
}
```
- `staging` = release build (R8 on, not debuggable, release key) pointed at the staging server. It is the **only** way to exercise obfuscated code against a backend that is safe to mutate.
- Cleartext / network-security exemptions live in `app/src/debug/res/xml/` and `app/src/staging/res/xml/`, **never** in `main`.
- Anything variant-dependent in code takes a constructor parameter defaulting to `BuildConfig.DEBUG` (e.g. `isDebugBuild: Boolean = BuildConfig.DEBUG`) — unit tests are generated for the debug variant only, so a bare `BuildConfig.DEBUG` branch is one no test can reach.

---

## 4. Core infrastructure (copy these, do not reinvent)

### 4.1 `Result<T>` — `:core:model`
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data class Loading(val percentage: Int? = null) : Result<Nothing>()   // STREAMS ONLY (download progress, observation)
}
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T>
inline fun <T> Result<T>.onError(action: (Throwable, String?) -> Unit): Result<T>
inline fun <T> Result<T>.onLoading(action: (Int?) -> Unit): Result<T>
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R>
```
**A one-shot repo method is `suspend fun x(): Result<T>`.** Two outcomes, two branches in every
caller. `Loading` is never emitted for a request; the ViewModel sets `isLoading = true` before the
call and `false` after. `Flow<Result<T>>` is reserved for things that genuinely emit more than
once — a Room observation, a download with progress.

### 4.2 `AppException` + `AppCodes` — `:core:common`
```kotlin
class AppException(errorMsg: String? = null, val errorCode: Int = AppCodes.UNDEFINED) : Exception(errorMsg)
object AppCodes {
    const val UNDEFINED = -1
    // HTTP codes as constants: OK = 200, UNAUTHORIZED = 401, FORBIDDEN = 403, NOT_FOUND = 404, UNPROCESSABLE_ENTITY = 422 ...
    // App-specific codes in the 5xxxx range, each with a KDoc saying what the USER should do differently:
    const val LOCATION_UNAVAILABLE = 51200
    const val SESSION_ENDED = 51500
}
```
Rule: **a new error code exists only if the user's next move is different.** Two failures with the same remedy share a code.

### 4.3 Network — `:core:network` is the client; endpoints live in `data:<feature>`
```kotlin
// core:network
@Serializable data class ApiResponse<out T>(@SerialName("data") val data: T?, @SerialName("error") val message: String?)
@Serializable data class ApiError(@SerialName("error") val message: String? = null)
suspend fun HttpResponse.errorMessage(): String   // ApiError's message, else the status line

object HttpClientFactory { fun create(): HttpClient = HttpClient(OkHttp) {
    install(HttpTimeout) { requestTimeoutMillis = INFINITE_TIMEOUT_MS; socketTimeoutMillis = 60_000 }  // a request timeout caps TOTAL time and kills downloads
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }) }
    HttpResponseValidator { /* >=500 → AppException(body.message, status) */ }
    defaultRequest { contentType(Json); accept(Json); url(BuildConfig.BASE_URL) }
    // NO Logging plugin in ANY build type — bodies carry passwords/tokens, and even INFO leaks path params. Log status+duration only.
}}

fun Throwable.toNetworkError(): Result.Error       // rethrows CancellationException; maps UnknownHost/Timeout/Connect/Serialization/IO → codes
suspend inline fun <reified T> apiCall(emptyDataMessage: String, crossinline call: suspend () -> HttpResponse): Result<T>   // the default
suspend inline fun apiCallNoContent(crossinline call: suspend () -> HttpResponse): Result<Unit>                          // 204 / null data on success
inline fun <reified T> apiCallFlow(...): Flow<Result<T>>                                                                  // streams only
fun HttpResponse.saveFile(context: Context, fileName: String): Flow<Result<File>>                                         // progress as Loading(pct)

val networkModule = module { single { HttpClientFactory.create() }; single<ConnectivityChecker> { ConnectivityCheckerImpl(androidContext()) } }
```
- A failed response is parsed as `ApiError`, **never** as `ApiResponse<T>`: failure bodies have a different shape, and a parse throw would erase the status code every caller branches on.
- **The feature's API surface lives in `data:<feature>/api`** — `interface XxxAPI` + `class XxxAPIImpl(private val client: HttpClient)` — and its DTOs in `data:<feature>/dto`. `core:network` never learns an endpoint; touching one feature's DTO recompiles that feature, not every data module.
- Every API method takes the `token: String` explicitly; the **repo** reads it from `AppDataStore`. DTOs never carry the token.
- DTO field names are the backend's spelling (`@SerialName("order_id")`). Never "fix" a backend typo in a DTO name — it silently stops parsing.

### 4.4 Database — `:core:database`
- One `AppDatabase`, `exportSchema = true` into `core/database/schemas/` (committed; a shipped version's JSON is never edited).
- `fallbackToDestructiveMigration` **only** for debug builds, via `AppDatabase.create(context, isDebugBuild: Boolean = BuildConfig.DEBUG)`.
- Package per table: `<table>/entities`, `<table>/dao`. DAOs are Koin `single`s built from the database. Room needs one `@Database`, which is why entities stay central while DTOs do not.
- Keep a `MigrationsTest` harness on the JVM (Robolectric + `AndroidSQLiteDriver` + schema dir as a unit-test asset) even before the first migration exists.

### 4.5 Preferences — `:core:datastore`
- `interface AppDataStore` bound to `AppDataStoreImpl(dataStore: DataStore<Preferences>)` — the interface is what makes every consumer testable without a `Context`, and taking a `DataStore` instead of a `Context` is what lets a JVM test drive a real store over a temp file.
- Install `ReplaceFileCorruptionHandler { emptyPreferences() }`.
- **Never `catch { emit(default) }` on a read failure.** Retry silently (no emission) for preferences; emit an explicit `Unavailable` state for anything the UI must react to; let `IOException` propagate for auth reads — an unreadable token is not a signed-out user.

### 4.6 Design system — `:core:designsystem`
- `AppTheme` (Material 3, light/dark — the M3 baseline palette until a brand replaces `Color.kt` with a Material Theme Builder export) + `ExtendedColorScheme` via a `CompositionLocal` for roles Material lacks (`warning`, `info`, `errorBg`). A family added there is filled in for every scheme — a miss is a compile error, not a black.
- **Only the strings the shared `core:ui` components reference live here** (OK, back, close, retry, pull-to-refresh…), plus `app_name`. **A feature's strings go in its presentation module's `res/values/strings.xml`** (and `values-<lang>/`), so the feature is self-contained and a copy change does not recompile the app. Reference shared ones as `DesignSystemR.string.x`, the module's own as its `R.string.x`.
- Prefer vector drawables over icon packs; a pack `api`'d here reaches every module and only shows its cost in the release APK.
- `dimen/Sdp.kt`: `Int.sdp` / `Int.ssp` resolving Intuit's `_Nsdp` tables with the resource **id cached per process** in an `IntArray`. Keep `res/raw/keep.xml` so the resource shrinker does not strip them (failure is silent: everything renders as plain dp).

### 4.7 MVI base — `:core:ui`
```kotlin
interface MviContract<Intent, State, Effect> {
    val state: StateFlow<State>; val effect: Flow<Effect>; fun onIntent(intent: Intent)
}

abstract class BaseMviViewModel<Intent, State, Effect>(
    initialState: State,
    protected val savedStateHandle: SavedStateHandle,          // REQUIRED, no default
) : ViewModel(), MviContract<Intent, State, Effect> {
    abstract val errorHandler: CoroutineExceptionHandler
    private val _state = MutableStateFlow(initialState); override val state = _state.asStateFlow()
    private val _effect = Channel<Effect>(Channel.BUFFERED)   // Channel, not SharedFlow: effects sent mid-rotation are buffered, never dropped
    override val effect: Flow<Effect> = _effect.receiveAsFlow()
    protected fun setState(reducer: State.() -> State) = _state.update { it.reducer() }
    protected fun sendEffect(effect: Effect) { viewModelScope.launch(errorHandler + Dispatchers.Main) { _effect.send(effect) } }
    protected fun <T : Any> savedState(key: String, default: T): T = savedStateHandle[key] ?: default
    protected fun <T : Any> saveState(key: String, value: T?) { savedStateHandle[key] = value }
    protected fun <T : Any> savedStateFlow(key: String, default: T): StateFlow<T> = savedStateHandle.getStateFlow(key, default)
    protected fun <T : Any> saved(key: String, default: T): ReadWriteProperty<Any?, T>   // write-through delegate
}
```
`:core:ui` also owns the shared component library: `AppButton`s, `AppCard`s, `AppTextField`s, `ErrorDialog`/`AppAlertDialog`/`SuccessDialog` on one `DialogScaffold`, `AppTopBar`, `LoadingOverlay`, `AppPullToRefreshBox`, snackbar. Features compose these; they do not restyle Material directly.

---

## 5. The feature recipe (domain → data → presentation)

### 5.1 Domain — `:domain:<feature>` (pure Kotlin)
```kotlin
// models/OrderModel.kt — plain data class, already formatted for display where formatting is a domain rule
data class OrderModel(val id: Int, val reference: String, val status: StatusModel, ...)

// repo/OrdersRepo.kt — KDoc on every method: what it returns, which error codes it can produce
interface OrdersRepo {
    suspend fun getOrders(): Result<List<OrderModel>>
    suspend fun updateStatus(orderId: Int, statusId: Int, latitude: Double, longitude: Double): Result<OrderModel>
    fun observeCachedOrders(): Flow<List<OrderModel>>          // a Flow only because Room genuinely streams
}

// usecases/GetOrdersUseCase.kt — one class, one invoke; the rule lives HERE
class GetOrdersUseCase(private val repo: OrdersRepo) {
    suspend operator fun invoke(isActive: Boolean): Result<List<OrderModel>> =
        repo.getOrders().map { list -> list.filter { it.status.isActive == isActive }.sortedBy { it.timeTo } }
}

// usecases/UpdateOrderStatusUseCase.kt — a precondition that must not be bypassed is enforced here, not in the UI
class UpdateOrderStatusUseCase(private val repo: OrdersRepo, private val location: LocationProvider) {
    suspend operator fun invoke(orderId: Int, statusId: Int): Result<OrderModel> {
        val fix = location.currentLocation()
            ?: return Result.Error(AppException(errorCode = AppCodes.LOCATION_UNAVAILABLE))
        return repo.updateStatus(orderId, statusId, fix.lat, fix.lng)
    }
}

// di/DomainOrdersModule.kt — constructor DSL: no counting get()s by hand
val domainOrdersModule = module {
    factoryOf(::GetOrdersUseCase)
    factoryOf(::UpdateOrderStatusUseCase)
}
```
Use cases are `factory`; they are stateless and final (no interface — tests build the real one).

### 5.2 Data — `:data:<feature>`
```kotlin
// dto/OrderDTO.kt — the backend's spelling, nullable where the backend may omit
@Serializable data class OrderDTO(@SerialName("id") val id: Int, @SerialName("order_ref") val reference: String? = null, ...)

// api/OrdersAPI.kt + api/OrdersAPIImpl.kt
interface OrdersAPI {
    suspend fun getOrders(token: String): Result<List<OrderDTO>>
    suspend fun updateStatus(token: String, body: ChangeStatusDTO): Result<OrderDTO>
}
class OrdersAPIImpl(private val client: HttpClient) : OrdersAPI {
    override suspend fun getOrders(token: String) = apiCall<List<OrderDTO>>("Empty response body") {
        client.get("orders") { bearerAuth(token) }
    }
}

// mapping/OrdersMapping.kt — extension functions only; the single place a DTO/Entity becomes a Model
fun OrderDTO.toModel(statuses: List<StatusModel>): OrderModel = OrderModel(
    id = id,
    reference = reference?.trim()?.uppercase().orEmpty(),      // normalise ONCE, here
    status = statuses.firstOrNull { it.id == status } ?: unknownStatus(status ?: 0),   // fail visible, never crash
)

// repo/OrdersRepoImpl.kt — composes API + DAO + DataStore; reads the token itself
class OrdersRepoImpl(private val api: OrdersAPI, private val statusDao: StatusDao, private val dataStore: AppDataStore) : OrdersRepo {
    override suspend fun getOrders(): Result<List<OrderModel>> {
        val statuses = statusDao.getAll().map { it.toModel() }
        return api.getOrders(dataStore.getAuthToken()).map { dtos -> dtos.map { it.toModel(statuses) } }
    }
}

// di/DataOrdersModule.kt — binds the API and the repo to their interfaces
val dataOrdersModule = module {
    singleOf(::OrdersAPIImpl) bind OrdersAPI::class
    singleOf(::OrdersRepoImpl) bind OrdersRepo::class
}
```
Repos and APIs are `single`s bound to their interfaces. Writes that must survive interleaving (login/logout, cross-store commit points) go through a `Mutex`-backed guard that covers **local storage only, never a network call**.

### 5.3 Presentation — `:presentation:<feature>`

**Contract (one file):**
```kotlin
data class OrdersState(
    val orders: List<OrderModel> = emptyList(),
    val isLoading: Boolean = false,                // owned here — the repo never says "loading"
    val pendingStart: OrderModel? = null,          // dialog-in-progress lives in state
    val errorMessage: String? = null,              // relayed from the backend
    @param:StringRes val errorMessageRes: Int? = null,   // authored by the app — takes precedence
) {
    val hasError get() = errorMessageRes != null || errorMessage != null   // derived, not stored
}
sealed class OrdersIntent { data object Resumed : OrdersIntent(); data class OrderClicked(val order: OrderModel) : OrdersIntent(); ... }
sealed class OrdersEffect { data class NavigateToDetails(val orderId: Int) : OrdersEffect(); data object SessionExpired : OrdersEffect() }
```
- **State** = everything the screen renders, including "which dialog is open". Derived values are `get()` properties, never duplicated fields.
- **Intent** = what the user did, in the user's words (`OrderClicked`, `StartConfirmed`), not what the VM should do (`LoadData`).
- **Effect** = one-shot things: navigation, snackbar, session expiry. Never anything the screen has to keep showing.

**ViewModel:**
```kotlin
class OrdersViewModel(
    private val getOrders: GetOrdersUseCase,
    private val updateOrderStatus: UpdateOrderStatusUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseMviViewModel<OrdersIntent, OrdersState, OrdersEffect>(OrdersState(), savedStateHandle) {

    override val errorHandler = CoroutineExceptionHandler { _, t -> setState { copy(isLoading = false, errorMessage = t.message) } }
    private var loadJob: Job? = null                        // hold jobs that can race

    override fun onIntent(intent: OrdersIntent) = when (intent) {
        OrdersIntent.Resumed -> load()
        is OrdersIntent.OrderClicked -> onOrderClicked(intent.order)
        ...
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(errorHandler) {
            setState { copy(isLoading = true, errorMessage = null, errorMessageRes = null) }
            getOrders(isActive = true)
                .onSuccess { setState { copy(isLoading = false, orders = it) } }
                .onError { t, msg -> report(t, msg) }
        }
    }

    private fun report(t: Throwable, msg: String?) {
        when ((t as? AppException)?.errorCode) {
            AppCodes.UNAUTHORIZED -> { setState { copy(isLoading = false) }; sendEffect(OrdersEffect.SessionExpired) }
            AppCodes.LOCATION_UNAVAILABLE -> setState { copy(isLoading = false, errorMessageRes = R.string.error_cant_get_location) }
            else -> setState { copy(isLoading = false, errorMessage = msg) }
        }
    }
}
```
Rules: decisions (which dialog, whether back is allowed, whether a tap is refused) are made in the ViewModel so they are testable. Nothing writes a "guessed" status into state after a write — the state follows the repo's answer. A write in flight refuses a second tap. Guards checked in the VM are **also** passed to the control's `enabled`.

**Route + Screen (same file):**
```kotlin
@Composable
fun OrdersRoute(onOpenDetails: (Int) -> Unit, onSessionExpired: () -> Unit, modifier: Modifier = Modifier,
                viewModel: OrdersViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {                                  // single collector, exactly-once delivery
        viewModel.effect.collect { when (it) {
            is OrdersEffect.NavigateToDetails -> onOpenDetails(it.orderId)
            OrdersEffect.SessionExpired -> onSessionExpired()
        } }
    }
    LifecycleResumeEffect(Unit) { viewModel.onIntent(OrdersIntent.Resumed); onPauseOrDispose { } }
    OrdersScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun OrdersScreen(state: OrdersState, onIntent: (OrdersIntent) -> Unit, modifier: Modifier = Modifier) { /* stateless */ }
```
- Route: ViewModel via `koinViewModel()`, effect collection, lifecycle hooks, `BackHandler`s, permission launchers. **Nothing else.**
- Screen: pure function of `state`; every tap is `onIntent(...)`. Resolve `CompositionLocal`s (colours, etc.) **once** at the top and pass down.
- Navigation callbacks are plain lambdas — a presentation module has **no** `navigation-compose` dependency.
- The screen's strings come from **this module's** `res/values/strings.xml`; only the shared ones come from `DesignSystemR`.

**DI:**
```kotlin
val presentationOrdersModule = module { viewModelOf(::OrdersViewModel) }   // SavedStateHandle is injected by Koin's viewModelOf
```

### 5.4 Wiring into `:app`
```kotlin
// di/AppModules.kt — the ONLY aggregation point
val appModules: List<Module> = listOf(
    networkModule, databaseModule, dataStoreModule,
    domainCommonModule, dataCommonModule,
    domain<Feature>Module, data<Feature>Module, presentation<Feature>Module,   // one line per feature module
    presentationShellModule,
)

// src/test/.../di/AppModulesTest.kt — the graph is proven at test time, not on the first screen open
@OptIn(KoinExperimentalAPI::class)
class AppModulesTest {
    @Test fun `every Koin definition can be resolved`() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(Context::class, SavedStateHandle::class, DataStore::class, HttpClientEngine::class, HttpClientConfig::class),
        )
    }
}
```
`extraTypes` lists only what a factory supplies outside Koin (the Android `Context`, the
`SavedStateHandle` a ViewModel receives from its owner, the `DataStore` built from the Context, Ktor's
engine/config behind `HttpClientFactory.create()`). Add a type there only when a factory genuinely
provides it — never to silence a real gap. Needs `koin-test` + `kotlin-reflect` as `testImplementation`.

Then: add the module to `settings.gradle.kts`, add `implementation(projects.<layer>.<feature>)` to `:app`, add the route to `Screen.kt` and its `composable<...>` to `AppNavHost`.

---

## 6. Navigation & shell

```kotlin
@Serializable sealed class Screen {
    @Serializable data object Splash : Screen()
    @Serializable data object Login : Screen()
    @Serializable data object HomeGraph : Screen() {           // nested GRAPH, not nested NavHost
        @Serializable data object Orders : Screen()
        @Serializable data object Settings : Screen()
    }
    @Serializable data class OrderDetails(val orderId: Int) : Screen()   // id, not the model → survives process death
}
```
- **One `NavHost`, one back stack.** Group drawer destinations in a nested `navigation<HomeGraph>` so the chrome has one thing to test (`destination.hierarchy.any { it.hasRoute<HomeGraph>() }`).
- **Chrome is conditional; the nav host is not.** `AppShell` wraps `AppNavHost` in drawer + scaffold and switches the chrome off per destination. Never branch *around* the NavHost — Compose recreates it and drops the back stack.
- Drawer moves: `popUpTo<HomeGraph.Orders> { saveState = true } + launchSingleTop + restoreState` — pop to the graph's **start destination**, not the graph (popping to the graph leaves one non-graph entry and system back closes the app).
- Route args are read from `SavedStateHandle` by property name in the ViewModel; screens refetch by id.
- `:presentation:shell` supplies drawer UI + `ShellViewModel` (session, current user, logout, session-expired dialog) and has **no navigation dependency**; `:app` maps drawer entries to `Screen`s.
- Session handling: a `401` anywhere → `Effect.SessionExpired` → the shell decides (ask/retry/logout). Logout **navigates nobody**; the session flow emitting `SignedOut` is the single owner of that navigation.
- `BackHandler`s exist only where back must do something other than pop; the *decision* lives in the ViewModel (`Intent.BackPressed` → VM picks: leave / confirm / swallow).

---

## 7. Error & UX conventions

| Situation | Treatment |
|---|---|
| Validation the user can fix in a field | **field error** on that field, no request leaves the device |
| Backend rejects with a code the user can act on (403/404/422 on a specific action) | dialog **naming the thing** (the code, the field) and mapped to an app-authored `errorMessageRes` |
| Anything else from the backend | relay the backend's message (`errorMessage`) |
| `401` | never handled locally — `SessionExpired` effect to the shell |
| Background/secondary fetch fails (a list the screen can live without) | **no dialog**; an inline notice with a retry icon next to the control it disables |
| Precondition failure (no GPS, out of range, missing config) | own `AppCode`, own string, ordered so the first check is the one the user can act on |

- `errorMessageRes` (app-authored) always wins over `errorMessage` (relayed) when both are set.
- Dialog messages that contain markup are **opt-in** (`messageHasMarkup = state.errorMessageRes != null`) — never run a backend string through an HTML parser.
- Blocking `LoadingOverlay` only for work the user is waiting on; background refreshes show in the control (button `isLoading`, switch `thumbContent`).
- A dialog with no safe dismiss (forced update, session expired) has **no** dismiss route at all; a dialog whose cancel costs nothing is fully dismissible.
- Everything that changes state after a network write follows the **server's answer**, never the local guess.

---

## 8. Localisation & theme
- Two-language apps: `AppLanguage` enum with a `fromTag` fallback in one place; no "follow system" entry unless the handset's locale is a real third answer. Theme keeps `SYSTEM`.
- Apply language + night mode **inside the composition** through one `ProvideAppConfiguration` composable (a `ContextThemeWrapper` around the activity with `applyOverrideConfiguration`), providing `LocalContext` + `LocalConfiguration` + `LocalLayoutDirection`. Do **not** use `AppCompatDelegate` on a `ComponentActivity` — it is silently ignored below API 33.
- First frame waits on the stored choice through a bounded `Loading / Loaded / Error` loader (no `runBlocking` in `onCreate`).
- Data layer reads the **stored** language, not `Locale.getDefault()`; formatters in `:core:common` pin `Locale.ENGLISH`.

---

## 9. Testing conventions

### 9.1 Layout
- `:core:testing` (JVM): `MainDispatcherRule` (swaps `Dispatchers.Main` for `UnconfinedTestDispatcher`, exposes `dispatcher`) + `src/main/resources/robolectric.properties` with `sdk=34` — one copy reaching every consumer through the compose convention plugin.
- Every test lives under `src/test` and runs with `./gradlew test`. No `androidTest` suites; nothing runs them on push.
- **`AppModulesTest` in `:app`** (§5.4) is mandatory from day one. It is the one test that fails for a `get()` nobody registered.

### 9.2 ViewModel tests
```kotlin
class OrdersViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val repo = FakeOrdersRepo()                                    // fake the REPO (interface)
    private fun viewModel() = OrdersViewModel(GetOrdersUseCase(repo), UpdateOrderStatusUseCase(repo, FakeLocationProvider()), SavedStateHandle())
    private fun TestScope.effectsOf(vm: OrdersViewModel) = mutableListOf<OrdersEffect>().also { backgroundScope.launch { vm.effect.toList(it) } }

    @Test fun `resuming loads the active tab`() = runTest(mainDispatcherRule.dispatcher) {   // share the dispatcher
        repo.orders = listOf(order(1, isActive = true), order(2, isActive = false))
        val vm = viewModel()
        vm.onIntent(OrdersIntent.Resumed)
        assertEquals(listOf(1), vm.state.value.orders.map { it.id })
    }
}
```
- Fake the repo, build the **real** use case — the rule under test stays in the test.
- Fakes record calls (`getOrdersCalls`) and, where a write-in-flight rule exists, expose a `CompletableDeferred` the fake `await`s before answering, so the test can act mid-flight.
- Effects emitted in `init` are already in the channel; assert on what construction did, or keep `init` quiet.
- Test names are sentences in backticks describing the rule, not the method.

### 9.3 Compose screen tests (Robolectric, JVM)
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xhdpi")             // Robolectric's default 320x470 puts half of every screen below the fold
class OrdersScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val intents = mutableListOf<OrdersIntent>()
    private fun showScreen(state: OrdersState = OrdersState()) = composeRule.setContent { AppTheme { OrdersScreen(state, intents::add) } }
    private fun string(res: Int, vararg args: Any) = composeRule.activity.getString(res, *args)
}
```
- No Koin, no nav, no ViewModel: state in, intents out.
- Match a dialog button through `hasAnyAncestor(isDialog())`; a label shared with a title through `hasClickAction()`.
- A test that types must mirror the edit back into state (a stateless screen reverts the field otherwise).
- The wiring (`ui-test-manifest` as `debugImplementation`, `isIncludeAndroidResources`, `isReturnDefaultValues`, the pinned `sdk`, the Windows trust store) comes from the compose convention plugin — a module never repeats it.
- A shell/navigation test in `:app` drives the **real** `AppShell` + `NavHost` + ViewModels over fakes, with `@Config(application = Application::class)` so the real `Application` does not start Koin.

### 9.4 Data-layer tests
- Repo impl tests exist to pin **mappers** (adjacent same-typed fields swap silently) and **write ordering** (fake DAO records ops in a list, not flags).
- Inject a failure at one specific write to prove a commit-point ordering.
- DataStore tests use a real `PreferenceDataStoreFactory` over a `TemporaryFolder` with its own `CoroutineScope(Dispatchers.IO)`, never `runTest`'s scope.

### 9.5 What stays hand-only
Camera, real IME, RTL layout on a device, pixels. Say so in the docs rather than pretending.

---

## 10. Release engineering

- **R8 on** for release (`isMinifyEnabled` + `isShrinkResources`). Keep rules only for what R8 cannot see: `@Serializable` route classes, DTO **fields** (JSON keys), enums resolved from stored strings. Libraries ship their own consumer rules.
- **CI (`ci.yml`)** on push/PR to the trunk: `./gradlew test` → `assembleDebug` → restore keystore from secrets → `:app:assembleRelease` → post-build APK assertions (e.g. count kept resources with `aapt2 dump resources`) → `lint`. Cache Robolectric jars keyed on the version + `robolectric.properties`.
- **Release (`release.yml`)** on a `v*` tag: verify tag == `app.versionName`, run tests, decode keystore, build, publish the APK renamed `<App>V<version>.apk`.
- Trunk takes no direct pushes (ruleset: PR + green check + up to date). Work happens on a `staging` branch.
- Cutting a release: bump **both** `app.versionName` and `app.versionCode` in `gradle.properties` in one commit → PR → merge → tag the merge commit → push tag. If the app self-updates from a backend version row, updating that row is part of the release, not a follow-up.
- Signing credentials: `local.properties` (gitignored) with env-var fallback; keystore backed up outside any one laptop.

---

## 11. Documentation rule
Keep a `CLAUDE.md` / `AGENTS.md` at the root that is the **source of truth**: module table, the conventions above, and — most importantly — the *why* behind every non-obvious decision (what broke, what it cost). `README.md` is a summary of it. When a rule here is changed, the doc changes in the same commit.

---

## 12. Checklist — adding feature `<feature>`

1. `settings.gradle.kts`: `include(":domain:<feature>", ":data:<feature>", ":presentation:<feature>")` (only the ones needed, §3.5). Each module's build file is the plugin id + namespace + its own deps (§3.3).
2. `:domain:<feature>`: models → `<Feature>Repo` interface (`suspend fun … : Result<T>`, KDoc per method) → use cases → `domain<Feature>Module` with `factoryOf`.
3. `:data:<feature>`: `dto/` (backend spelling) → `api/<Feature>API` + `Impl` via `apiCall` → `mapping/` → `repo/<Feature>RepoImpl` → `data<Feature>Module` with `singleOf(...) bind`.
4. `:core:database` only if a table is needed: entity + DAO → register in `AppDatabase` → export schema → bind DAO in `databaseModule`.
5. `:presentation:<feature>`: `contract/` → `viewmodel/` (owns `isLoading`) → `screen/` (`Route` + `Screen`) → `component/` → its own `res/values/strings.xml` (+ every language) → `presentation<Feature>Module` with `viewModelOf`.
6. Shared strings/drawables only if a `core:ui` component needs them → `:core:designsystem`.
7. `:app`: dependencies → `AppModules.kt` → `Screen.kt` → `AppNavHost` → shell/drawer entry if applicable. **Run `AppModulesTest`.**
8. Tests: `<Feature>ViewModelTest` (fake repo + real use cases), `<Feature>ScreenTest` (Robolectric), `<Feature>RepoImplTest` if there is a mapper or a write ordering worth pinning.
9. Update `CLAUDE.md`: module table row + a section explaining every decision a future reader would not guess.
