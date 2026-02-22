package com.aana.aegislink.core

import android.net.Uri

class UrlClassifier(
    private val blacklist: DomainLookup,
    private val whitelist: DomainLookup,
    private val instantTrustEnabled: InstantTrustFlag
) {
    suspend fun classify(url: String, trustSignal: Boolean): Classification {
        val domain = Uri.parse(url).host?.lowercase()
        if (domain.isNullOrBlank()) return Classification.UNKNOWN

        if (blacklist.containsDomain(domain)) {
            return Classification.BLACKLISTED
        }

        if (trustSignal && instantTrustEnabled.isEnabled()) {
            return Classification.TRUSTED
        }

        if (whitelist.containsDomain(domain)) {
            return Classification.WHITELISTED
        }

        return Classification.UNKNOWN
    }
}

class DaoDomainLookup(
    private val contains: suspend (String) -> Boolean
) : DomainLookup {
    override suspend fun containsDomain(domain: String): Boolean = contains(domain)
}

interface DomainLookup {
    suspend fun containsDomain(domain: String): Boolean
}

interface InstantTrustFlag {
    suspend fun isEnabled(): Boolean
}

class EmptyDomainLookup : DomainLookup {
    override suspend fun containsDomain(domain: String): Boolean = false
}

class DisabledInstantTrust : InstantTrustFlag {
    override suspend fun isEnabled(): Boolean = false
}
