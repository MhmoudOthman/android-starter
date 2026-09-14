package com.starter.app.core.common.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val API_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
private val API_TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)
private val DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
private val DISPLAY_TIME = DateTimeFormatter.ofPattern("hh:mm(a)", Locale.ENGLISH)

/**
 * `2024-12-12` → `12/12/2024`. Returns the input untouched when it isn't a date the API shape
 * covers, so a backend change surfaces as an odd-looking label rather than a crash.
 */
fun String.toDateFormat(): String = try {
    LocalDate.parse(this, API_DATE).format(DISPLAY_DATE)
} catch (e: DateTimeParseException) {
    this
}

/** `08:00:00` → `08:00(AM)`. Same lenient fallback as [toDateFormat]. */
fun String.toTimeFormat(): String = try {
    LocalTime.parse(this, API_TIME).format(DISPLAY_TIME)
} catch (e: DateTimeParseException) {
    this
}

/** `300.0` → `300.00`, grouped with thousands separators. */
fun Double.toPriceFormat(): String =
    DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(this)

fun Double?.toPriceFormat(): String =
    DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(this ?: 0.0)
