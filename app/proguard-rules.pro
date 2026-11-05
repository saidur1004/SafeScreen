# ==============================================================================
# SafeScreen Production R8 & ProGuard Optimization Rules
# Ensures safe code obfuscation, shrinking, and reflection preservation.
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. TensorFlow Lite & TFLite Support (NNAPI / Native C++ bindings)
# ------------------------------------------------------------------------------
-keep class org.tensorflow.lite.** { *; }
-keep interface org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-keep interface org.tensorflow.lite.support.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**
-dontwarn org.tensorflow.lite.support.**

# ------------------------------------------------------------------------------
# 2. Room Database & SQLite ORM
# ------------------------------------------------------------------------------
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class * extends androidx.room.RoomOpenHelper$Delegate { *; }
-dontwarn androidx.room.**

# ------------------------------------------------------------------------------
# 3. Jetpack DataStore Preferences
# ------------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-dontwarn androidx.datastore.**

# ------------------------------------------------------------------------------
# 4. Dagger Hilt & Dependency Injection
# ------------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.EntryPoint interface * { *; }
-keep @dagger.hilt.InstallIn interface * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}
-dontwarn dagger.hilt.**

# ------------------------------------------------------------------------------
# 5. Kotlin Coroutines & Asynchronous Flows
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------
# 6. SafeScreen Domain Models, Enums & Reflection Objects
# ------------------------------------------------------------------------------
-keep class com.safescreen.app.data.local.entity.** { *; }
-keep class com.safescreen.app.data.ai.** { *; }
-keep class com.safescreen.app.services.ai.** { *; }
-keep class com.safescreen.app.services.capture.CaptureMetrics { *; }
-keep class com.safescreen.app.services.audio.AudioCaptureMetrics { *; }
-keep class com.safescreen.app.presentation.screens.settings.SensitivityLevel { *; }
-keep class com.safescreen.app.presentation.screens.settings.PerformanceProfile { *; }
-keep class com.safescreen.app.presentation.screens.home.BlurStyle { *; }

# ------------------------------------------------------------------------------
# 7. Android Lifecycle & System Services
# ------------------------------------------------------------------------------
-keep class com.safescreen.app.services.ShieldProtectionService { *; }
-keep class com.safescreen.app.services.SafeScreenAccessibilityService { *; }
-keep class com.safescreen.app.services.overlay.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

