package com.example.findme.presentation


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff.Mode
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.findme.R
import com.example.findme.databinding.ActivityMainBinding
import com.example.findme.other.OnDataClearListener
import com.example.findme.presentation.account.RegistrationActivity
import com.example.findme.presentation.forms.CreateFormActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
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

        sharPref = getSharedPreferences("MainSharedPref", MODE_PRIVATE)
        sharePrefEditor = sharPref.edit()
        navHostFragment  = supportFragmentManager.findFragmentById(R.id.MainFragmentHost) as NavHostFragment
        navController = navHostFragment.navController


        viewModel.accLogin = intent?.getStringExtra(KEY_LOGIN) ?:  sharPref.getString(KEY_LOGIN, null)
        viewModel.accPassword = intent?.getStringExtra(KEY_PASSWORD) ?:  sharPref.getString(KEY_PASSWORD, null)
        viewModel.accName = intent?.getStringExtra(KEY_NAME) ?: sharPref.getString(KEY_NAME, null)
        viewModel.accSurname = intent?.getStringExtra(KEY_SURNAME) ?:  sharPref.getString(KEY_SURNAME, null)
        viewModel.accAvatar = intent?.getStringExtra(KEY_AVATAR) ?:  sharPref.getString(KEY_AVATAR, null)
        btnSelected = sharPref.getInt(KEY, 1)
        chooseBotBar(btnSelected, this)

        binding.searchButton.setOnClickListener{ chooseBotBar(1, this) }
        binding.createButton.setOnClickListener {
            startActivity(Intent(this, CreateFormActivity::class.java))
        }
        binding.accountButton.setOnClickListener{
            chooseBotBar(3, this)
        }
    }

    private fun chooseBotBar(fragment: Int, context: Context) {
        with(binding) {
            when (fragment) {
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
                    if(viewModel.accName == null && viewModel.accSurname == null){
                        startActivity(Intent(context, RegistrationActivity::class.java))
                    }else {
                        searchButton.colorFilter = null
                        searchButton.isEnabled = true
                        accountButton.setColorFilter(getColor(R.color.selected), Mode.SRC_ATOP)
                        accountButton.isEnabled = false
                        btnSelected = 3
                        if (navController.currentDestination?.id != R.id.accountFragment) {
                            val bundle = Bundle().apply {
                                putString(KEY_LOGIN, viewModel.accLogin)
                                putString(KEY_PASSWORD, viewModel.accPassword)
                                putString(KEY_NAME, viewModel.accName)
                                putString(KEY_SURNAME, viewModel.accSurname)
                                putString(KEY_AVATAR, viewModel.accAvatar)
                            }
                            navController.navigate(R.id.action_search_to_accountFragment, bundle)
                        }
                    }
                }
            }
        }
    }

    override fun clearUserData() {
        viewModel.clearAll()
        chooseBotBar(1, this)
    }


    override fun onPause() {
        super.onPause()
        sharePrefEditor.putInt(KEY, btnSelected)
            .putString(KEY_LOGIN, viewModel.accLogin)
            .putString(KEY_PASSWORD, viewModel.accPassword)
            .putString(KEY_NAME, viewModel.accName)
            .putString(KEY_SURNAME, viewModel.accSurname)
            .putString(KEY_AVATAR, viewModel.accAvatar)
            .apply()
    }

    companion object{
        const val KEY = "selected bottom bar button"
        const val KEY_LOGIN = "Login"
        const val KEY_PASSWORD = "Password"
        const val KEY_NAME = "Name"
        const val KEY_SURNAME = "Surname"
        const val KEY_AVATAR = "AvatarUrl"
    }

}