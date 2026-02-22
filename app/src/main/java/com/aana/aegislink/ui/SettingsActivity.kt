package com.aana.aegislink.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aana.aegislink.AegisLinkApp
import com.aana.aegislink.R
import com.aana.aegislink.db.SettingsEntity
import com.aana.aegislink.security.SecurityKeyStore
import com.aana.aegislink.utils.SettingsKeys
import kotlinx.coroutines.launch
import android.content.Intent
import com.aana.aegislink.ui.AboutActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val app = application as AegisLinkApp
        val settingsDao = app.db.settingsDao()

        val toggleInstantTrust = findViewById<Switch>(R.id.toggleInstantTrust)
        val toggleSanitization = findViewById<Switch>(R.id.toggleSanitization)
        val toggleReferral = findViewById<Switch>(R.id.toggleReferral)
        val toggleVt = findViewById<Switch>(R.id.toggleVt)
        val toggleAutoWhitelist = findViewById<Switch>(R.id.toggleAutoWhitelist)
        val toggleAutoBlacklist = findViewById<Switch>(R.id.toggleAutoBlacklist)
        val vtKeyInput = findViewById<EditText>(R.id.vtKeyInput)
        val aboutButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.aboutButton)
        val saveVtKeyButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.saveVtKeyButton)

        val keyStore = SecurityKeyStore(this)

        lifecycleScope.launch {
            toggleInstantTrust.isChecked =
                settingsDao.getValue(SettingsKeys.INSTANT_TRUST)?.toBooleanStrictOrNull() ?: false
            toggleSanitization.isChecked =
                settingsDao.getValue(SettingsKeys.URL_SANITIZATION)?.toBooleanStrictOrNull() ?: true
            toggleReferral.isChecked =
                settingsDao.getValue(SettingsKeys.REFERRAL_MARKETING)?.toBooleanStrictOrNull() ?: false
            toggleVt.isChecked =
                settingsDao.getValue(SettingsKeys.VT_ENABLED)?.toBooleanStrictOrNull() ?: false
            toggleAutoWhitelist.isChecked =
                settingsDao.getValue(SettingsKeys.AUTO_WHITELIST)?.toBooleanStrictOrNull() ?: false
            toggleAutoBlacklist.isChecked =
                settingsDao.getValue(SettingsKeys.AUTO_BLACKLIST)?.toBooleanStrictOrNull() ?: false
            vtKeyInput.setText(keyStore.getApiKey().orEmpty())
            vtKeyInput.isEnabled = toggleVt.isChecked
        }

        toggleInstantTrust.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.INSTANT_TRUST, isChecked.toString()))
            }
        }

        toggleSanitization.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.URL_SANITIZATION, isChecked.toString()))
            }
        }

        toggleReferral.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.REFERRAL_MARKETING, isChecked.toString()))
            }
        }

        toggleAutoWhitelist.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.AUTO_WHITELIST, isChecked.toString()))
            }
        }

        toggleAutoBlacklist.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.AUTO_BLACKLIST, isChecked.toString()))
            }
        }

        toggleVt.setOnCheckedChangeListener { _, isChecked ->
            vtKeyInput.isEnabled = isChecked
            lifecycleScope.launch {
                settingsDao.upsert(SettingsEntity(SettingsKeys.VT_ENABLED, isChecked.toString()))
            }
        }

        vtKeyInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val key = vtKeyInput.text?.toString()?.trim().orEmpty()
                if (key.isNotEmpty()) {
                    keyStore.saveApiKey(key)
                } else {
                    keyStore.clearApiKey()
                }
            }
        }

        saveVtKeyButton.setOnClickListener {
            val key = vtKeyInput.text?.toString()?.trim().orEmpty()
            if (key.isNotEmpty()) {
                keyStore.saveApiKey(key)
                android.widget.Toast.makeText(this, getString(R.string.vt_key_saved), android.widget.Toast.LENGTH_SHORT).show()
            } else {
                keyStore.clearApiKey()
                android.widget.Toast.makeText(this, getString(R.string.vt_key_cleared), android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
