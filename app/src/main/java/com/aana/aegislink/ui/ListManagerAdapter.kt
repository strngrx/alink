package com.aana.aegislink.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aana.aegislink.R

class ListManagerAdapter(
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<ListManagerAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()

    fun submit(domains: List<String>) {
        items.clear()
        items.addAll(domains)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_domain, parent, false)
        return ViewHolder(view, onRemove)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onRemove: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val domainText: TextView = itemView.findViewById(R.id.domainText)
        private val removeButton: View = itemView.findViewById(R.id.removeButton)

        fun bind(domain: String) {
            domainText.text = domain
            removeButton.setOnClickListener { onRemove(domain) }
        }
    }
}
