package com.brainwallet.tools.manager

import org.junit.Test
import java.io.File

/**
 * Informational test that scans app/src/main for every `AnalyticsManager.log*` call site
 * (logCustomEvent / logCustomEventWithParams / logCustomAdHocEvent) and reports:
 *   - The total number of call sites
 *   - Each placement (file:line, method, event name/expression)
 *   - A count of call sites grouped by resolved event name
 *   - A count of call sites grouped by method
 *
 * This test NEVER fails on the audit contents — it is purely diagnostic, so adding, removing,
 * or renaming an analytics event elsewhere in the app does not break CI. It only fails if the
 * scan itself can't run (e.g. the source tree moved and no call sites were found at all), which
 * would mean the audit is broken, not that analytics events changed.
 *
 * Usage:
 *   ./gradlew :app:testDebugUnitTest --tests "*.AnalyticsEventAuditTest"
 */
class AnalyticsEventAuditTest {

    companion object {
        private val SRC_MAIN_JAVA_DIR: File by lazy {
            val candidates = listOf(
                File("src/main/java"),
                File("app/src/main/java"),
            )
            candidates.firstOrNull { it.isDirectory }
                ?: error(
                    "Cannot locate app/src/main/java. " +
                        "Searched: ${candidates.map { it.absolutePath }}"
                )
        }

        private val CALL_SITE_PATTERN = Regex(
            """AnalyticsManager\.(logCustomEvent|logCustomEventWithParams|logCustomAdHocEvent)\s*\(\s*([^,)]+)"""
        )
    }

    private data class EventPlacement(
        val displayPath: String,
        val line: Int,
        val method: String,
        val eventExpression: String,
    )

    @Test
    fun `print AnalyticsManager log call site audit`() {
        val placements = scanForCallSites()
        check(placements.isNotEmpty()) {
            "No AnalyticsManager.log* call sites found under ${SRC_MAIN_JAVA_DIR.absolutePath} " +
                "— the audit scan is likely broken (source layout changed?), not that analytics calls vanished."
        }

        println(buildReport(placements))
    }

    private fun scanForCallSites(): List<EventPlacement> {
        val placements = mutableListOf<EventPlacement>()

        SRC_MAIN_JAVA_DIR.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                val displayPath = "app/src/main/java/" +
                    file.relativeTo(SRC_MAIN_JAVA_DIR).path.replace(File.separatorChar, '/')

                file.readLines().forEachIndexed { index, rawLine ->
                    val trimmed = rawLine.trimStart()
                    if (trimmed.startsWith("//") || trimmed.startsWith("*")) return@forEachIndexed

                    val match = CALL_SITE_PATTERN.find(rawLine) ?: return@forEachIndexed
                    val (method, eventExpression) = match.destructured
                    placements += EventPlacement(
                        displayPath = displayPath,
                        line = index + 1,
                        method = method,
                        eventExpression = eventExpression.trim(),
                    )
                }
            }

        return placements
    }

    private fun buildReport(placements: List<EventPlacement>): String = buildString {
        val divider = "=".repeat(100)

        appendLine(divider)
        appendLine("AnalyticsManager.log* call-site audit — ${placements.size} total placements")
        appendLine(divider)
        placements
            .sortedWith(compareBy({ it.displayPath }, { it.line }))
            .forEach {
                appendLine("${it.displayPath}:${it.line}  [${it.method}]  ${it.eventExpression}")
            }

        appendLine()
        appendLine("-- By event name/expression " + "-".repeat(70))
        placements.groupBy { it.eventExpression }
            .toSortedMap()
            .forEach { (event, sites) ->
                appendLine("  ${sites.size.toString().padStart(2)}x  $event")
                sites.forEach { appendLine("        ${it.displayPath}:${it.line}") }
            }

        appendLine()
        appendLine("-- By method " + "-".repeat(85))
        placements.groupBy { it.method }
            .toSortedMap()
            .forEach { (method, sites) ->
                appendLine("  ${sites.size.toString().padStart(2)}x  $method")
            }

        appendLine()
        appendLine("Distinct event names/expressions: ${placements.map { it.eventExpression }.distinct().size}")
        appendLine(divider)
    }
}
