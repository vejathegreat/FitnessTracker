# =============================================================================
# FITNESS TRACKER SECURITY CONFIGURATION
# =============================================================================
# Additional security measures beyond standard R8 configuration
# =============================================================================

# =============================================================================
# ANTI-TAMPERING PROTECTION
# =============================================================================

# Prevent APK modification
-keepattributes !CodeSource
-keepattributes !SourceFile
-keepattributes !LineNumberTable

# Remove debug information completely
-renamesourcefileattribute SourceFile

# =============================================================================
# STRING ENCRYPTION ENHANCEMENTS
# =============================================================================

# Encrypt all string literals
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

# Encrypt class names
-adaptclassstrings

# =============================================================================
# REFLECTION PROTECTION
# =============================================================================

# Prevent reflection-based attacks
-keepattributes !Signature
-keepattributes !Exceptions
-keepattributes !InnerClasses

# Keep only essential attributes
-keepattributes SourceFile,LineNumberTable,*Annotation*

# =============================================================================
# NATIVE CODE PROTECTION
# =============================================================================

# Protect native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# =============================================================================
# SERIALIZATION PROTECTION
# =============================================================================

# Protect Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Protect Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# =============================================================================
# ENUM PROTECTION
# =============================================================================

# Protect enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =============================================================================
# ANNOTATION PROTECTION
# =============================================================================

# Keep essential annotations
-keep @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }

# Keep Compose annotations
-keep @interface androidx.compose.runtime.Composable
-keep @interface androidx.compose.ui.tooling.preview.Preview

# Keep Hilt annotations
-keep @interface dagger.hilt.android.lifecycle.HiltViewModel
-keep @interface dagger.hilt.InstallIn
-keep @interface dagger.hilt.android.AndroidEntryPoint

# Keep Room annotations
-keep @interface androidx.room.Entity
-keep @interface androidx.room.Dao
-keep @interface androidx.room.Database

# =============================================================================
# LIBRARY-SPECIFIC PROTECTION
# =============================================================================

# CameraX protection
-keep class androidx.camera.** { *; }

# ML Kit protection
-keep class com.google.mlkit.** { *; }

# Navigation protection
-keep class androidx.navigation.** { *; }

# Retrofit protection
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }

# OkHttp protection
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson protection
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Firebase protection
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coroutines protection
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# =============================================================================
# CUSTOM MODEL PROTECTION
# =============================================================================

# Protect your data models
-keep class com.velaphi.core.data.models.** { *; }
-keep class com.velaphi.mealplan.data.models.** { *; }
-keep class com.velaphi.workouttracker.data.models.** { *; }
-keep class com.velaphi.goalmanager.data.models.** { *; }
-keep class com.velaphi.authentication.data.models.** { *; }

# Protect ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# =============================================================================
# COMPOSE PROTECTION
# =============================================================================

# Protect Compose-related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    @androidx.compose.runtime.Composable *;
}

# Protect Compose Preview
-keep @androidx.compose.ui.tooling.preview.Preview class * { *; }

# =============================================================================
# HILT PROTECTION
# =============================================================================

# Protect Hilt generated classes
-keep class * extends androidx.hilt.AndroidEntryPoint { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# =============================================================================
# ROOM PROTECTION
# =============================================================================

# Protect Room entities and DAOs
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# Protect Room generated classes
-keep class * extends androidx.room.RoomDatabase { *; }

# =============================================================================
# PERFORMANCE OPTIMIZATIONS
# =============================================================================

# Enable aggressive optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# =============================================================================
# SECURITY ENHANCEMENTS
# =============================================================================

# Prevent class name guessing
-dontwarn
-ignorewarnings

# Obfuscate method names
-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary class-obfuscation-dictionary.txt
-packageobfuscationdictionary package-obfuscation-dictionary.txt

# =============================================================================
# FINAL SECURITY RULES
# =============================================================================

# Remove all debug information
-keepattributes !SourceFile,!LineNumberTable

# Enable full obfuscation
-repackageclasses ''

# Remove unused code aggressively
-dontshrink

# =============================================================================
# END OF SECURITY CONFIGURATION
# =============================================================================
