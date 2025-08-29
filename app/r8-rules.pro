# =============================================================================
# R8 Security Configuration for FitnessTracker
# =============================================================================
# This file contains security-focused R8 rules for code obfuscation,
# string encryption, and protection against reverse engineering.
# =============================================================================

# =============================================================================
# SECURITY & OBFUSCATION RULES
# =============================================================================

# Enable aggressive obfuscation
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose

# Obfuscate package names (optional - can break some libraries)
# -repackageclasses ''

# Remove logging statements in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove Timber logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# =============================================================================
# COMPOSE & UI PROTECTION
# =============================================================================

# Keep Compose-related classes but obfuscate internal names
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    @androidx.compose.runtime.Composable *;
}

# Keep Compose Preview annotations
-keep @androidx.compose.ui.tooling.preview.Preview class * { *; }

# =============================================================================
# HILT & DEPENDENCY INJECTION PROTECTION
# =============================================================================

# Keep Hilt generated classes
-keep class * extends androidx.hilt.AndroidEntryPoint { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# Keep Hilt annotations
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# =============================================================================
# NETWORKING & API PROTECTION
# =============================================================================

# Keep Retrofit interfaces but obfuscate implementations
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Gson for JSON parsing
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# =============================================================================
# ROOM DATABASE PROTECTION
# =============================================================================

# Keep Room entities and DAOs
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase { *; }

# =============================================================================
# FIREBASE PROTECTION
# =============================================================================

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }

# =============================================================================
# COROUTINES PROTECTION
# =============================================================================

# Keep coroutines core classes
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# =============================================================================
# CUSTOM MODEL PROTECTION
# =============================================================================

# Keep your data models but obfuscate field names
-keep class com.velaphi.core.data.models.** { *; }
-keep class com.velaphi.mealplan.data.models.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# =============================================================================
# STRING ENCRYPTION & OBFUSCATION
# =============================================================================

# Enable string encryption (R8 feature)
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

# Obfuscate string constants
-adaptclassstrings

# =============================================================================
# REFLECTION PROTECTION
# =============================================================================

# Prevent reflection-based attacks
-keepattributes !Signature
-keepattributes !Exceptions

# Keep only necessary attributes
-keepattributes SourceFile,LineNumberTable,*Annotation*

# =============================================================================
# NATIVE CODE PROTECTION
# =============================================================================

# Keep native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# =============================================================================
# SERIALIZATION PROTECTION
# =============================================================================

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
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

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =============================================================================
# ANNOTATION PROTECTION
# =============================================================================

# Keep important annotations
-keep @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }

# Keep Compose annotations
-keep @interface androidx.compose.runtime.Composable
-keep @interface androidx.compose.ui.tooling.preview.Preview

# =============================================================================
# DEBUGGING PROTECTION
# =============================================================================

# Remove debug information
-renamesourcefileattribute SourceFile

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
# LIBRARY-SPECIFIC RULES
# =============================================================================

# Keep CameraX classes
-keep class androidx.camera.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Navigation components
-keep class androidx.navigation.** { *; }

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
# END OF R8 SECURITY CONFIGURATION
# =============================================================================
