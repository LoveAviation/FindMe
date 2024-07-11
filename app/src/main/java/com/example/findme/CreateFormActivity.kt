package com.example.findme

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.findme.databinding.ActivityCreateFormBinding

class CreateFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateFormBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backTitleBtn.setOnClickListener {
            finish()
        }
    }
}