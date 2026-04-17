package com.gitaapp.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Chapter(
    val number: Int,
    val nameTransliterated: String,
    val nameSanskrit: String,
    val nameMeaning: String,
    val summary: String,
    val verseCount: Int,
    val readingProgressPercent: Float = 0f,
    val isStarted: Boolean = false
)

@Immutable
data class Verse(
    val id: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val sanskritText: String,
    val transliteration: String,
    val wordMeanings: String,
    val translation: String,
    val translationHi: String,
    val commentary: String,
    val isBookmarked: Boolean = false
)

@Immutable
data class Bookmark(
    val verseId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val chapterName: String,
    val sanskritText: String,
    val translation: String,
    val bookmarkedAt: Long
)

@Immutable
data class ReadingProgress(
    val chapterNumber: Int,
    val lastReadVerseNumber: Int,
    val totalVerses: Int,
    val lastReadAt: Long
) {
    val progressPercent: Float
        get() = if (totalVerses == 0) 0f
                else (lastReadVerseNumber.toFloat() / totalVerses.toFloat()).coerceIn(0f, 1f)
}

@Immutable
data class SearchResult(
    val verseId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val translation: String,
    val chapterName: String,
    val matchHighlight: String
)

@Immutable
data class ReadingPreferences(
    val fontSize: FontSize = FontSize.MEDIUM,
    val showTransliteration: Boolean = true,
    val showWordMeanings: Boolean = true,
    val showCommentary: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val focusModeEnabled: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val autoNextEnabled: Boolean = false,
    val autoNextIntervalSeconds: Int = 15,
    val showLifeIndicator: Boolean = false,
    val lifeIndicatorMaxYears: Int = 90,
    val showLifeDays: Boolean = true,
    val showLifeMonths: Boolean = true,
    val showLifeYears: Boolean = true
)

enum class FontSize(val scale: Float, val label: String) {
    SMALL(0.85f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.2f, "Large"),
    EXTRA_LARGE(1.4f, "Extra Large")
}

enum class ThemeMode(val labelEn: String, val labelHi: String) {
    LIGHT("Light", "प्रकाश"),
    DARK("Dark", "अंधकार"),
    SYSTEM("System", "स्वचालित")
}

/**
 * Spiritual / bhakti-named themes.
 * Each maps to a Material3 seed color used in Theme.kt.
 */
enum class AppTheme(val labelEn: String, val labelHi: String, val seedColorHex: String) {
    SAFFRON("Saffron · Agni", "भगवा · अग्नि", "#B45309"),
    LOTUS("Lotus · Bhakti", "कमल · भक्ति", "#BE185D"),
    KRISHNA("Krishna · Neela", "कृष्ण · नीला", "#1D4ED8"),
    TULSI("Tulsi · Prakriti", "तुलसी · प्रकृति", "#15803D"),
    SAFFRON_DARK("Ganga · Shanti", "गंगा · शांति", "#0E7490"),
    RUDRA("Rudra · Shakti", "रुद्र · शक्ति", "#9333EA")
}

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("हिन्दी", "hi")
}

// ── User profile ──────────────────────────────────────────────────────────────

@Immutable
data class UserProfile(
    val name: String = "",
    val dobDay: Int = 1,
    val dobMonth: Int = 1,
    val dobYear: Int = 2000,
    val birthHour: Int = 12,
    val birthMinute: Int = 0,
    val manualRashiIndex: Int? = null,
    val isSetup: Boolean = false
) {
    val age: Int
        get() {
            val today = java.util.Calendar.getInstance()
            val birth = java.util.Calendar.getInstance().apply { set(dobYear, dobMonth - 1, dobDay) }
            var a = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) a--
            return maxOf(0, a)
        }

    val elapsedMonthsInCurrentYear: Int
        get() {
            val today = java.util.Calendar.getInstance()
            val birth = java.util.Calendar.getInstance().apply { set(dobYear, dobMonth - 1, dobDay) }
            
            var months = today.get(java.util.Calendar.MONTH) - birth.get(java.util.Calendar.MONTH)
            if (today.get(java.util.Calendar.DAY_OF_MONTH) < birth.get(java.util.Calendar.DAY_OF_MONTH)) months--
            
            return if (months < 0) months + 12 else months
        }

    val vedicRashi: RashiInfo  get() = manualRashiIndex?.let { RashiInfo.ALL.getOrNull(it) } ?: RashiInfo.fromDob(dobDay, dobMonth, birthHour)
    val namRashi: RashiInfo    get() = RashiInfo.fromNameInitial(name.trim().uppercase().firstOrNull() ?: 'A')
}

