package com.gitaapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gitaapp.core.database.dao.ChapterDao
import com.gitaapp.core.database.dao.VerseDao
import com.gitaapp.core.database.entity.ChapterEntity
import com.gitaapp.core.database.entity.VerseEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * One-shot WorkManager worker that reads the bundled JSON assets and
 * populates the Room database on first launch.
 *
 * Idempotent — checks if chapters already exist before inserting.
 */
@HiltWorker
class SeedDatabaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Guard: skip if already seeded
            if (chapterDao.getChapterCount() > 0) {
                return Result.success()
            }

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

            // Load and parse chapters
            val chaptersJson = applicationContext.assets
                .open(CHAPTERS_FILE)
                .bufferedReader()
                .use { it.readText() }

            val chaptersData = json.decodeFromString<List<ChapterJson>>(chaptersJson)
            val chapterEntities = chaptersData.map { it.toEntity() }
            chapterDao.insertAll(chapterEntities)

            // Load and parse verses
            val versesJson = applicationContext.assets
                .open(VERSES_FILE)
                .bufferedReader()
                .use { it.readText() }

            val versesData = json.decodeFromString<List<VerseJson>>(versesJson)
            val verseEntities = versesData.map { it.toEntity() }
            verseDao.insertAll(verseEntities)

            // Update widget after seeding
            try {
                val verse = verseDao.getRandomVerse()
                if (verse != null) {
                    com.gitaapp.widget.VerseOfDayWidgetUpdater.update(
                        context = applicationContext,
                        verseRef = "${verse.chapterNumber}.${verse.verseNumber}",
                        sanskrit = verse.sanskritText,
                        translationEn = verse.translation,
                        translationHi = verse.translationHi,
                        language = "ENGLISH" // Default
                    )
                }
            } catch (e: Exception) {
                // Ignore widget update errors
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "SeedDatabaseWork"
        private const val CHAPTERS_FILE = "gita_chapters.json"
        private const val VERSES_FILE = "gita_verses.json"
        private const val MAX_RETRIES = 3
    }
}

// ── JSON deserialization models ───────────────────────────────────────────────

@Serializable
private data class ChapterJson(
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("name_transliterated") val nameTransliterated: String,
    @SerialName("name_sanskrit") val nameSanskrit: String,
    @SerialName("name_meaning") val nameMeaning: String,
    @SerialName("summary") val summary: String,
    @SerialName("verse_count") val verseCount: Int
) {
    fun toEntity() = ChapterEntity(
        chapterNumber = chapterNumber,
        nameTransliterated = nameTransliterated,
        nameSanskrit = nameSanskrit,
        nameMeaning = nameMeaning,
        summary = summary,
        verseCount = verseCount
    )
}

@Serializable
private data class VerseJson(
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_number") val verseNumber: Int,
    @SerialName("sanskrit_text") val sanskritText: String,
    @SerialName("transliteration") val transliteration: String,
    @SerialName("word_meanings") val wordMeanings: String,
    @SerialName("translation") val translation: String,
    @SerialName("commentary") val commentary: String
) {
    fun toEntity() = VerseEntity(
        verseId = "$chapterNumber.$verseNumber",
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        sanskritText = sanskritText,
        transliteration = transliteration,
        wordMeanings = wordMeanings,
        translation = translation,
        translationHi = commentary,
        commentary = commentary
    )
}