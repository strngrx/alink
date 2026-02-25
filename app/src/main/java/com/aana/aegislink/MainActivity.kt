package com.aana.aegislink

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aana.aegislink.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PREFS_NAME = "AegisLinkPrefs"
    private val PREF_KEY_PREFERRED_BROWSER = "preferred_browser_package"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        if (intent?.action == Intent.ACTION_VIEW) {
            handleUrlIntent(intent)
            return
        }

        binding.setDefaultBrowserButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        }

        binding.choosePreferredBrowserButton.setOnClickListener {
            showBrowserChooser()
        }
        checkDefaultBrowser()
    }

    override fun onResume() {
        super.onResume()
        checkDefaultBrowser()
    }

    private fun checkDefaultBrowser() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        val resolveInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)

        if (resolveInfo?.activityInfo?.packageName == packageName) {
            binding.setDefaultBrowserButton.isEnabled = false
            binding.setDefaultBrowserButton.text = "Set as Default Browser (already set)"
        } else {
            binding.setDefaultBrowserButton.isEnabled = true
            binding.setDefaultBrowserButton.text = "Set as Default Browser"
        }
    }

    private fun handleUrlIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null) {
            val normalizedUri = URLNormaliser.normalize(data)
            val preferredBrowserPackage = getPreferredBrowser()

            if (preferredBrowserPackage != null) {
                val browserIntent = Intent(Intent.ACTION_VIEW, normalizedUri)
                browserIntent.setPackage(preferredBrowserPackage)
                try {
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open link with preferred browser.", Toast.LENGTH_SHORT).show()
                    showBrowserChooser()
                }
            } else {
                Toast.makeText(this, "Please choose a preferred browser first.", Toast.LENGTH_SHORT).show()
                showBrowserChooser()
            }
        }
        finish()
    }

    private fun showBrowserChooser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        val pm = packageManager
        val browsers = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        val browserPackages = browsers.map { it.activityInfo.packageName }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Choose a browser")
        builder.setItems(browserPackages.toTypedArray()) { _, which ->
            val selectedPackage = browserPackages[which]
            savePreferredBrowser(selectedPackage)
        }
        builder.show()
    }

    private fun savePreferredBrowser(packageName: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_PREFERRED_BROWSER, packageName).apply()
        Toast.makeText(this, "Preferred browser saved: $packageName", Toast.LENGTH_SHORT).show()
    }

    private fun getPreferredBrowser(): String? {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(PREF_KEY_PREFERRED_BROWSER, null)
    }
}
