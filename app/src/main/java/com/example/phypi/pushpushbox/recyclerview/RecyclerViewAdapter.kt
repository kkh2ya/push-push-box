package com.example.phypi.pushpushbox.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.phypi.pushpushbox.R

class RecyclerViewAdapter(var list: List<Rowdata>, var onRowClicked: View.OnClickListener) : RecyclerView.Adapter<RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val inflateVIew: View = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        val viewHolder: RecyclerViewHolder = RecyclerViewHolder(inflateVIew)
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int) {
        viewHolder.titleView.setText(list.get(position).title)
        viewHolder.contentView.setText(list.get(position).content)
        viewHolder.rowLayout.setOnClickListener(onRowClicked)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
