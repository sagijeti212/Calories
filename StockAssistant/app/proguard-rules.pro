# Keep Retrofit + Gson DTO classes
-keep class com.stockassistant.app.data.api.dto.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }

# Room
-keep class androidx.room.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**
