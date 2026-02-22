package com.aana.aegislink.core

class UrlSanitizer(
    private val ruleEngine: RuleEngine
) {
    suspend fun sanitize(input: String): SanitizerOutcome {
        return ruleEngine.apply(input)
    }
}

interface RuleEngine {
    suspend fun apply(url: String): SanitizerOutcome
}

class NoOpRuleEngine : RuleEngine {
    override suspend fun apply(url: String): SanitizerOutcome {
        return SanitizerOutcome(url = url, trustSignal = false)
    }
}

class ToggleableRuleEngine(
    private val delegate: RuleEngine,
    private val isEnabled: suspend () -> Boolean
) : RuleEngine {
    override suspend fun apply(url: String): SanitizerOutcome {
        return if (isEnabled()) delegate.apply(url) else SanitizerOutcome(url, false)
    }
}
