package com.aana.aegislink.api

import org.json.JSONObject

object VtResultParser {
    fun parse(resultJson: String): VtVerdict? {
        val stats = JSONObject(resultJson)
            .optJSONObject("data")
            ?.optJSONObject("attributes")
            ?.optJSONObject("stats")
            ?: return null

        return VtVerdict(
            malicious = stats.optInt("malicious", 0),
            suspicious = stats.optInt("suspicious", 0),
            harmless = stats.optInt("harmless", 0)
        )
    }
}
