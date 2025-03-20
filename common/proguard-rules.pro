# 保留Annotation不混淆
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable

# 保护所有公共工具类
-keep class org.yameida.worktool.common.utils.** { *; }
-keepclassmembers class org.yameida.worktool.common.utils.** {
    public static *;
}

# 保护扩展函数
-keep class org.yameida.worktool.common.extensions.** { *; }
-keepclassmembers class org.yameida.worktool.common.extensions.** {
    public static *;
}

# 保护基础组件
-keep class org.yameida.worktool.common.base.** { *; }
-keepclassmembers class org.yameida.worktool.common.base.** {
    public <methods>;
}

# 保护常量类
-keep class org.yameida.worktool.common.constants.** { *; }

# 保护自定义View
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Kotlin相关
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# 通用工具库
-keep class com.jakewharton.timber.** { *; }
-dontwarn org.jetbrains.annotations.**
-keep class org.jetbrains.annotations.** { *; }

# 保护枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 移除日志打印
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
}