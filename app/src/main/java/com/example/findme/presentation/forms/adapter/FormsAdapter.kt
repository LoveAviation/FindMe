package com.example.findme.presentation.forms.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.findme.databinding.FormItemBinding
import com.example.forms_sup.entity.Form

class FormsAdapter(private val items: List<Form>) : RecyclerView.Adapter<ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val binding = FormItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.itemTitle.text = items[position].title
        holder.binding.itemDescription.text = items[position].description
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ItemViewHolder(val binding: FormItemBinding) : RecyclerView.ViewHolder(binding.root)