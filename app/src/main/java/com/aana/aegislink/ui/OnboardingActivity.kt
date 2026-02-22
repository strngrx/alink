package com.aana.aegislink.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aana.aegislink.AegisLinkApp
import com.aana.aegislink.R
import com.aana.aegislink.db.SettingsEntity
import com.aana.aegislink.utils.SettingsKeys
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val app = application as AegisLinkApp
        val settingsDao = app.db.settingsDao()

        val recycler = findViewById<RecyclerView>(R.id.browserRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val adapter = BrowserListAdapter { item ->
            lifecycleScope.launch {
                settingsDao.upsert(
                    SettingsEntity(SettingsKeys.PREFERRED_BROWSER_PACKAGE, item.packageName)
                )
            }
        }
        recycler.adapter = adapter
        adapter.submit(loadBrowsers())

        findViewById<MaterialButton>(R.id.setDefaultBrowserButton).setOnClickListener {
            openDefaultAppsSettings()
        }

        findViewById<MaterialButton>(R.id.finishOnboardingButton).setOnClickListener {
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.ONBOARDING_COMPLETE, "true"))
                startActivity(Intent(this@OnboardingActivity, com.aana.aegislink.MainActivity::class.java))
                finish()
            }
        }
    }

    private fun loadBrowsers(): List<BrowserItem> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"))
        val infos = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return infos.map {
            BrowserItem(
                label = it.loadLabel(packageManager).toString(),
                packageName = it.activityInfo.packageName
            )
        }.sortedBy { it.label.lowercase() }
    }

    private fun openDefaultAppsSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val appSettings = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
            startActivity(appSettings)
        }
    }
}
