# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.chronos.clock.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.chronos.clock.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.chronos.clock.data.**$$serializer { *; }

# Model classes are reflected over by the serializer plugin.
-keep class com.chronos.clock.data.** { *; }
