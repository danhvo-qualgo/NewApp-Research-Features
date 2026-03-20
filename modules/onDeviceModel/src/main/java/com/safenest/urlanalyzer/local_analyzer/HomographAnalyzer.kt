/*
 * HomographAnalyzer.kt — Unicode confusable detection.
 *
 * Mirrors iOS HomographAnalyzer.swift and Python local_analyzer/homograph.py.
 */

package com.safenest.urlanalyzer.local_analyzer

import java.net.IDN

object HomographAnalyzer {

    // Confusables map — matches iOS confusables
    private data class Confusable(val looksLike: String, val script: String)

    private val confusables: Map<Int, Confusable> = mapOf(
        // Cyrillic
        0x0430 to Confusable("a", "Cyrillic"),
        0x0435 to Confusable("e", "Cyrillic"),
        0x043E to Confusable("o", "Cyrillic"),
        0x0440 to Confusable("p", "Cyrillic"),
        0x0441 to Confusable("c", "Cyrillic"),
        0x0443 to Confusable("y", "Cyrillic"),
        0x0445 to Confusable("x", "Cyrillic"),
        0x0455 to Confusable("s", "Cyrillic"),
        0x0456 to Confusable("i", "Cyrillic"),
        0x0458 to Confusable("j", "Cyrillic"),
        0x04BB to Confusable("h", "Cyrillic"),
        0x0501 to Confusable("d", "Cyrillic"),
        0x051B to Confusable("q", "Cyrillic"),
        0x051D to Confusable("w", "Cyrillic"),
        // Greek
        0x03B1 to Confusable("a", "Greek"),
        0x03BF to Confusable("o", "Greek"),
        0x03B5 to Confusable("e", "Greek"),
        0x03BA to Confusable("k", "Greek"),
        0x03BD to Confusable("v", "Greek"),
        0x03C1 to Confusable("p", "Greek"),
        0x03C4 to Confusable("t", "Greek"),
        0x03C5 to Confusable("u", "Greek"),
        0x03C9 to Confusable("w", "Greek"),
        // Latin Extended
        0x0131 to Confusable("i", "Latin Extended"),
        0x1E05 to Confusable("b", "Latin Extended"),
        0x1E37 to Confusable("l", "Latin Extended"),
        0x1E43 to Confusable("m", "Latin Extended"),
        0x1E47 to Confusable("n", "Latin Extended"),
        0x0111 to Confusable("d", "Latin Extended"),
        0x0142 to Confusable("l", "Latin Extended")
    )

    fun analyze(domain: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "isHomograph" to false,
            "score" to 0.0,
            "confusableChars" to emptyList<Map<String, String>>(),
            "hasMixedScripts" to false,
            "isIDN" to false,
            "punycode" to ""
        )

        if (domain.isEmpty()) return result

        val labels = domain.split(".")

        // Check IDN / punycode
        if (domain.contains("xn--")) {
            result["isIDN"] = true
            result["punycode"] = domain
        }
        // Check for non-ASCII characters
        if (domain.any { it.code > 127 }) {
            result["isIDN"] = true
            // Convert to punycode via IDN class
            try {
                val ascii = IDN.toASCII(domain)
                if (ascii != domain) {
                    result["punycode"] = ascii
                }
            } catch (_: Exception) {}
        }

        // Analyze characters without TLD
        val domainWithoutTLD = if (labels.size > 1) {
            labels.dropLast(1).joinToString(".")
        } else {
            labels[0]
        }

        var totalChars = 0
        val confusableCharsFound = mutableListOf<Map<String, String>>()
        val scripts = mutableSetOf<String>()

        for (codePoint in domainWithoutTLD.codePoints().toArray()) {
            if (codePoint == '.'.code) continue
            totalChars++

            // Track scripts
            val ch = codePoint.toChar()
            when {
                ch.isLetter() && codePoint < 128 -> scripts.add("Latin")
                codePoint in 0x0400..0x052F -> scripts.add("Cyrillic")
                codePoint in 0x0370..0x03FF -> scripts.add("Greek")
            }

            confusables[codePoint]?.let { entry ->
                confusableCharsFound.add(
                    mapOf(
                        "char" to String(Character.toChars(codePoint)),
                        "unicode" to "U+%04X".format(codePoint),
                        "looksLike" to entry.looksLike,
                        "script" to entry.script
                    )
                )
            }
        }

        result["confusableChars"] = confusableCharsFound
        result["hasMixedScripts"] = scripts.size > 1

        if (totalChars > 0 && confusableCharsFound.isNotEmpty()) {
            result["isHomograph"] = true
            result["score"] = confusableCharsFound.size.toDouble() / totalChars.toDouble()
        }

        return result
    }
}
