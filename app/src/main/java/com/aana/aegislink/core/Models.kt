package com.aana.aegislink.core

data class SanitizerOutcome(
    val url: String,
    val trustSignal: Boolean
)

enum class Classification {
    BLACKLISTED,
    TRUSTED,
    WHITELISTED,
    UNKNOWN
}

data class PipelineResult(
    val originalUrl: String,
    val normalizedUrl: String,
    val sanitizedUrl: String,
    val domain: String?,
    val trustSignal: Boolean,
    val classification: Classification
)
