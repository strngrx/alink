package com.aana.aegislink.core

import android.content.Context
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicReference

class ClearUrlsRuleEngine(
    context: Context,
    private val referralMarketingFlag: ReferralMarketingFlag,
    private val assetName: String = "data.minify.json"
) : RuleEngine {
    private val appContext = context.applicationContext
    private val cachedCatalog = AtomicReference<RuleCatalog?>(null)

    override suspend fun apply(url: String): SanitizerOutcome {
        val catalog = loadCatalog()
        var current = url
        var trustSignal = false

        for (provider in catalog.providers) {
            if (!provider.urlPattern.containsMatchIn(current)) continue
            if (provider.exceptions.any { it.containsMatchIn(current) }) {
                return SanitizerOutcome(url = current, trustSignal = true)
            }

            val redirected = provider.applyRedirectionChain(current)
            if (redirected != current) {
                current = redirected
                trustSignal = true
            }

            val rawApplied = provider.applyRawRules(current)
            if (rawApplied != current) {
                current = rawApplied
                trustSignal = true
            }

            val cleaned = provider.applyQueryRules(
                current,
                allowReferral = referralMarketingFlag.isAllowed()
            )
            if (cleaned != current) {
                current = cleaned
                trustSignal = true
            }
        }

        return SanitizerOutcome(url = current, trustSignal = trustSignal)
    }

    private fun loadCatalog(): RuleCatalog {
        val cached = cachedCatalog.get()
        if (cached != null) return cached

        val json = appContext.assets.open(assetName).bufferedReader().use { it.readText() }
        val catalog = RuleCatalog.fromJson(json)
        cachedCatalog.compareAndSet(null, catalog)
        return cachedCatalog.get() ?: catalog
    }
}

interface ReferralMarketingFlag {
    suspend fun isAllowed(): Boolean
}

class ReferralMarketingDisabled : ReferralMarketingFlag {
    override suspend fun isAllowed(): Boolean = false
}

private data class RuleCatalog(
    val providers: List<ProviderRuleSet>
) {
    companion object {
        fun fromJson(json: String): RuleCatalog {
            val root = JSONObject(json)
            val providersObject = root.optJSONObject("providers") ?: JSONObject()
            val providers = mutableListOf<ProviderRuleSet>()

            val providerNames = providersObject.keys()
            while (providerNames.hasNext()) {
                val name = providerNames.next()
                val providerJson = providersObject.optJSONObject(name) ?: continue
                providers.add(ProviderRuleSet.fromJson(name, providerJson))
            }

            return RuleCatalog(providers)
        }
    }
}

private data class ProviderRuleSet(
    val name: String,
    val urlPattern: Regex,
    val rules: List<Regex>,
    val rawRules: List<Regex>,
    val referralMarketing: List<Regex>,
    val exceptions: List<Regex>,
    val redirections: List<Regex>
) {
    fun applyRedirectionChain(url: String): String {
        var current = url
        var changed = true
        var guard = 0
        while (changed && guard < 5) {
            guard += 1
            changed = false
            for (rule in redirections) {
                val match = rule.find(current) ?: continue
                val group = match.groupValues.getOrNull(1) ?: continue
                if (group.isBlank()) continue
                val decoded = runCatching { URLDecoder.decode(group, "UTF-8") }.getOrDefault(current)
                if (decoded != current) {
                    current = decoded
                    changed = true
                    break
                }
            }
        }
        return current
    }

    fun applyRawRules(url: String): String {
        var current = url
        for (rule in rawRules) {
            current = rule.replace(current, "")
        }
        return current
    }

    fun applyQueryRules(url: String, allowReferral: Boolean): String {
        val httpUrl = url.toHttpUrlOrNull() ?: return url
        val builder = httpUrl.newBuilder()
        builder.query(null)

        val total = httpUrl.querySize
        var changed = false

        for (i in 0 until total) {
            val name = httpUrl.queryParameterName(i)
            val value = httpUrl.queryParameterValue(i)
            val remove = matchesAny(name, rules) ||
                (!allowReferral && matchesAny(name, referralMarketing))
            if (remove) {
                changed = true
            } else {
                builder.addQueryParameter(name, value)
            }
        }

        return if (changed) builder.build().toString() else url
    }

    private fun matchesAny(value: String, patterns: List<Regex>): Boolean {
        for (regex in patterns) {
            if (regex.matches(value)) return true
        }
        return false
    }

    companion object {
        fun fromJson(name: String, json: JSONObject): ProviderRuleSet {
            val urlPattern = json.optString("urlPattern").toRegex(RegexOption.IGNORE_CASE)
            val rules = regexList(json.optJSONArray("rules"))
            val rawRules = regexList(json.optJSONArray("rawRules"))
            val referralMarketing = regexList(json.optJSONArray("referralMarketing"))
            val exceptions = regexList(json.optJSONArray("exceptions"))
            val redirections = regexList(json.optJSONArray("redirections"))

            return ProviderRuleSet(
                name = name,
                urlPattern = urlPattern,
                rules = rules,
                rawRules = rawRules,
                referralMarketing = referralMarketing,
                exceptions = exceptions,
                redirections = redirections
            )
        }

        private fun regexList(array: JSONArray?): List<Regex> {
            if (array == null) return emptyList()
            val list = ArrayList<Regex>(array.length())
            for (i in 0 until array.length()) {
                val raw = array.optString(i)
                if (raw.isNullOrBlank()) continue
                list.add(raw.toRegex(RegexOption.IGNORE_CASE))
            }
            return list
        }
    }
}
