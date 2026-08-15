package com.brainwallet.localisation

import org.junit.Test
import java.io.File
import kotlin.test.fail

/**
 * Verifies every strings.xml under app/src/main/res (the default `values/` plus every
 * locale/qualifier `values-` folder, e.g. `values-fr`) has correctly escaped apostrophes
 * and double quotes.
 *
 * An unescaped `'` or `"` inside a string value is perfectly valid XML - it parses fine, so
 * a plain XML-well-formedness check (like DocumentBuilder, used by
 * [StringsLocalisationCoverageTest]) won't catch it - but it fails aapt2's *resource*
 * compile with e.g. "error: apostrophe not preceded by \\" the moment a real Gradle build
 * runs. Translators/contributors hit this constantly with contractions ("don't", "can't")
 * and languages that use apostrophes as part of normal grammar (French "l'adresse", "n'est",
 * Italian "dell'indirizzo", etc.) - this test catches it here instead of at build time, or
 * worse, in CI on a PR nobody expected to touch native/build config.
 *
 * Escaping/quoting rules modeled, per
 * https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes:
 *  - Every *unescaped* `"` toggles whether the following characters are inside a quoted
 *    span (Android allows quoting to be switched on/off mid-value, e.g. to preserve
 *    whitespace around an inline `<b>` tag - this codebase actually does that, e.g.
 *    `"Just for you. \n It is the <b>private key</b> that lets you send."` - so a value
 *    isn't simply "quoted" or "not quoted" as a whole).
 *  - A literal `'` must be escaped as `\'` whenever it occurs *outside* a quoted span.
 *    Inside a quoted span it's taken literally and needs no escaping.
 *  - If a value ends with an odd number of unescaped `"` (i.e. still "inside quotes" when
 *    the value runs out), that's an unterminated quoted span - always a bug.
 *  - An escaped quote (`\"`) is always just a literal `"` character - it never toggles the
 *    quoted-span state.
 *
 * Deliberately scans the *raw* file text via regex rather than a value pulled out of a DOM
 * tree, so it can't be fooled by XML entities like `&apos;`/`&quot;` having already been
 * decoded into bare characters by an XML parser before this test ever sees them.
 */
class StringsXmlEscapingTest {

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

        private val COMMENT_REGEX = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)

        // Self-closing <string .../> entries (e.g. a deliberately empty/blank string) have
        // no body to check - and critically, must be stripped out *before* the paired-tag
        // regex below runs, or that regex's lazy match will skip right past the self-closing
        // "/>" looking for a "</string>" that isn't there, and instead swallow everything up
        // to the *next* unrelated string's closing tag as if it belonged to this one.
        private val SELF_CLOSING_STRING_REGEX = Regex("""<string\s[^>]*/>""")

        private val STRING_REGEX = Regex(
            """<string\s+[^>]*?name="([^"]+)"[^>]*>(.*?)</string>""",
            RegexOption.DOT_MATCHES_ALL
        )
        private val ITEM_REGEX = Regex(
            """<item(?:\s[^>]*)?>(.*?)</item>""",
            RegexOption.DOT_MATCHES_ALL
        )
    }

    @Test
    fun `every string value has properly escaped apostrophes and quotes`() {
        val stringsFiles = discoverStringsFiles()
        check(stringsFiles.isNotEmpty()) {
            "No strings.xml files found under ${RES_DIR.absolutePath} - check the search path"
        }

        val failures = stringsFiles.flatMap { checkFile(it) }

        if (failures.isNotEmpty()) {
            fail(
                "${failures.size} string resource(s) have an unescaped apostrophe or an " +
                    "unterminated quoted span (escape with \\' / \\\", or balance the quotes):\n" +
                    failures.joinToString("\n") { "  - $it" }
            )
        }
    }

    private fun discoverStringsFiles(): List<File> =
        RES_DIR.listFiles()
            .orEmpty()
            .filter { it.isDirectory && (it.name == "values" || it.name.startsWith("values-")) }
            .mapNotNull { dir -> File(dir, "strings.xml").takeIf(File::exists) }
            .sortedBy { it.path }

    private fun checkFile(file: File): List<String> {
        val withoutComments = COMMENT_REGEX.replace(file.readText(), "")
        val withoutSelfClosing = SELF_CLOSING_STRING_REGEX.replace(withoutComments, "")
        val violations = mutableListOf<String>()

        STRING_REGEX.findAll(withoutSelfClosing).forEach { match ->
            val name = match.groupValues[1]
            val content = match.groupValues[2]
            findEscapingIssues(content).forEach { issue ->
                violations += "${file.path}: string \"$name\" - $issue"
            }
        }

        ITEM_REGEX.findAll(withoutSelfClosing).forEach { match ->
            findEscapingIssues(match.groupValues[1]).forEach { issue ->
                violations += "${file.path}: <item> - $issue"
            }
        }

        return violations
    }

    /**
     * Scans a single raw (still XML-entity-encoded) string value for apostrophes that
     * needed escaping and weren't, and for unterminated quoted spans, per the class doc.
     */
    private fun findEscapingIssues(rawContent: String): List<String> {
        val issues = mutableListOf<String>()
        var consecutiveBackslashes = 0
        var insideQuotedSpan = false

        rawContent.forEachIndexed { index, c ->
            if (c == '\\') {
                consecutiveBackslashes++
                return@forEachIndexed
            }

            val isEscaped = consecutiveBackslashes % 2 == 1
            consecutiveBackslashes = 0

            when (c) {
                '"' -> if (!isEscaped) {
                    insideQuotedSpan = !insideQuotedSpan
                }
                '\'' -> if (!isEscaped && !insideQuotedSpan) {
                    issues += "unescaped apostrophe (') at offset $index in \"${rawContent.trim()}\""
                }
            }
        }

        if (insideQuotedSpan) {
            issues += "unterminated quoted span (odd number of unescaped \") in \"${rawContent.trim()}\""
        }

        return issues
    }
}
