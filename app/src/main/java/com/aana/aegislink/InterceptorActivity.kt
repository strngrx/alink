package com.aana.aegislink

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aana.aegislink.core.DbInstantTrustFlag
import com.aana.aegislink.core.DbReferralMarketingFlag
import com.aana.aegislink.core.NoOpRuleEngine
import com.aana.aegislink.core.UrlClassifier
import com.aana.aegislink.core.UrlNormalizer
import com.aana.aegislink.core.UrlPipeline
import com.aana.aegislink.core.UrlSanitizer
import com.aana.aegislink.core.ClearUrlsRuleEngine
import com.aana.aegislink.core.DaoDomainLookup
import com.aana.aegislink.core.ToggleableRuleEngine
import kotlinx.coroutines.launch
import com.aana.aegislink.shortlink.ShortlinkDetector
import com.aana.aegislink.shortlink.UnshortenService
import com.aana.aegislink.core.Classification
import com.aana.aegislink.ui.MaliciousUrlBottomSheet
import com.aana.aegislink.ui.UnknownUrlBottomSheet
import com.aana.aegislink.utils.BrowserForwarder
import com.aana.aegislink.db.BlacklistEntity
import com.aana.aegislink.db.WhitelistEntity
import com.aana.aegislink.utils.StatsRecorder
import android.widget.Toast
import com.aana.aegislink.utils.SettingsKeys
import com.aana.aegislink.utils.DevLogStore