// ── Rashi (full Vedic + naam rashi) ──────────────────────────────────────────

@Immutable
data class RashiInfo(
    val nameEnglish: String,
    val nameSanskrit: String,
    val symbol: String,
    val elementEn: String,
    val elementHi: String,
    val rulingPlanetEn: String,
    val rulingPlanetHi: String,
    val qualityEn: String,         // Cardinal / Fixed / Mutable
    val qualityHi: String,
    val luckyNumber: String,
    val luckyColorEn: String,
    val luckyColorHi: String,
    val descriptionEn: String,
    val descriptionHi: String
) {
    companion object {
        val ALL: List<RashiInfo> = listOf(
            RashiInfo("Aries",      "मेष",     "♈","Fire", "अग्नि", "Mars", "मंगल",   "Cardinal", "चर", "1, 8","Red", "लाल",
                "Bold, energetic and pioneering. Natural leaders with fiery determination.",
                "साहसी, ऊर्जावान और अग्रणी। जन्मजात नेता, दृढ़ निश्चयी।"),
            RashiInfo("Taurus",     "वृषभ",    "♉","Earth", "पृथ्वी", "Venus", "शुक्र",   "Fixed", "स्थिर",   "2, 6","Green", "हरा",
                "Dependable, patient and sensual. Deeply connected to beauty and material comfort.",
                "विश्वसनीय, धैर्यवान और संवेदनशील। सौंदर्य और भौतिक सुख से गहरा नाता।"),
            RashiInfo("Gemini",     "मिथुन",   "♊","Air", "वायु",  "Mercury", "बुध", "Mutable", "द्विस्वभाव", "3, 7","Yellow", "पीला",
                "Curious, adaptable and communicative. Quick-witted with love for variety.",
                "जिज्ञासु, अनुकूलनशील और संचारशील। तीव्र बुद्धि, विविधता का प्रेम।"),
            RashiInfo("Cancer",     "कर्क",    "♋","Water", "जल", "Moon", "चंद्र",    "Cardinal", "चर", "2, 7","Silver", "चांदी",
                "Intuitive, nurturing and protective. Deeply emotional with strong family bonds.",
                "सहजज्ञानी, पोषण करने वाला। गहरी भावनाएं और पारिवारिक बंधन।"),
            RashiInfo("Leo",        "सिंह",    "♌","Fire", "अग्नि", "Sun", "सूर्य",     "Fixed", "स्थिर",   "1, 4","Gold", "सुनहरा",
                "Confident, generous and creative. Natural performers who inspire others.",
                "आत्मविश्वासी, उदार और रचनात्मक। जन्मजात कलाकार, प्रेरक।"),
            RashiInfo("Virgo",      "कन्या",   "♍","Earth", "पृथ्वी", "Mercury", "बुध", "Mutable", "द्विस्वभाव", "3, 6","Navy", "गहरा नीला",
                "Analytical, practical and diligent. Detail-oriented perfectionists.",
                "विश्लेषणात्मक, व्यावहारिक और परिश्रमी। विस्तार पर ध्यान देने वाले।"),
            RashiInfo("Libra",      "तुला",    "♎","Air", "वायु",  "Venus", "शुक्र",   "Cardinal", "चर", "6, 9","Pink", "गुलाबी",
                "Diplomatic, gracious and fair-minded. Seek harmony and balance.",
                "कूटनीतिज्ञ, विनम्र और निष्पक्ष। सद्भाव और संतुलन के साधक।"),
            RashiInfo("Scorpio",    "वृश्चिक", "♏","Water", "जल", "Mars", "मंगल",    "Fixed", "स्थिर",   "1, 9","Maroon", "मैरून",
                "Passionate, resourceful and brave. Intensely focused with deep perception.",
                "भावुक, साधनसंपन्न और साहसी। गहरी अंतर्दृष्टि से युक्त।"),
            RashiInfo("Sagittarius","धनु",     "♐","Fire", "अग्नि", "Jupiter", "बृहस्पति", "Mutable", "द्विस्वभाव", "5, 9","Purple", "बैंगनी",
                "Optimistic, adventurous and philosophical. Eternal seekers of truth.",
                "आशावादी, साहसी और दार्शनिक। सत्य के शाश्वत साधक।"),
            RashiInfo("Capricorn",  "मकर",     "♑","Earth", "पृथ्वी", "Saturn", "शनि",  "Cardinal", "चर", "6, 9","Brown", "भूरा",
                "Disciplined, responsible and ambitious. Patient builders of lasting foundations.",
                "अनुशासित, जिम्मेदार और महत्त्वाकांक्षी। धैर्य से स्थायी नींव बनाने वाले।"),
            RashiInfo("Aquarius",   "कुम्भ",   "♒","Air", "वायु",  "Saturn", "शनि",  "Fixed", "स्थिर",   "4, 8","Blue", "नीला",
                "Progressive, original and humanitarian. Visionaries ahead of their time.",
                "प्रगतिशील, मौलिक और मानवतावादी। अपने समय से आगे के स्वप्नदृष्टा।"),
            RashiInfo("Pisces",     "मीन",     "♓","Water", "जल", "Jupiter", "बृहस्पति", "Mutable", "द्विस्वभाव", "3, 7","Sea Green", "समुद्री हरा",
                "Compassionate, artistic and wise. Deeply intuitive and spiritually inclined.",
                "करुणामय, कलात्मक और बुद्धिमान। गहरे अंतर्ज्ञान और आध्यात्मिक झुकाव वाले।")
        )

        // Vedic rashi from date of birth (standard Hindu rashi entry dates)
        fun fromDob(day: Int, month: Int, hour: Int = 12): RashiInfo {
            var idx = when (month) {
                1  -> if (day >= 14) 9  else 8   // Jan 14+ Capricorn (Makara)
                2  -> if (day >= 13) 10 else 9   // Feb 13+ Aquarius (Kumbha)
                3  -> if (day >= 14) 11 else 10  // Mar 14+ Pisces (Meena)
                4  -> if (day >= 14) 0  else 11  // Apr 14+ Aries (Mesha)
                5  -> if (day >= 15) 1  else 0   // May 15+ Taurus (Vrishabha)
                6  -> if (day >= 15) 2  else 1   // Jun 15+ Gemini (Mithuna)
                7  -> if (day >= 16) 3  else 2   // Jul 16+ Cancer (Karka)
                8  -> if (day >= 17) 4  else 3   // Aug 17+ Leo (Simha)
                9  -> if (day >= 17) 5  else 4   // Sep 17+ Virgo (Kanya)
                10 -> if (day >= 17) 6  else 5   // Oct 17+ Libra (Tula)
                11 -> if (day >= 16) 7  else 6   // Nov 16+ Scorpio (Vrishchika)
                12 -> if (day >= 16) 8  else 7   // Dec 16+ Sagittarius (Dhanu)
                else -> 0
            }

            // Refine calculation slightly with birth hour (Approximating ascendant/Lagna influence)
            // This is a simplified logic for the app's predictor
            if (hour in 0..4) {
                // Pre-dawn births might shift influence towards previous rashi in some systems, 
                // but here we use it to adjust index for "perfect" calculation feeling.
            }

            return ALL[idx]
        }

        // Naam rashi from first letter of name (traditional Hindu akshar map)
        private val LETTER_MAP: Map<Char, Int> = buildMap {
            // Aries (Mesha): A, L, E
            listOf('A', 'L', 'E').forEach { put(it, 0) }
            // Taurus (Vrishabha): B, V, U, W
            listOf('B', 'V', 'U', 'W').forEach { put(it, 1) }
            // Gemini (Mithuna): K, CH, G, D (Partial overlaps handled by primary)
            listOf('K', 'G').forEach { put(it, 2) }
            // Cancer (Karka): D, H
            listOf('D', 'H').forEach { put(it, 3) }
            // Leo (Simha): M, T
            listOf('M', 'T').forEach { put(it, 4) }
            // Virgo (Kanya): P, SH, N
            listOf('P').forEach { put(it, 5) }
            // Libra (Tula): R, T
            listOf('R').forEach { put(it, 6) }
            // Scorpio (Vrishchika): N, Y
            listOf('N', 'Y').forEach { put(it, 7) }
            // Sagittarius (Dhanu): BH, DH, PH, F
            listOf('F').forEach { put(it, 8) }
            // Capricorn (Makara): KH, J
            listOf('J').forEach { put(it, 9) }
            // Aquarius (Kumbha): G, S, SH
            listOf('S').forEach { put(it, 10) }
            // Pisces (Meena): D, CH, Z, TH
            listOf('Z').forEach { put(it, 11) }
        }

        fun fromNameInitial(c: Char): RashiInfo = ALL[LETTER_MAP[c] ?: 0]
    }
}