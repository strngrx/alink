package com.aana.aegislink.api

class VtRepository(
    private val service: VirusTotalService,
    private val cacheStore: VtCacheStore
) {
    suspend fun getOrScan(url: String, apiKey: String, nowMillis: Long): VtVerdict? {
        val cached = cacheStore.getCached(url, nowMillis, TTL_MILLIS)
        if (!cached.isNullOrBlank()) {
            return VtResultParser.parse(cached)
        }

        val resultJson = service.scanUrl(apiKey, url) ?: return null
        cacheStore.put(url, resultJson, nowMillis)
        return VtResultParser.parse(resultJson)
    }

    companion object {
        private const val TTL_MILLIS = 24 * 60 * 60 * 1000L
    }
}
