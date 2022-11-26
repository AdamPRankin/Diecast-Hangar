package com.example.diecasthangar.ui

import com.example.diecasthangar.data.model.Reaction
import com.example.diecasthangar.databinding.RecyclerReactionRowLayoutBinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class ReactionsRecyclerAdapter(): RecyclerView.Adapter<ReactionsRecyclerAdapter.ViewHolder>() {
    var reacts = ArrayList<Reaction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerReactionRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val react = reacts[position]

        holder.reactIcon.setImageResource(react.icon)
        holder.reactName.text = react.type
        holder.reactNumber.text = react.number.toString()

    }
    inner class ViewHolder(binding: RecyclerReactionRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        private var view: View = binding.root

        val reactIcon = binding.reactRowIcon
        val reactName = binding.reactRowText
        val reactNumber = binding.reactRowNumber

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return reacts.size
    }



}