class InterceptorActivity : AppCompatActivity(),
    MaliciousUrlBottomSheet.Listener,
    UnknownUrlBottomSheet.Listener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent?.data
        if (uri == null) {
            finish()
            return
        }

        val app = application as AegisLinkApp
        val settingsDao = app.db.settingsDao()
        val pipeline = UrlPipeline(
            normalizer = UrlNormalizer(),
            sanitizer = UrlSanitizer(
                ToggleableRuleEngine(
                    delegate = ClearUrlsRuleEngine(
                        context = this,
                        referralMarketingFlag = DbReferralMarketingFlag(settingsDao)
                    ),
                    isEnabled = {
                        settingsDao.getValue(SettingsKeys.URL_SANITIZATION)
                            ?.toBooleanStrictOrNull() ?: true
                    }
                )
            ),
            classifier = UrlClassifier(
                blacklist = DaoDomainLookup { domain ->
                    app.db.blacklistDao().containsDomain(domain)
                },
                whitelist = DaoDomainLookup { domain ->
                    app.db.whitelistDao().containsDomain(domain)
                },
                instantTrustEnabled = DbInstantTrustFlag(settingsDao)
            ),
            shortlinkDetector = ShortlinkDetector(this),
            unshortenService = UnshortenService()
        )

        lifecycleScope.launch {
            val result = pipeline.run(uri.toString())
            val devLogsEnabled = app.db.settingsDao()
                .getValue(SettingsKeys.DEV_LOGS_ENABLED)
                ?.toBooleanStrictOrNull()
                ?: false
            if (devLogsEnabled) {
                DevLogStore(this@InterceptorActivity).append("Intercept result: ${result.classification}")
            }
            when (result.classification) {
                Classification.BLACKLISTED -> {
                    StatsRecorder(app.db.statisticsDao()).recordBlocked()
                    handleAutoBlacklist(result.sanitizedUrl)
                    MaliciousUrlBottomSheet.newInstance(result.sanitizedUrl)
                        .show(supportFragmentManager, "malicious")
                }
                Classification.UNKNOWN -> {
                    UnknownUrlBottomSheet.newInstance(result.sanitizedUrl)
                        .show(supportFragmentManager, "unknown")
                }
                Classification.TRUSTED,
                Classification.WHITELISTED -> {
                    StatsRecorder(app.db.statisticsDao()).recordCleaned()
                    BrowserForwarder(this@InterceptorActivity, settingsDao)
                        .forward(result.sanitizedUrl)
                    finish()
                }
            }
        }
    }

    override fun onMaliciousClosed() {
        finish()
    }

    override fun onReportFalsePositive(url: String) {
        Toast.makeText(this, getString(R.string.report_received), Toast.LENGTH_SHORT).show()
    }

    override fun onProceedAnyway(url: String) {
        lifecycleScope.launch {
            val app = application as AegisLinkApp
            val settingsDao = app.db.settingsDao()
            val autoWhitelist = settingsDao
                .getValue(SettingsKeys.AUTO_WHITELIST)
                ?.toBooleanStrictOrNull()
                ?: false
            if (autoWhitelist) {
                showAutoWhitelistConfirm(url)
            } else {
                proceedOnce(url)
            }
        }
    }

    override fun onAddToBlacklist(url: String) {
        lifecycleScope.launch {
            val app = application as AegisLinkApp
            val domain = Uri.parse(url).host?.lowercase() ?: return@launch
            app.db.blacklistDao().insert(
                BlacklistEntity(
                    domain = domain,
                    addedDate = System.currentTimeMillis(),
                    source = "user"
                )
            )
            Toast.makeText(this@InterceptorActivity, getString(R.string.added_to_blacklist), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAddToWhitelist(url: String) {
        lifecycleScope.launch {
            val app = application as AegisLinkApp
            val domain = Uri.parse(url).host?.lowercase() ?: return@launch
            app.db.whitelistDao().insert(
                WhitelistEntity(
                    domain = domain,
                    addedDate = System.currentTimeMillis(),
                    userAdded = true
                )
            )
            Toast.makeText(this@InterceptorActivity, getString(R.string.added_to_whitelist), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUnknownClosed() {
        finish()
    }

    private fun handleAutoBlacklist(url: String) {
        lifecycleScope.launch {
            val app = application as AegisLinkApp
            val settingsDao = app.db.settingsDao()
            val autoBlacklist = settingsDao
                .getValue(SettingsKeys.AUTO_BLACKLIST)
                ?.toBooleanStrictOrNull()
                ?: false
            if (!autoBlacklist) return@launch

            val domain = Uri.parse(url).host?.lowercase().orEmpty()
            if (domain.isBlank()) return@launch

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this@InterceptorActivity)
                .setTitle(getString(R.string.auto_blacklist_confirm_title))
                .setMessage(getString(R.string.auto_blacklist_confirm_body, domain))
                .setPositiveButton(R.string.add_to_blacklist) { _, _ ->
                    lifecycleScope.launch {
                        app.db.blacklistDao().insert(
                            BlacklistEntity(
                                domain = domain,
                                addedDate = System.currentTimeMillis(),
                                source = "auto"
                            )
                        )
                        Toast.makeText(
                            this@InterceptorActivity,
                            getString(R.string.added_to_blacklist),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }
    }

    private fun proceedOnce(url: String) {
        lifecycleScope.launch {
            val app = application as AegisLinkApp
            StatsRecorder(app.db.statisticsDao()).recordCleaned()
            BrowserForwarder(this@InterceptorActivity, app.db.settingsDao())
                .forward(url)
            finish()
        }
    }

    private fun showAutoWhitelistConfirm(url: String) {
        val domain = Uri.parse(url).host?.lowercase().orEmpty()
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.auto_whitelist_confirm_title))
            .setMessage(getString(R.string.auto_whitelist_confirm_body, domain))
            .setPositiveButton(R.string.always_allow) { _, _ ->
                lifecycleScope.launch {
                    val app = application as AegisLinkApp
                    if (domain.isNotBlank()) {
                        app.db.whitelistDao().insert(
                            WhitelistEntity(
                                domain = domain,
                                addedDate = System.currentTimeMillis(),
                                userAdded = true
                            )
                        )
                    }
                    proceedOnce(url)
                }
            }
            .setNegativeButton(R.string.proceed_once) { _, _ ->
                proceedOnce(url)
            }
            .show()
    }
}
