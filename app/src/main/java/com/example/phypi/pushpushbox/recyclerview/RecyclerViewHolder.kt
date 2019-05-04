package com.example.phypi.pushpushbox.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.phypi.pushpushbox.R

class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var titleView: TextView = itemView.findViewById(R.id.row_title)
    var contentView: TextView = itemView.findViewById(R.id.row_content)
    var rowLayout: LinearLayout = itemView.findViewById(R.id.row_wrap)
}