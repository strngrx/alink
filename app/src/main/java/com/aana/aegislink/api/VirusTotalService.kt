package com.aana.aegislink.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VirusTotalService(
    private val client: OkHttpClient = defaultClient()
) {
    private val limiter = VtRateLimiter()

    suspend fun scanUrl(apiKey: String, url: String): String? = withContext(Dispatchers.IO) {
        if (!limiter.tryAcquire(System.currentTimeMillis())) return@withContext null
        val bodyJson = JSONObject().put("url", url).toString()
        val request = Request.Builder()
            .url("https://www.virustotal.com/api/v3/urls")
            .addHeader("x-apikey", apiKey)
            .post(bodyJson.toRequestBody(JSON))
            .build()

        val analysisId = runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = response.body?.string() ?: return@use null
                JSONObject(json).optJSONObject("data")?.optString("id")
            }
        }.getOrNull() ?: return@withContext null

        return@withContext pollAnalysis(apiKey, analysisId)
    }

    private suspend fun pollAnalysis(apiKey: String, analysisId: String): String? = withContext(Dispatchers.IO) {
        val url = "https://www.virustotal.com/api/v3/analyses/$analysisId"
        val deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apikey", apiKey)
                .get()
                .build()

            val result = runCatching {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    response.body?.string()
                }
            }.getOrNull()

            if (!result.isNullOrBlank()) {
                val status = JSONObject(result)
                    .optJSONObject("data")
                    ?.optJSONObject("attributes")
                    ?.optString("status")
                if (status == "completed") {
                    return@withContext result
                }
            }

            delay(POLL_INTERVAL_MS)
        }

        return@withContext null
    }

    companion object {
        private const val POLL_TIMEOUT_MS = 15_000L
        private const val POLL_INTERVAL_MS = 1_000L
        private val JSON = "application/json; charset=utf-8".toMediaType()
        const val RATE_LIMIT_PER_MIN = 4

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .callTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }
}
