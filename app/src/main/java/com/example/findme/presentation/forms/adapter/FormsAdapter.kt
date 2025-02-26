package com.example.findme.presentation.forms.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.findme.databinding.FormItemBinding
import com.example.forms_sup.entity.Form
import com.example.findme.R

/**
 * Адаптер для показа анкет в SearchFragment
 *
 * @param context нужен контекст для получение ресурсов в строке 33
 * @param items поставить сюда список анкет класса Form
 * @param onItemClick имплементируйте эту функцию чтобы правильно обрабатывать нажатия на анкеты
 */

class FormsAdapter(
    private val context: Context,
    private val items: List<Form>,
    private val onItemClick: (Form) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val binding = FormItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.itemTitle.text = items[position].title
        holder.binding.itemDescription.text = items[position].description
        holder.binding.tagsList.text = context.getString(R.string.tags, items[position].tags.joinToString(", "))

        if (items[position].authorAvatar != null){
            Glide.with(holder.binding.root)
                .load(items[position].authorAvatar)
                .circleCrop()
                .into(holder.binding.avatar)
        }else{
            Glide.with(holder.binding.root)
                .load(R.drawable.profile_button)
                .circleCrop()
                .into(holder.binding.avatar)
        }

        holder.binding.mainLayout.setOnClickListener{
            onItemClick(items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ItemViewHolder(val binding: FormItemBinding) : RecyclerView.ViewHolder(binding.root)