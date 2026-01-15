# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes LineNumberTable,SourceFile

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all classes untuk mencegah crash pada dependency injection
-keep class com.wall.fakelyze.** { *; }

# TensorFlow Lite - Critical untuk mencegah crash ML model
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# Koin Dependency Injection
-keep class org.koin.** { *; }
-keep class kotlin.Metadata { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcher {}

# DataStore
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.** { *; }

# Room Database
-keep class androidx.room.** { *; }
-keepclassmembers class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep data classes dan model classes
-keep class com.wall.fakelyze.data.model.** { *; }
-keep class com.wall.fakelyze.domain.model.** { *; }

# Keep ViewModels
-keep class com.wall.fakelyze.ui.screens.**.ViewModel { *; }
-keepclassmembers class com.wall.fakelyze.ui.screens.**.ViewModel { *; }

# Keep Application class
-keep class com.wall.fakelyze.FakelyzeApplication { *; }

# Keep MainActivity
-keep class com.wall.fakelyze.MainActivity { *; }

# Keep ImageClassifier
-keep class com.wall.fakelyze.ml.ImageClassifier { *; }
-keepclassmembers class com.wall.fakelyze.ml.ImageClassifier { *; }

# Keep Repository implementations
-keep class com.wall.fakelyze.data.repository.**Impl { *; }
-keepclassmembers class com.wall.fakelyze.data.repository.**Impl { *; }

# Prevent obfuscation of enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
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

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Gson (jika digunakan)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp dan Retrofit (jika digunakan di masa depan)
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Mencegah warning dan error
-dontwarn org.tensorflow.**
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.coroutines.**
-dontwarn org.koin.**

# Keep annotation classes
-keep class kotlin.annotation.** { *; }
-keepattributes *Annotation*

# Keep reflection untuk Koin
-keepattributes *Annotation*, InnerClasses
-dontnote kotlin.**
-dontwarn kotlin.**
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**
