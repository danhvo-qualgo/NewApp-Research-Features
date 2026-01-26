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

# Keep metadata for serialization classes
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

# Keep classes annotated with @Serializable
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep generated serializer classes
-keepclassmembers class **$$serializer { *; }

# Keep kotlinx.serialization.* classes and their members
-keepnames class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Do not warn about kotlinx.serialization related classes
-dontwarn kotlinx.serialization.**

# Keep enum members if you are serializing enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}