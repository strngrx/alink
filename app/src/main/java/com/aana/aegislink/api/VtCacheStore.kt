package com.aana.aegislink.api

import com.aana.aegislink.db.VtCacheDao
import com.aana.aegislink.db.VtCacheEntity
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class VtCacheStore(
    private val dao: VtCacheDao
) {
    suspend fun getCached(url: String, nowMillis: Long, ttlMillis: Long): String? {
        val hash = hashUrl(url)
        val timestamp = dao.getTimestamp(hash) ?: return null
        if (nowMillis - timestamp > ttlMillis) {
            dao.prune(nowMillis - ttlMillis)
            return null
        }
        return dao.getResult(hash)
    }

    suspend fun put(url: String, resultJson: String, nowMillis: Long) {
        dao.upsert(
            VtCacheEntity(
                urlHash = hashUrl(url),
                scanResult = resultJson,
                timestamp = nowMillis
            )
        )
    }

    private fun hashUrl(url: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray(StandardCharsets.UTF_8))
        val sb = StringBuilder(digest.size * 2)
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}
