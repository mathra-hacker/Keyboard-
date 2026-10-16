package ime.suggest

/**
 * Very small language detector based on Unicode ranges.
 */
object LanguageDetector {
    enum class Language { ENGLISH, SINHALA, UNKNOWN }

    fun detectLanguage(text: String): Language {
        if (text.isEmpty()) return Language.UNKNOWN
        // If any character in Sinhala block U+0D80..U+0DFF is present, treat as Sinhala
        for (ch in text) {
            val code = ch.code
            if (code in 0x0D80..0x0DFF) return Language.SINHALA
            // Strictly A-Z (65-90) and a-z (97-122)
            if (code in 0x0041..0x005A || code in 0x0061..0x007A) return Language.ENGLISH
        }
        return Language.UNKNOWN
    }
}
