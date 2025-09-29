# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# FIXED: Hilt/Dagger keeps (minimum)
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }

# FIXED: TensorFlow Lite / org.tensorflow
-keep class org.tensorflow.** { *; }
-keep class org.tensorflow.lite.** { *; }

# FIXED: JNI loaders (keep native methods)
-keepclasseswithmembernames class * {
    native <methods>;
}

# FIXED: Example keep for codecs/models if reflective usage occurs
-keep class com.pandora.core.ai.compression.** { *; }
-keep class com.pandora.core.ai.ml.** { *; }
