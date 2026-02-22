package com.aana.aegislink.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityKeyStore(context: Context) {
    private val appContext = context.applicationContext
    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        appContext,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_VT_API, key).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_VT_API, null)
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_VT_API).apply()
    }

    companion object {
        private const val PREFS_NAME = "aegis_secure"
        private const val KEY_VT_API = "vt_api_key"
    }
}
