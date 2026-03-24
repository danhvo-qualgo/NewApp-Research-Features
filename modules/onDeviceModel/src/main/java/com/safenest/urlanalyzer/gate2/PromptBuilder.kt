/*
 * PromptBuilder.kt — Builds system + user prompts for the SLM.
 *
 * Mirrors iOS PromptBuilder.swift and Python gate2/prompt_builder.py.
 * Loads gate2_prompts.json templates.
 */

package com.safenest.urlanalyzer.gate2

import android.content.Context
import com.safenest.urlanalyzer.*
import org.json.JSONObject

class PromptBuilder(context: Context) {

    private val systemPrompt: String
    private val userTemplate: String

    init {
        val jsonStr = context.assets.open("gate2_prompts.json")
            .bufferedReader().use { it.readText() }
        val json = JSONObject(jsonStr)
        systemPrompt = json.getString("system_prompt")
        userTemplate = json.getString("user_prompt_template")
    }

    fun buildSystemPrompt(): String = systemPrompt

    fun buildUserPrompt(
        url: String,
        gate1Result: Gate1Result,
        signals: List<Gate2Signal>,
        analyzerData: AnalyzerData
    ): String {
        return userTemplate
            .replace("{url}", url)
            .replace("{gate1_verdict}", gate1Result.verdict)
            .replace("{gate1_confidence}", "%.2f".format(gate1Result.riskScore))
            .replace("{signals_section}", buildSignalsSection(signals))
            .replace("{typosquat_section}", buildTyposquatSection(analyzerData.typosquat))
            .replace("{homograph_section}", buildHomographSection(analyzerData.homograph))
            .replace("{ssl_section}", buildSSLSection(analyzerData.ssl))
            .replace("{scamdb_section}", buildScamDBSection(analyzerData.scamDB))
            .replace("{pageinfo_section}", buildPageInfoSection(analyzerData.pageInfo))
    }

    // MARK: - Section builders

    private fun buildSignalsSection(signals: List<Gate2Signal>): String {
        if (signals.isEmpty()) return "No significant scam signals detected."
        return signals.joinToString("\n") {
            "- [${it.severity.uppercase()}] ${it.signalName}: ${it.description}"
        }
    }

    private fun buildTyposquatSection(data: Map<String, Any?>): String {
        val lines = mutableListOf("- isTyposquat: ${data["isTyposquat"] ?: false}")
        if (data["isTyposquat"] == true) {
            lines.add("- matchedDomain: ${data["matchedDomain"] ?: ""}")
            lines.add("- matchedBrand: ${data["matchedBrand"] ?: ""}")
            lines.add("- distance: ${data["distance"] ?: 0}")
            (data["score"] as? Double)?.let {
                lines.add("- score: ${"%.2f".format(it)}")
            }
        }
        return lines.joinToString("\n")
    }

    private fun buildHomographSection(data: Map<String, Any?>): String {
        val lines = mutableListOf(
            "- isHomograph: ${data["isHomograph"] ?: false}",
            "- isIDN: ${data["isIDN"] ?: false}",
            "- hasMixedScripts: ${data["hasMixedScripts"] ?: false}"
        )
        (data["punycode"] as? String)?.takeIf { it.isNotEmpty() }?.let {
            lines.add("- punycode: $it")
        }
        (data["score"] as? Double)?.let {
            lines.add("- score: ${"%.2f".format(it)}")
        }
        @Suppress("UNCHECKED_CAST")
        (data["confusableChars"] as? List<Map<String, String>>)?.takeIf { it.isNotEmpty() }?.let { chars ->
            lines.add("- confusableChars:")
            for (c in chars) {
                lines.add("  - char: ${c["char"] ?: ""}, looksLike: ${c["looksLike"] ?: ""}, " +
                    "script: ${c["script"] ?: ""}, unicode: ${c["unicode"] ?: ""}")
            }
        }
        return lines.joinToString("\n")
    }

    private fun buildSSLSection(data: Map<String, Any?>): String {
        val lines = mutableListOf("- valid: ${data["valid"] ?: false}")
        if (data["valid"] == true) {
            lines.add("- issuer: ${data["issuer"] ?: ""}")
            lines.add("- protocol: ${data["protocol"] ?: ""}")
            lines.add("- expiresAt: ${data["expiresAt"] ?: ""}")
            lines.add("- daysUntilExpiry: ${data["daysUntilExpiry"] ?: 0}")
            @Suppress("UNCHECKED_CAST")
            (data["subjectAltNames"] as? List<String>)?.takeIf { it.isNotEmpty() }?.let {
                lines.add("- subjectAltNames: ${it.joinToString(", ")}")
            }
        }
        return lines.joinToString("\n")
    }

    private fun buildScamDBSection(data: Map<String, Any?>): String {
        return "- found: ${data["found"] ?: false}"
    }

    private fun buildPageInfoSection(data: Map<String, Any?>): String {
        val lines = mutableListOf("- reachable: ${data["reachable"] ?: false}")
        if (data["reachable"] == true) {
            lines.add("- statusCode: ${data["statusCode"] ?: 0}")
            lines.add("- finalUrl: ${data["finalUrl"] ?: ""}")
            lines.add("- title: ${data["title"] ?: ""}")
            lines.add("- description: ${data["description"] ?: ""}")
        }
        return lines.joinToString("\n")
    }
}
