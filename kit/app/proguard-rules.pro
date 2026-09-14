# R8 rules for the release/staging builds. Most dependencies ship their own consumer rules inside
# their artifact — Ktor, Room, kotlinx.serialization, Compose, Lottie all do — so what is left here
# is only the things R8 cannot see from the bytecode alone.

# --- Typed navigation routes -------------------------------------------------------------------
#
# `Screen` and its nested routes are @Serializable, and NavController resolves them through the
# generated serializers at runtime rather than from a call site R8 can trace. Obfuscating the class
# names changes the route keys with them.
-keep class com.starter.app.navigation.Screen { *; }
-keep class com.starter.app.navigation.Screen$* { *; }
-keepclassmembers class com.starter.app.navigation.Screen$* {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- DTOs --------------------------------------------------------------------------------------
#
# Every network model is @Serializable and its property names are the JSON keys the backend sends.
# The serialization plugin keeps the generated serializers, but the field names still have to
# survive: they are the backend's spelling and nothing regenerates them.
-keepclassmembers @kotlinx.serialization.Serializable class com.starter.app.** {
    <fields>;
}

# --- Enums -------------------------------------------------------------------------------------
#
# Enums resolved from strings held in DataStore (a language tag, a theme mode) go through
# `valueOf`/`values()`, which R8 cannot follow back from the stored string.
-keepclassmembers enum com.starter.app.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Keep the crash reports readable -------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
