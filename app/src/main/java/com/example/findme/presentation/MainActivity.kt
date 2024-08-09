package com.example.findme.presentation


import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff.Mode
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.findme.R
import com.example.findme.databinding.ActivityMainBinding
import com.example.findme.domain.OnDataClearListener


class MainActivity : AppCompatActivity(), OnDataClearListener {
    private lateinit var binding : ActivityMainBinding
    private var btnSelected = 1
    private lateinit var sharPref: SharedPreferences
    private lateinit var sharePrefEditor: SharedPreferences.Editor
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private val viewModel : SaveDataVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharPref = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        sharePrefEditor = sharPref.edit()
        navHostFragment  = supportFragmentManager.findFragmentById(R.id.MainFragmentHost) as NavHostFragment
        navController = navHostFragment.navController


        viewModel.accName = intent?.getStringExtra(KEY_NAME) ?: sharPref.getString(KEY_NAME, null)
        viewModel.accSurname = intent?.getStringExtra(KEY_SURNAME) ?:  sharPref.getString(KEY_SURNAME, null)
        btnSelected = sharPref.getInt(KEY, 1)
        chooseBotBar(btnSelected)

        binding.searchButton.setOnClickListener{ chooseBotBar(1) }
        binding.createButton.setOnClickListener {
            startActivity(Intent(this, CreateFormActivity::class.java))
        }
        binding.accountButton.setOnClickListener{
            if(viewModel.accName == null && viewModel.accSurname == null){
                startActivity(Intent(this, RegistrationActivity::class.java))
            }
            else chooseBotBar(3)
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
                    if(navController.currentDestination?.id != R.id.search){ navController.navigate(
                        R.id.action_accountFragment_to_search
                    )}
                }

                3 -> {
                    searchButton.colorFilter = null
                    searchButton.isEnabled = true
                    accountButton.setColorFilter(getColor(R.color.selected), Mode.SRC_ATOP)
                    accountButton.isEnabled = false
                    btnSelected = 3
                    if(navController.currentDestination?.id != R.id.accountFragment){
                        val bundle = Bundle().apply {
                            putString(KEY_NAME, viewModel.accName)
                            putString(KEY_SURNAME, viewModel.accSurname)
                        }
                        navController.navigate(R.id.action_search_to_accountFragment, bundle)
                    }
                }
            }
        }
    }

    override fun clearUserData() {
        viewModel.clearAll()
        chooseBotBar(1)
    }


    override fun onPause() {
        super.onPause()
        sharePrefEditor.putInt(KEY, btnSelected)
            .putString(KEY_NAME, viewModel.accName)
            .putString(KEY_SURNAME, viewModel.accSurname)
            .apply()
    }

    companion object{
        const val KEY = "bot_bar_selected"
        const val KEY_NAME = "Name"
        const val KEY_SURNAME = "Surname"
    }
}