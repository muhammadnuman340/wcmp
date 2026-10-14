# WCMP ProGuard / R8 Rules

# Keep generic data models (Room entities, DataStore objects)
-keep class com.numan.wcmp.data.local.entity.** { *; }
-keepclassmembers class com.numan.wcmp.data.local.entity.** { *; }

# WorkManager and Room are generally well-handled by R8's built-in consumer rules
# but keep specific Domain Enums just in case to avoid serialization drift
-keepclassmembers enum com.numan.wcmp.domain.** { *; }

# Keep the Application class
-keep class com.numan.wcmp.WcmpApplication { *; }
