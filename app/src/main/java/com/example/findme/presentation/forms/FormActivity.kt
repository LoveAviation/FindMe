package com.example.findme.presentation.forms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.ActivityFormBinding
import dagger.Binds

class FormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = intent.getStringExtra(KEY_TITLE)
        binding.title.text = intent.getStringExtra(KEY_TITLE)
        binding.description.text = intent.getStringExtra(KEY_DESCRIPTION)
        binding.tagsList.text = "Tags: ${intent.getStringExtra(KEY_TAGS)}"
        binding.author.text = intent.getStringExtra(KEY_AUTHOR)

        val avatar = intent.getStringExtra(KEY_AVATAR)
        if(avatar != null) {
            Glide.with(binding.root)
                .load(intent.getStringExtra(KEY_AVATAR))
                .circleCrop()
                .into(binding.authorAvatar)
        }

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }
    }

    companion object{
        const val KEY_TITLE = "title"
        const val KEY_DESCRIPTION = "description"
        const val KEY_TAGS = "tags"
        const val KEY_LOCATION = "location"
        const val KEY_AUTHOR = "author"
        const val KEY_AVATAR = "author_avatar"
    }
}