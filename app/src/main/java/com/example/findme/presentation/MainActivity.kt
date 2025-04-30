package com.example.findme.presentation


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PorterDuff.Mode
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.findme.R
import com.example.findme.databinding.ActivityMainBinding
import com.example.findme.other.OnDataClearListener
import com.example.findme.other.SaveDataClass
import com.example.findme.presentation.account.AccountVM
import com.example.findme.presentation.account.RegistrationActivity
import com.example.findme.presentation.forms.CreateFormActivity
import com.example.findme.presentation.forms.FormsVM
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.getValue

/**
 * Главный класс
 * В нем хранятся данные об аккаунте и идет работа Toolbar'a
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnDataClearListener {
    private lateinit var binding : ActivityMainBinding
    private var btnSelected = 1
    private lateinit var sharPref: SharedPreferences
    private lateinit var sharePrefEditor: SharedPreferences.Editor
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private val accData  = SaveDataClass()

    private var userChecked = false

    private val viewModel : AccountVM by viewModels()
    private val viewModel2 : FormsVM by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("message")
            if(message == "deleted") clearUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadLocale()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val preferences = getSharedPreferences("settings", MODE_PRIVATE)
        val theme = preferences.getString("theme", "def")
        AppCompatDelegate.setDefaultNightMode(
            if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES
            else if (theme == "light") AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        setContentView(binding.root)

        sharPref = getSharedPreferences("MainSharedPref", MODE_PRIVATE)
        sharePrefEditor = sharPref.edit()
        navHostFragment  = supportFragmentManager.findFragmentById(R.id.MainFragmentHost) as NavHostFragment
        navController = navHostFragment.navController

        accData.accLogin = intent?.getStringExtra(KEY_LOGIN) ?:  sharPref.getString(KEY_LOGIN, null)
        accData.accPassword = intent?.getStringExtra(KEY_PASSWORD) ?:  sharPref.getString(KEY_PASSWORD, null)
        accData.accName = intent?.getStringExtra(KEY_NAME) ?: sharPref.getString(KEY_NAME, null)
        accData.accSurname = intent?.getStringExtra(KEY_SURNAME) ?:  sharPref.getString(KEY_SURNAME, null)
        accData.accAvatar = intent?.getStringExtra(KEY_AVATAR) ?:  sharPref.getString(KEY_AVATAR, null)
        btnSelected = sharPref.getInt(KEY, 1)
        chooseBotBar(btnSelected)
        if(accData.accLogin != null){
            var login : String = accData.accLogin!!
            viewModel.checkUser(this, login)
            viewModel.checkState.observe(this){ result ->
                if (result == false){
                    viewModel.deleteAccount(this, login)
                    viewModel.deletingState.observe(this){ state ->
                        if(state == true){
                            viewModel2.deleteAllForms(this, login)
                            viewModel2.formDeletingResult.observe(this) { value ->
                                if (value == true) { clearUserData(); userChecked = true }
                            }
                        }
                    }
                }else{
                    userChecked = true
                }
            }
        }else{
            userChecked = true
        }

        val filter = IntentFilter("com.example.broadcast.MY_NOTIFICATION")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        binding.searchButton.setOnClickListener{ chooseBotBar(1) }
        binding.createButton.setOnClickListener {
            if(accData.accName == null && accData.accSurname == null){
                goToRegistration()
            }else{
                if (userChecked) {
                    var intent = Intent(this, CreateFormActivity::class.java).apply {
                        putExtra(KEY_LOGIN, accData.accLogin)
                        putExtra(KEY_NAME, accData.accName)
                        putExtra(KEY_SURNAME, accData.accSurname)
                        putExtra(KEY_AVATAR, accData.accAvatar)
                    }
                    startActivity(intent)
                }else{
                    Toast.makeText(this, getString(R.string.checking_in_progress_please_wait), Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.accountButton.setOnClickListener{
            if (userChecked) {
                chooseBotBar(3)
            }else{
                Toast.makeText(this, getString(R.string.checking_in_progress_please_wait), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun chooseBotBar(fragment: Int) {
        with(binding) {
            when (fragment) {
                1 -> {
                    searchButton.setColorFilter(getColor(R.color.primaryColor), Mode.SRC_ATOP)
                    searchButton.isEnabled = false
                    accountButton.colorFilter = null
                    accountButton.isEnabled = true
                    btnSelected = 1
                    if(navController.currentDestination?.id != R.id.search){ navController.navigate(
                        R.id.action_accountFragment_to_search
                    )}
                }

                3 -> {
                    if(accData.accName == null && accData.accSurname == null){
                        goToRegistration()
                    }else {
                        searchButton.colorFilter = null
                        searchButton.isEnabled = true
                        accountButton.setColorFilter(getColor(R.color.primaryColor), Mode.SRC_ATOP)
                        accountButton.isEnabled = false
                        btnSelected = 3
                        if (navController.currentDestination?.id != R.id.accountFragment) {
                            val bundle = Bundle().apply {
                                putString(KEY_LOGIN, accData.accLogin)
                                putString(KEY_PASSWORD, accData.accPassword)
                                putString(KEY_NAME, accData.accName)
                                putString(KEY_SURNAME, accData.accSurname)
                                putString(KEY_AVATAR, accData.accAvatar)
                            }
                            navController.navigate(R.id.action_search_to_accountFragment, bundle)
                        }
                    }
                }
            }
        }
    }

    private fun goToRegistration(){
        if(isInternetAvailable()){
            startActivity(Intent(this, RegistrationActivity::class.java))
        }else{
            Snackbar.make(binding.root, getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun clearUserData() {
        accData.clearAll()
        chooseBotBar(1)
    }


    override fun onPause() {
        super.onPause()
        sharePrefEditor.putInt(KEY, btnSelected)
            .putString(KEY_LOGIN, accData.accLogin)
            .putString(KEY_PASSWORD, accData.accPassword)
            .putString(KEY_NAME, accData.accName)
            .putString(KEY_SURNAME, accData.accSurname)
            .putString(KEY_AVATAR, accData.accAvatar)
            .apply()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
        val network = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun loadLocale() {
        val preferences = getSharedPreferences("settings", MODE_PRIVATE)
        val languageCode = preferences.getString("language", Locale.getDefault().language) ?: Locale.getDefault().language

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }


    companion object{
        const val KEY = "selected bottom bar button"
        const val KEY_LOGIN = "Login"
        const val KEY_PASSWORD = "Password"
        const val KEY_NAME = "Name"
        const val KEY_SURNAME = "Surname"
        const val KEY_AVATAR = "AvatarUrl"

        var instance: MainActivity? = null
    }

}