package com.aana.aegislink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aana.aegislink.utils.StatsKeys
import com.aana.aegislink.AegisLinkApp
import com.aana.aegislink.ui.ListManagerActivity
import android.content.Intent
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.aana.aegislink.ui.SettingsActivity
import com.aana.aegislink.ui.OnboardingActivity
import com.aana.aegislink.ui.AboutActivity
import com.aana.aegislink.utils.SettingsKeys
import androidx.appcompat.app.AppCompatDelegate
import com.aana.aegislink.utils.DefaultBrowserChecker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = application as AegisLinkApp
        lifecycleScope.launch {
            val onboarded = app.db.settingsDao()
                .getValue(com.aana.aegislink.utils.SettingsKeys.ONBOARDING_COMPLETE)
                ?.toBooleanStrictOrNull()
                ?: false
            if (!onboarded) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
                return@launch
            }
        }
        val cleanedText = findViewById<TextView>(R.id.cleanedCount)
        val blockedText = findViewById<TextView>(R.id.blockedCount)
        val statusBody = findViewById<TextView>(R.id.statusBody)
        val manageLists = findViewById<MaterialButton>(R.id.manageListsButton)
        val settingsButton = findViewById<MaterialButton>(R.id.settingsButton)
        val aboutButton = findViewById<MaterialButton>(R.id.aboutButton)
        val themeButton = findViewById<MaterialButton>(R.id.themeToggleButton)

        manageLists.setOnClickListener {
            startActivity(Intent(this, ListManagerActivity::class.java))
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        themeButton.setOnClickListener {
            lifecycleScope.launch {
                val current = app.db.settingsDao().getValue(SettingsKeys.THEME_MODE) ?: "system"
                val next = when (current) {
                    "dark" -> "light"
                    "light" -> "dark"
                    else -> "dark"
                }
                app.db.settingsDao().upsert(com.aana.aegislink.db.SettingsEntity(SettingsKeys.THEME_MODE, next))
                val nightMode = if (next == "dark") {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }

        lifecycleScope.launch {
            val cleaned = app.db.statisticsDao().getCount(StatsKeys.TOTAL_CLEANED) ?: 0
            val blocked = app.db.statisticsDao().getCount(StatsKeys.TOTAL_BLOCKED) ?: 0
            cleanedText.text = getString(R.string.cleaned_count_format, cleaned)
            blockedText.text = getString(R.string.blocked_count_format, blocked)
        }

        statusBody.text = if (DefaultBrowserChecker.isDefaultBrowser(this)) {
            getString(R.string.status_active)
        } else {
            getString(R.string.status_inactive)
        }
    }
}
