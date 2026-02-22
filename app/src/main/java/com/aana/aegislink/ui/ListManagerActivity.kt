package com.aana.aegislink.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aana.aegislink.R
import com.aana.aegislink.AegisLinkApp
import kotlinx.coroutines.launch
import android.content.Intent
import com.aana.aegislink.ui.SettingsActivity
import android.net.Uri
import android.widget.Toast
import com.aana.aegislink.db.BlacklistEntity
import com.aana.aegislink.db.WhitelistEntity
import com.google.android.material.textfield.TextInputEditText

class ListManagerActivity : AppCompatActivity() {
    private lateinit var blacklistAdapter: ListManagerAdapter
    private lateinit var whitelistAdapter: ListManagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_manager)

        val app = application as AegisLinkApp
        blacklistAdapter = ListManagerAdapter { domain ->
            lifecycleScope.launch {
                app.db.blacklistDao().delete(domain)
                loadLists(app, blacklistAdapter, whitelistAdapter)
            }
        }
        whitelistAdapter = ListManagerAdapter { domain ->
            lifecycleScope.launch {
                app.db.whitelistDao().delete(domain)
                loadLists(app, blacklistAdapter, whitelistAdapter)
            }
        }

        val blacklistRecycler = findViewById<RecyclerView>(R.id.blacklistRecycler)
        blacklistRecycler.layoutManager = LinearLayoutManager(this)
        blacklistRecycler.adapter = blacklistAdapter

        val whitelistRecycler = findViewById<RecyclerView>(R.id.whitelistRecycler)
        whitelistRecycler.layoutManager = LinearLayoutManager(this)
        whitelistRecycler.adapter = whitelistAdapter

        val blacklistInput = findViewById<TextInputEditText>(R.id.blacklistInput)
        val whitelistInput = findViewById<TextInputEditText>(R.id.whitelistInput)
        findViewById<com.google.android.material.button.MaterialButton>(R.id.blacklistAddButton)
            .setOnClickListener {
                val domain = normalizeDomain(blacklistInput.text?.toString().orEmpty())
                if (domain == null) {
                    Toast.makeText(this, getString(R.string.invalid_domain), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    app.db.blacklistDao().insert(
                        BlacklistEntity(
                            domain = domain,
                            addedDate = System.currentTimeMillis(),
                            source = "user"
                        )
                    )
                    blacklistInput.setText("")
                    loadLists(app, blacklistAdapter, whitelistAdapter)
                }
            }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.whitelistAddButton)
            .setOnClickListener {
                val domain = normalizeDomain(whitelistInput.text?.toString().orEmpty())
                if (domain == null) {
                    Toast.makeText(this, getString(R.string.invalid_domain), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    app.db.whitelistDao().insert(
                        WhitelistEntity(
                            domain = domain,
                            addedDate = System.currentTimeMillis(),
                            userAdded = true
                        )
                    )
                    whitelistInput.setText("")
                    loadLists(app, blacklistAdapter, whitelistAdapter)
                }
            }

        lifecycleScope.launch {
            loadLists(app, blacklistAdapter, whitelistAdapter)
        }

        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.listManagerToolbar)
            .setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_settings) {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                } else {
                    false
                }
            }
    }

    override fun onResume() {
        super.onResume()
        val app = application as AegisLinkApp
        val blacklistRecycler = findViewById<RecyclerView>(R.id.blacklistRecycler)
        val whitelistRecycler = findViewById<RecyclerView>(R.id.whitelistRecycler)
        val blacklistAdapter = blacklistRecycler.adapter as? ListManagerAdapter ?: return
        val whitelistAdapter = whitelistRecycler.adapter as? ListManagerAdapter ?: return
        lifecycleScope.launch {
            loadLists(app, blacklistAdapter, whitelistAdapter)
        }
    }

    private suspend fun loadLists(
        app: AegisLinkApp,
        blacklistAdapter: ListManagerAdapter,
        whitelistAdapter: ListManagerAdapter
    ) {
        blacklistAdapter.submit(app.db.blacklistDao().allDomains())
        whitelistAdapter.submit(app.db.whitelistDao().allDomains())
    }

    private fun normalizeDomain(input: String): String? {
        val trimmed = input.trim().lowercase()
        if (trimmed.isBlank()) return null
        val candidate = if (trimmed.contains("://")) {
            Uri.parse(trimmed).host
        } else {
            trimmed.split("/")[0].split(":")[0]
        }
        if (candidate.isNullOrBlank()) return null
        val valid = candidate.all { it.isLetterOrDigit() || it == '.' || it == '-' }
        return if (valid) candidate else null
    }
}
