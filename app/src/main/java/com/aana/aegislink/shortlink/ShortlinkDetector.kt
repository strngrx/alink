package com.aana.aegislink.shortlink

import android.content.Context
import org.json.JSONArray
import java.util.concurrent.atomic.AtomicReference

class ShortlinkDetector(
    context: Context,
    private val assetName: String = "shorteners_list.json"
) {
    private val appContext = context.applicationContext
    private val cachedDomains = AtomicReference<Set<String>?>(null)

    fun isShortlink(domain: String): Boolean {
        val normalized = domain.lowercase()
        val list = loadDomains()
        return list.contains(normalized)
    }

    private fun loadDomains(): Set<String> {
        val cached = cachedDomains.get()
        if (cached != null) return cached

        val json = appContext.assets.open(assetName).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val set = HashSet<String>(array.length())
        for (i in 0 until array.length()) {
            val value = array.optString(i)
            if (value.isNullOrBlank()) continue
            set.add(value.lowercase())
        }
        cachedDomains.compareAndSet(null, set)
        return cachedDomains.get() ?: set
    }
}
