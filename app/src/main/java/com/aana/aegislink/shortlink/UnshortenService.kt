package com.aana.aegislink.shortlink

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UnshortenService(
    private val client: OkHttpClient = defaultClient()
) {
    suspend fun resolve(url: String): String? = withContext(Dispatchers.IO) {
        val bodyJson = JSONObject().put("url", url).toString()
        val body = bodyJson.toRequestBody(JSON)
        val request = Request.Builder()
            .url("https://unshorten.me/api/v2/unshorten")
            .post(body)
            .build()

        var attempt = 0
        while (attempt < 2) {
            val result = runCatching {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val text = response.body?.string() ?: return@use null
                    val resolved = JSONObject(text).optString("resolved", "")
                    if (resolved.isBlank()) null else resolved
                }
            }.getOrNull()
            if (!result.isNullOrBlank()) return@withContext result
            attempt += 1
        }

        return@withContext null
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .build()
        }
    }
}
