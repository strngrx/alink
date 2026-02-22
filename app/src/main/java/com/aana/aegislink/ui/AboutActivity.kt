package com.aana.aegislink.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aana.aegislink.AegisLinkApp
import com.aana.aegislink.R
import com.aana.aegislink.db.SettingsEntity
import com.aana.aegislink.utils.DevLogStore
import com.aana.aegislink.utils.SettingsKeys
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AboutActivity : AppCompatActivity() {
    private var tapCount = 0
    private var lastTapMs = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val app = application as AegisLinkApp
        val settingsDao = app.db.settingsDao()
        val logStore = DevLogStore(this)

        val title = findViewById<TextView>(R.id.aboutTitle)
        val logsSection = findViewById<TextView>(R.id.logsSectionTitle)
        val logsBody = findViewById<TextView>(R.id.logsBody)
        val clearButton = findViewById<MaterialButton>(R.id.clearLogsButton)

        fun setLogsVisible(visible: Boolean) {
            logsSection.visibility = if (visible) TextView.VISIBLE else TextView.GONE
            logsBody.visibility = if (visible) TextView.VISIBLE else TextView.GONE
            clearButton.visibility = if (visible) TextView.VISIBLE else TextView.GONE
        }

        lifecycleScope.launch {
            val enabled = settingsDao.getValue(SettingsKeys.DEV_LOGS_ENABLED)
                ?.toBooleanStrictOrNull() ?: false
            setLogsVisible(enabled)
            if (enabled) {
                logsBody.text = logStore.readAll().ifBlank { getString(R.string.logs_empty) }
            }
        }

        title.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastTapMs > 2000) {
                tapCount = 0
            }
            lastTapMs = now
            tapCount += 1
            if (tapCount >= 7) {
                tapCount = 0
                lifecycleScope.launch {
                    settingsDao.upsert(SettingsEntity(SettingsKeys.DEV_LOGS_ENABLED, "true"))
                    setLogsVisible(true)
                    logsBody.text = logStore.readAll().ifBlank { getString(R.string.logs_empty) }
                }
            }
        }

        clearButton.setOnClickListener {
            lifecycleScope.launch {
                logStore.clear()
                logsBody.text = getString(R.string.logs_empty)
            }
        }
    }
}
