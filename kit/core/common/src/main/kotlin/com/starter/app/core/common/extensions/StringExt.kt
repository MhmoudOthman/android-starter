package com.starter.app.core.common.extensions

fun String.isValidEmail(): Boolean =
    this.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))

fun String.isValidPhone(): Boolean =
    this.matches(Regex("^[+]?[0-9]{10,13}\$"))

fun String.toSlug(): String =
    this.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

/**
 * A version string with its suffixes dropped — `2.0.0-Release` and `2.0.0-Dev` both become `2.0.0`.
 *
 * `BuildConfig.VERSION_NAME` is the base version plus a build-type suffix (`-Dev` / `-Release` /
 * `-Staging`), while the backend's `GET version` row holds the base on its own. v1 compared the raw
 * strings, which was safe there because its `versionName` carried no suffix at all.
 *
 * Both sides of every version comparison go through this, and both are checked with `!=` rather
 * than "newer than" — so a suffix that leaks into the comparison is not a stray prompt: on the
 * splash the update dialog has no dismiss, making it a screen the user cannot get past. Keep any
 * new suffix behind the first `-`.
 *
 * It lives here rather than beside either caller because there are now two — the splash and the
 * settings screen's own check — and a rule this sharp must not be written down twice.
 */
fun String.baseVersion(): String = substringBefore('-').trim()
