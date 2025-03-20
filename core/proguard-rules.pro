# 保留Annotation不混淆
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable

# 保护所有核心API接口
-keep public class org.yameida.worktool.core.api.** { *; }

# 保护所有Model类
-keep class org.yameida.worktool.core.model.** { *; }
-keepclassmembers class org.yameida.worktool.core.model.** {
    <fields>;
    <methods>;
}

# 保护核心服务实现
-keep class org.yameida.worktool.core.service.** { *; }
-keepclassmembers class org.yameida.worktool.core.service.** {
    public <methods>;
}

# 保护数据库相关类
-keep class org.yameida.worktool.core.data.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# 保护自定义注解
-keep @interface org.yameida.worktool.core.annotation.** { *; }
-keepclassmembers class * {
    @org.yameida.worktool.core.annotation.* <methods>;
    @org.yameida.worktool.core.annotation.* <fields>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keepclasseswithmembers class * {
    @dagger.* <fields>;
}
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}

# 保护枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保护Native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 移除日志
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}