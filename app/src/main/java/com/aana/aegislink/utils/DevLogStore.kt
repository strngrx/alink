package com.aana.aegislink.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DevLogStore(
    private val context: Context
) {
    private val fileName = "dev_logs.txt"
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    suspend fun append(message: String) = withContext(Dispatchers.IO) {
        val line = "${formatter.format(Date())} | $message\n"
        context.openFileOutput(fileName, Context.MODE_APPEND).use { out ->
            out.write(line.toByteArray(Charsets.UTF_8))
        }
    }

    suspend fun readAll(): String = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            context.openFileInput(fileName).bufferedReader().use { it.readText() }
        }.getOrDefault("")
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        context.deleteFile(fileName)
    }
}
