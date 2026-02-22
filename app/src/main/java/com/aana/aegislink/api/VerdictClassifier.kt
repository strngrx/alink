package com.aana.aegislink.api

object VerdictClassifier {
    fun classify(verdict: VtVerdict?): VtLabel {
        if (verdict == null) return VtLabel.UNKNOWN
        val bad = verdict.malicious + verdict.suspicious
        return when {
            verdict.malicious >= 4 -> VtLabel.DANGEROUS
            bad in 1..3 -> VtLabel.SUSPICIOUS
            bad == 0 -> VtLabel.UNDETECTED
            else -> VtLabel.UNKNOWN
        }
    }
}
