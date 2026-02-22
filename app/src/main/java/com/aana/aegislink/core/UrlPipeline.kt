package com.aana.aegislink.core

import android.net.Uri
import com.aana.aegislink.shortlink.ShortlinkDetector
import com.aana.aegislink.shortlink.UnshortenService

class UrlPipeline(
    private val normalizer: UrlNormalizer,
    private val sanitizer: UrlSanitizer,
    private val classifier: UrlClassifier,
    private val shortlinkDetector: ShortlinkDetector?,
    private val unshortenService: UnshortenService?
) {
    suspend fun run(rawUrl: String): PipelineResult {
        return runInternal(rawUrl, 0)
    }

    private suspend fun runInternal(rawUrl: String, depth: Int): PipelineResult {
        val normalized = normalizer.normalize(rawUrl)
        val normalizedDomain = Uri.parse(normalized).host?.lowercase()

        if (depth == 0 && normalizedDomain != null && shortlinkDetector != null && unshortenService != null) {
            if (shortlinkDetector.isShortlink(normalizedDomain)) {
                val resolved = unshortenService.resolve(normalized)
                if (!resolved.isNullOrBlank()) {
                    return runInternal(resolved, depth + 1)
                }
            }
        }

        val sanitizerOutcome = sanitizer.sanitize(normalized)
        val classification = classifier.classify(
            url = sanitizerOutcome.url,
            trustSignal = sanitizerOutcome.trustSignal
        )
        val domain = Uri.parse(sanitizerOutcome.url).host?.lowercase()

        return PipelineResult(
            originalUrl = rawUrl,
            normalizedUrl = normalized,
            sanitizedUrl = sanitizerOutcome.url,
            domain = domain,
            trustSignal = sanitizerOutcome.trustSignal,
            classification = classification
        )
    }
}
