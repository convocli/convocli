# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.convocli.**$$serializer { *; }
-keepclassmembers class com.convocli.** {
    *** Companion;
}
-keepclasseswithmembers class com.convocli.** {
    kotlinx.serialization.KSerializer serializer(...);
}
