package com.example.findme.presentation


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
import com.example.findme.domain.OnDataClearListener
import com.example.findme.presentation.account.RegistrationActivity
import com.example.findme.presentation.forms.CreateFormActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnDataClearListener, GestureDetector.OnGestureListener {
    private lateinit var binding : ActivityMainBinding
    private var btnSelected = 1
    private lateinit var sharPref: SharedPreferences
    private lateinit var sharePrefEditor: SharedPreferences.Editor
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private val viewModel : SaveDataVM by viewModels()

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gestureDetector = GestureDetector(this, this, Handler(Looper.getMainLooper()))

        sharPref = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        sharePrefEditor = sharPref.edit()
        navHostFragment  = supportFragmentManager.findFragmentById(R.id.MainFragmentHost) as NavHostFragment
        navController = navHostFragment.navController


        viewModel.accLogin = intent?.getStringExtra(KEY_LOGIN) ?:  sharPref.getString(KEY_LOGIN, null)
        viewModel.accPassword = intent?.getStringExtra(KEY_PASSWORD) ?:  sharPref.getString(KEY_PASSWORD, null)
        viewModel.accName = intent?.getStringExtra(KEY_NAME) ?: sharPref.getString(KEY_NAME, null)
        viewModel.accSurname = intent?.getStringExtra(KEY_SURNAME) ?:  sharPref.getString(KEY_SURNAME, null)
        viewModel.accAvatar = intent?.getStringExtra(KEY_AVATAR) ?:  sharPref.getString(KEY_AVATAR, null)
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
                            putString(KEY_AVATAR, viewModel.accAvatar)
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
            .putString(KEY_LOGIN, viewModel.accLogin)
            .putString(KEY_PASSWORD, viewModel.accPassword)
            .putString(KEY_NAME, viewModel.accName)
            .putString(KEY_SURNAME, viewModel.accSurname)
            .putString(KEY_AVATAR, viewModel.accAvatar)
            .apply()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
        }
    }

    companion object{
        const val KEY = "selected bottom bar button"
        const val KEY_LOGIN = "Login"
        const val KEY_PASSWORD = "Password"
        const val KEY_NAME = "Name"
        const val KEY_SURNAME = "Surname"
        const val KEY_AVATAR = "AvatarUrl"
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2.y - (e1!!.y)
            val diffX = e2.x - (e1.x)
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        chooseBotBar(1)
                    }
                    else {
                        chooseBotBar(3)
                    }
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }
}