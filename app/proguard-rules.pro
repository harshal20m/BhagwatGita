# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# ── Room ──────────────────────────────────────────────────────────────────────
# Keep all Room entities, DAOs, and database classes intact.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract !static *;
}
# Room-generated _Impl classes are loaded via reflection at runtime
-keep class **.*_Impl { *; }
-keep class **.*_Impl$* { *; }

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
# Hilt-generated component classes
-keep class **_HiltComponents* { *; }
-keep class **_GeneratedInjector { *; }
-keep class *_MembersInjector { *; }

# ── WorkManager + HiltWorker ──────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
# HiltWorkerFactory uses reflection to instantiate workers
-keep @dagger.assisted.AssistedInject class * { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }

# ── Kotlinx Serialization ─────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}
# Keep all @Serializable data classes used in SeedDatabaseWorker JSON parsing
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static ** $serializer;
    synthetic <init>(**);
    *** serializer();
}

# ── Jetpack Compose ───────────────────────────────────────────────────────────
# Compose compiler handles most of this, but keep lambda stability info
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keep class com.gitaapp.core.di.PreferencesManager { *; }

# ── App models (used in Room POJOs and Hilt injection) ───────────────────────
-keep class com.gitaapp.data.model.** { *; }
-keep class com.gitaapp.core.database.entity.** { *; }
-keep class com.gitaapp.core.database.dao.** { *; }

# ── Remove logging in release ──────────────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# ── Miscellaneous ─────────────────────────────────────────────────────────────
# Keep the BuildConfig class
-keep class com.gitaapp.BuildConfig { *; }
# Avoid stripping enum values used in DataStore preferences
-keepclassmembers enum com.gitaapp.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
