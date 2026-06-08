# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep native line numbers and debugging attributes for logs but secure the names
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,Exceptions,*Annotation*

# Hide the original source file name in stack traces
-renamesourcefileattribute SourceFile

# --- Jetpack Compose Rules ---
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# --- Room Database Protection Rules ---
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Dao
-dontwarn androidx.room.**

# --- Moshi Serialization Reflection Rules ---
-keep class com.example.data.model.** { *; }
-keep class * {
    @com.squareup.moshi.Json *;
}
-dontwarn com.squareup.moshi.**

# --- Retrofit and OkHttp Rules ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Media3 and ExoPlayer Rules ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Secure intellectual protection limits against repackaging ---
-keep class com.example.data.security.SecurityManager { *; }
-keepclassmembers class com.example.data.security.SecurityManager {
    public *;
}

