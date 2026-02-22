package com.aana.aegislink.core

import android.net.Uri

class UrlNormalizer {
    fun normalize(input: String): String {
        val decodedInput = Uri.decode(input)
        val parsed = Uri.parse(decodedInput)
        val scheme = parsed.scheme?.lowercase()
        val host = parsed.host?.lowercase()
        val port = parsed.port
        val path = parsed.path?.trimEnd('/') ?: ""
        val query = parsed.encodedQuery

        val rebuilt = Uri.Builder().apply {
            if (!scheme.isNullOrBlank()) scheme(scheme)
            if (!host.isNullOrBlank()) {
                authority(
                    if (port == -1) host else "$host:$port"
                )
            }
            if (path.isNotEmpty()) {
                encodedPath(path)
            }
            if (!query.isNullOrBlank()) {
                encodedQuery(query)
            }
        }.build()

        return rebuilt.toString()
    }
}
