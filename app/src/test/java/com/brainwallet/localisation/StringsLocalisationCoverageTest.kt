package com.brainwallet.localisation

import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.roundToInt

/**
 * Informational test that compares strings.xml coverage across all locale resource folders.
 *
 * For each values-XX folder found under app/src/main/res it reports:
 *   - How many of the English (values/) keys are present
 *   - The coverage percentage
 *   - Which keys are missing (printed at the end for each locale)
 *
 * This test NEVER fails — it is purely diagnostic. Run it and read the console output.
 *
 * Usage:
 *   ./gradlew :app:testDebugUnitTest --tests "*.StringsLocalisationCoverageTest"
 */
class StringsLocalisationCoverageTest {

    companion object {
        private val RES_DIR: File by lazy {
            val candidates = listOf(
                File("src/main/res"),
                File("app/src/main/res"),
            )
            candidates.firstOrNull { it.isDirectory }
                ?: error(
                    "Cannot locate app/src/main/res. " +
                        "Searched: ${candidates.map { it.absolutePath }}"
                )
        }

        private val EXCLUDED_DIRS = setOf(
            "values-land",
            "values-night",
            "values-w600dp",
            "values-w1240dp",
            "values-h700dp"
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // Main test entry point
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun `print localisation coverage report`() {
        val englishKeys = parseStringKeys(File(RES_DIR, "values/strings.xml"))
        check(englishKeys.isNotEmpty()) {
            "English strings.xml is empty or missing — check path ${RES_DIR.absolutePath}/values/strings.xml"
        }

        val localeResults = discoverLocaleFiles()
            .map { (locale, file) ->
                val localeKeys = parseStringKeys(file)
                val present = englishKeys.intersect(localeKeys).size
                val missing = englishKeys - localeKeys
                val extra = localeKeys - englishKeys // keys the locale has but English doesn't
                LocaleReport(
                    locale = locale,
                    file = file,
                    total = englishKeys.size,
                    present = present,
                    missing = missing.sorted(),
                    extra = extra.sorted(),
                )
            }
            .sortedByDescending { it.coveragePct }

        printReport(englishKeys, localeResults)

        // The test itself is informational — it never asserts failure.
        // Remove the comment below to turn it into a real CI gate:
        // localeResults.forEach { assertTrue("${it.locale} coverage below 80%", it.coveragePct >= 80.0) }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────

    /** Parse all <string name="..."> keys from a strings.xml file. */
    private fun parseStringKeys(file: File): Set<String> {
        if (!file.exists()) return emptySet()
        return try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val nodes = doc.getElementsByTagName("string")
            (0 until nodes.length)
                .map { nodes.item(it) as Element }
                .mapNotNull { it.getAttribute("name").takeIf { n -> n.isNotBlank() } }
                .toSet()
        } catch (e: Exception) {
            System.err.println("  ⚠ Could not parse ${file.path}: ${e.message}")
            emptySet()
        }
    }

    /**
     * Find every values-XX/strings.xml under RES_DIR.
     * Returns pairs of (locale-suffix, file), e.g. ("ar", File("…/values-ar/strings.xml")).
     */
    private fun discoverLocaleFiles(): List<Pair<String, File>> =
        RES_DIR.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") && it.name !in EXCLUDED_DIRS }
            .mapNotNull { dir ->
                val locale = dir.name.removePrefix("values-")
                val stringsFile = File(dir, "strings.xml")
                if (stringsFile.exists()) locale to stringsFile else null
            }
            .sortedBy { it.first }

    /** Pretty-print the full coverage report to stdout. */
    private fun printReport(englishKeys: Set<String>, results: List<LocaleReport>) {
        val divider = "─".repeat(72)

        println()
        println("╔══════════════════════════════════════════════════════════════════════╗")
        println("║          BRAINWALLET  ·  Strings Localisation Coverage Report        ║")
        println("╚══════════════════════════════════════════════════════════════════════╝")
        println()
        println("  English baseline:  ${englishKeys.size} keys")
        println("  Locales found:     ${results.size}")
        println()
        println("  %-12s  %6s / %-6s  %7s  %s".format("Locale", "Keys", "Total", "Coverage", "Status"))
        println("  $divider")

        results.forEach { r ->
            val bar = progressBar(r.coveragePct, width = 20)
            val status = when {
                r.file.length() == 0L -> "⚠ EMPTY FILE"
                r.present == 0 -> "✗ NO MATCHING KEYS"
                r.coveragePct >= 95 -> "✓ Complete"
                r.coveragePct >= 75 -> "△ Partial"
                else -> "✗ Incomplete"
            }
            println(
                "  %-12s  %6d / %-6d  %6.1f%%  [%s] %s".format(
                    r.locale,
                    r.present,
                    r.total,
                    r.coveragePct,
                    bar,
                    status
                )
            )
        }

        println()
        println("  $divider")
        println()

        // Detail section — missing keys per locale
        val withMissing = results.filter { it.missing.isNotEmpty() }
        if (withMissing.isEmpty()) {
            println("  🎉 All locales have 100% key coverage!")
        } else {
            println("  Missing keys by locale")
            println("  (keys present in English but absent from the locale file)")
            println()
            withMissing.forEach { r ->
                println("  ┌── ${r.locale}  (${r.missing.size} missing, ${r.coveragePct.roundToInt()}% covered)")
                r.missing.forEach { key -> println("  │   $key") }
                println()
            }
        }

        // Extra keys section (locale has keys not in English)
        val withExtra = results.filter { it.extra.isNotEmpty() }
        if (withExtra.isNotEmpty()) {
            println()
            println("  Keys present in locale but NOT in English (stale / untranslated additions)")
            println()
            withExtra.forEach { r ->
                println("  ┌── ${r.locale}  (${r.extra.size} extra)")
                r.extra.forEach { key -> println("  │   $key") }
                println()
            }
        }

        println()
    }

    private fun progressBar(pct: Double, width: Int): String {
        val filled = ((pct / 100.0) * width).roundToInt().coerceIn(0, width)
        return "█".repeat(filled) + "░".repeat(width - filled)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Data
    // ──────────────────────────────────────────────────────────────────────

    private data class LocaleReport(
        val locale: String,
        val file: File,
        val total: Int,
        val present: Int,
        val missing: List<String>,
        val extra: List<String>,
    ) {
        val coveragePct: Double get() = if (total == 0) 0.0 else (present.toDouble() / total) * 100.0
    }
}
