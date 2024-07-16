package com.example.findme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff.Mode
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.findme.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var btnSelected = 1
    private val key = "bot_bar_selected"
    private lateinit var sharPref: SharedPreferences
    private lateinit var myEdit: SharedPreferences.Editor
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharPref = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        myEdit = sharPref.edit()
        navHostFragment  = supportFragmentManager.findFragmentById(R.id.MainFragmentHost) as NavHostFragment
        navController = navHostFragment.navController

        btnSelected = sharPref.getInt(key, 1)
        chooseBotBar(btnSelected)

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

    private fun chooseBotBar(a: Int) {
        with(binding) {
            when (a) {
                1 -> {
                    searchButton.setColorFilter(getColor(R.color.selected), Mode.SRC_ATOP)
                    searchButton.isEnabled = false
                    accountButton.colorFilter = null
                    accountButton.isEnabled = true
                    btnSelected = 1
                    if(navController.currentDestination?.id != R.id.search){ navController.navigate(R.id.action_accountFragment_to_search)}
                }

                3 -> {
                    searchButton.colorFilter = null
                    searchButton.isEnabled = true
                    accountButton.setColorFilter(getColor(R.color.selected), Mode.SRC_ATOP)
                    accountButton.isEnabled = false
                    btnSelected = 3
                    navController.navigate(R.id.action_search_to_accountFragment)
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        myEdit.putInt(key, btnSelected)
        myEdit.apply()
    }
}