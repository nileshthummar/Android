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

# AppsFlyer SDK:
-dontwarn com.appsflyer.*

# ========== Brightcove Start:
##---------------Proguard configuration for SDK  ----------
-dontwarn com.brightcove.player.display.VideoDisplayComponent
-dontwarn com.brightcove.player.view.BrightcoveClosedCaptioningSurfaceView
-dontwarn com.brightcove.player.view.BrightcoveClosedCaptioningSurfaceView$1
-dontwarn com.google.**
-dontwarn tv.freewheel.**
-dontwarn android.media.**
-keep class android.media.** { *; }
-keep class com.google.** { *; }
-keep interface com.google.** { *; }
-keep class com.google.ads.interactivemedia.** { *; }
-keep interface com.google.ads.interactivemedia.** { *; }

#--- PiP Support Specific Rules ---
-dontwarn android.app.**

-keep class com.brightcove.player.** { *; }


##---------------Proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------Proguard configuration for Requery  ----------
-dontwarn java.lang.FunctionalInterface
-dontwarn java.util.**
-dontwarn java.time.**
-dontwarn javax.annotation.**
-dontwarn javax.cache.**
-dontwarn javax.naming.**
-dontwarn javax.transaction.**
-dontwarn java.sql.**
-dontwarn javax.sql.**
-dontwarn android.support.**
-dontwarn io.requery.cache.**
-dontwarn io.requery.rx.**
-dontwarn io.requery.reactivex.**
-dontwarn io.requery.reactor.**
-dontwarn io.requery.query.**
-dontwarn io.requery.android.**
-dontwarn io.requery.proxy.**

-keepclassmembers enum io.requery.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

##---------------Proguard configuration for FileDownloader Library  ----------
-dontwarn okhttp3.*
-dontwarn okio.**
-dontwarn com.liulishuo.filedownloader.**

# ========== Brightcove End
