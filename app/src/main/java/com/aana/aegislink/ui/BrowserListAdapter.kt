package com.aana.aegislink.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aana.aegislink.R

data class BrowserItem(
    val label: String,
    val packageName: String
)

class BrowserListAdapter(
    private val onSelect: (BrowserItem) -> Unit
) : RecyclerView.Adapter<BrowserListAdapter.ViewHolder>() {

    private val items = mutableListOf<BrowserItem>()

    fun submit(list: List<BrowserItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_browser, parent, false)
        return ViewHolder(view, onSelect)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onSelect: (BrowserItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView = itemView.findViewById(R.id.browserLabel)
        private val selectButton: View = itemView.findViewById(R.id.browserSelectButton)

        fun bind(item: BrowserItem) {
            label.text = item.label
            selectButton.setOnClickListener { onSelect(item) }
        }
    }
}
