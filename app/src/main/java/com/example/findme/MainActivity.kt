package com.example.findme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.PorterDuff.Mode;
import android.view.animation.Animation
import com.example.findme.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var bot_bar_selected = 1
    private val KEY = "bot_bar_selected"

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        bot_bar_selected = savedInstanceState.getInt(KEY)
        chooseBotBar(bot_bar_selected)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchButton.setOnClickListener {
            chooseBotBar(1)
        }
        binding.createButton.setOnClickListener {
            startActivity(Intent(this, CreateFormActivity::class.java))
        }
        binding.accountButton.setOnClickListener {
            chooseBotBar(3)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY, bot_bar_selected)
        super.onSaveInstanceState(outState)
    }

    private fun chooseBotBar(a: Int) {
        with(binding) {
            when (a) {
                1 -> {
                    searchButton.setColorFilter(getColor(R.color.selected), Mode.SRC_ATOP)
                    createButton.colorFilter = null
                    accountButton.colorFilter = null
                    bot_bar_selected = 1
                }

                3 -> {
                    binding.searchButton.colorFilter = null
                    binding.createButton.colorFilter = null
                    binding.accountButton.setColorFilter(getColor(R.color.selected), Mode.SRC_ATOP)
                    bot_bar_selected = 3
                }
            }
        }
    }
}