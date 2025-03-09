-keep class org.xwalk.core.** {
  *;
}

-dontwarn org.chromium.**

-keep class org.chromium.** {
  *;
}

-keep class com.squareup.okhttp3.** {
  *;
}

-keepclassmembers class * implements javax.net.ssl.SSLSocketFactory {
    private final javax.net.ssl.SSLSocketFactory delegate;
}