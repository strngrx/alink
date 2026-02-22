package com.aana.aegislink.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.aana.aegislink.R
import com.aana.aegislink.AegisLinkApp
import com.aana.aegislink.api.VerdictClassifier
import com.aana.aegislink.api.VirusTotalService
import com.aana.aegislink.api.VtRepository
import com.aana.aegislink.api.VtCacheStore
import com.aana.aegislink.security.SecurityKeyStore
import com.aana.aegislink.utils.DevLogStore
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class UnknownUrlBottomSheet : BottomSheetDialogFragment() {
    private var statusText: TextView? = null
    private var scanButton: MaterialButton? = null
    private var statusCard: MaterialCardView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_unknown, container, false)
        val url = requireArguments().getString(ARG_URL).orEmpty()
        view.findViewById<TextView>(R.id.unknownUrlText).text = url

        statusText = view.findViewById(R.id.vtStatusBody)
        statusCard = view.findViewById(R.id.vtStatusCard)
        scanButton = view.findViewById(R.id.scanVtButton)
        scanButton?.setOnClickListener { runVtScan(url) }
        view.findViewById<MaterialButton>(R.id.proceedButton).setOnClickListener {
            dismissAllowingStateLoss()
            (activity as? Listener)?.onProceedAnyway(url)
        }
        view.findViewById<MaterialButton>(R.id.closeButton).setOnClickListener {
            dismissAllowingStateLoss()
            (activity as? Listener)?.onUnknownClosed()
        }
        view.findViewById<MaterialButton>(R.id.addToBlacklistButton).setOnClickListener {
            (activity as? Listener)?.onAddToBlacklist(url)
        }
        view.findViewById<MaterialButton>(R.id.addToWhitelistButton).setOnClickListener {
            (activity as? Listener)?.onAddToWhitelist(url)
        }
        return view
    }

    interface Listener {
        fun onProceedAnyway(url: String)
        fun onAddToBlacklist(url: String)
        fun onAddToWhitelist(url: String)
        fun onUnknownClosed()
    }

    private fun runVtScan(url: String) {
        val app = activity?.application as? AegisLinkApp ?: return
        scanButton?.isEnabled = false
        statusText?.text = getString(R.string.vt_scanning)

        viewLifecycleOwner.lifecycleScope.launch {
            val settingsDao = app.db.settingsDao()
            val vtEnabled = settingsDao
                .getValue(com.aana.aegislink.utils.SettingsKeys.VT_ENABLED)
                ?.toBooleanStrictOrNull()
                ?: false
            if (!vtEnabled) {
                statusText?.text = getString(R.string.vt_disabled)
                scanButton?.isEnabled = true
                return@launch
            }

            val apiKey = SecurityKeyStore(requireContext()).getApiKey()
            if (apiKey.isNullOrBlank()) {
                statusText?.text = getString(R.string.vt_missing_key)
                scanButton?.isEnabled = true
                return@launch
            }

            val repository = VtRepository(
                service = VirusTotalService(),
                cacheStore = VtCacheStore(app.db.vtCacheDao())
            )

            val verdict = repository.getOrScan(url, apiKey, System.currentTimeMillis())
            val label = VerdictClassifier.classify(verdict)
            val labelText = when (label) {
                com.aana.aegislink.api.VtLabel.UNDETECTED -> getString(R.string.vt_undetected)
                com.aana.aegislink.api.VtLabel.SUSPICIOUS -> getString(R.string.vt_suspicious)
                com.aana.aegislink.api.VtLabel.DANGEROUS -> getString(R.string.vt_dangerous)
                com.aana.aegislink.api.VtLabel.UNKNOWN -> getString(R.string.vt_unknown)
            }

            val devLogsEnabled = settingsDao
                .getValue(com.aana.aegislink.utils.SettingsKeys.DEV_LOGS_ENABLED)
                ?.toBooleanStrictOrNull()
                ?: false
            if (devLogsEnabled) {
                DevLogStore(requireContext()).append("VT scan verdict: $label")
            }

            statusText?.text = if (verdict == null) {
                getString(R.string.vt_unknown)
            } else {
                getString(
                    R.string.vt_status_format,
                    verdict.malicious,
                    verdict.suspicious,
                    verdict.harmless,
                    labelText
                )
            }
            val color = when (label) {
                com.aana.aegislink.api.VtLabel.DANGEROUS -> R.color.vt_danger
                com.aana.aegislink.api.VtLabel.SUSPICIOUS -> R.color.vt_warning
                com.aana.aegislink.api.VtLabel.UNDETECTED -> R.color.vt_safe
                com.aana.aegislink.api.VtLabel.UNKNOWN -> R.color.vt_neutral
            }
            statusCard?.setCardBackgroundColor(requireContext().getColor(color))
            scanButton?.isEnabled = true
        }
    }

    companion object {
        private const val ARG_URL = "arg_url"

        fun newInstance(url: String): UnknownUrlBottomSheet {
            return UnknownUrlBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_URL, url) }
            }
        }
    }
}
