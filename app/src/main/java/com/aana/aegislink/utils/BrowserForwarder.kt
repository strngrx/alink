package com.aana.aegislink.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aana.aegislink.db.SettingsDao

class BrowserForwarder(
    private val context: Context,
    private val settingsDao: SettingsDao
) {
    suspend fun forward(url: String) {
        val preferred = settingsDao.getValue(SettingsKeys.PREFERRED_BROWSER_PACKAGE)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (!preferred.isNullOrBlank() && preferred != context.packageName) {
            intent.setPackage(preferred)
        }

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            if (!preferred.isNullOrBlank()) {
                intent.`package` = null
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(com.aana.aegislink.R.string.no_browser_found),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
