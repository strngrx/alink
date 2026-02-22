package com.aana.aegislink.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.aana.aegislink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class MaliciousUrlBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_malicious, container, false)
        val url = requireArguments().getString(ARG_URL).orEmpty()
        view.findViewById<TextView>(R.id.maliciousUrlText).text = url
        view.findViewById<MaterialButton>(R.id.maliciousCloseButton).setOnClickListener {
            dismissAllowingStateLoss()
            (activity as? Listener)?.onMaliciousClosed()
        }
        view.findViewById<MaterialButton>(R.id.reportFalsePositiveButton).setOnClickListener {
            (activity as? Listener)?.onReportFalsePositive(url)
        }
        return view
    }

    interface Listener {
        fun onMaliciousClosed()
        fun onReportFalsePositive(url: String)
    }

    companion object {
        private const val ARG_URL = "arg_url"

        fun newInstance(url: String): MaliciousUrlBottomSheet {
            return MaliciousUrlBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_URL, url) }
            }
        }
    }
}
