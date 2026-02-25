package com.aana.aegislink

import android.net.Uri

object URLNormaliser {

    fun normalize(uri: Uri): Uri {
        // In a real-world scenario, you might want to:
        // - Remove tracking parameters
        // - Convert to a canonical representation
        // - etc.
        // For now, we'll just return the original URI
        return uri
    }
}
