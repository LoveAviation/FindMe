package com.example.findme.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.findme.databinding.ActivityCreateFormBinding

class CreateFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateFormBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }
    }
}