package com.example.findme.presentation.forms.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.findme.databinding.TagItemBinding

/**
 * Адаптер для списка тэгов
 *
 * @param items список слов
 * @param listener имплементируйте это, чтобы правильно отвечать на нажатия на крестик
 */

class TagsAdapter(
    private val items: List<String>,
    private val listener: OnButtonClickListener
) : RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: TagItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.tag.text = item
            binding.delete.setOnClickListener {
                listener.onButtonClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnButtonClickListener {
        fun onButtonClick(position: Int)
    }
}