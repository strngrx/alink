package com.aana.aegislink

import android.app.Application
import androidx.room.Room
import androidx.appcompat.app.AppCompatDelegate
import com.aana.aegislink.db.AegisDatabase
import com.aana.aegislink.utils.SettingsKeys
import com.aana.aegislink.utils.SeedLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AegisLinkApp : Application() {
    lateinit var db: AegisDatabase
        private set
    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            AegisDatabase::class.java,
            "aegis.db"
        ).build()

        appScope.launch {
            val mode = db.settingsDao().getValue(SettingsKeys.THEME_MODE) ?: "system"
            val nightMode = when (mode) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            withContext(Dispatchers.Main) {
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }

            SeedLoader(
                context = applicationContext,
                settingsDao = db.settingsDao(),
                blacklistDao = db.blacklistDao()
            ).seedBlacklistIfNeeded()
        }
    }
}
