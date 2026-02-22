package com.aana.aegislink.api

class VtRateLimiter(
    private val maxPerMinute: Int = VirusTotalService.RATE_LIMIT_PER_MIN
) {
    private val timestamps = ArrayDeque<Long>(maxPerMinute)

    @Synchronized
    fun tryAcquire(nowMillis: Long): Boolean {
        val windowStart = nowMillis - 60_000L
        while (timestamps.isNotEmpty() && timestamps.first() < windowStart) {
            timestamps.removeFirst()
        }
        if (timestamps.size >= maxPerMinute) return false
        timestamps.addLast(nowMillis)
        return true
    }
}
