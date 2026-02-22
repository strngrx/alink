package com.aana.aegislink.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object DefaultBrowserChecker {
    fun isDefaultBrowser(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"))
        val resolved = context.packageManager.resolveActivity(intent, 0) ?: return false
        return resolved.activityInfo.packageName == context.packageName
    }
}
