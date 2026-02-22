package com.aana.aegislink.api

data class VtVerdict(
    val malicious: Int,
    val suspicious: Int,
    val harmless: Int
)

enum class VtLabel {
    UNDETECTED,
    SUSPICIOUS,
    DANGEROUS,
    UNKNOWN
}
