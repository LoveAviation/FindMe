package com.example.findme.presentation.account

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.ActivityRegistrationBinding
import com.example.findme.other.Account
import com.example.findme.presentation.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.concurrent.thread

@AndroidEntryPoint
class RegistrationActivity: AppCompatActivity() {
    private lateinit var binding : ActivityRegistrationBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var status = 1
        private val viewModel: AccountVM by viewModels()
    private var localURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    localURI = uri
                    setImage(uri.toString())
                }
            }
        }

        checkPermissions()

        binding.button.setOnClickListener {
            val login : String = binding.loginEditText.text.toString()
            val password : String = binding.passwordEditText.text.toString()
            val name : String = binding.nameEditText.text.toString()
            val surname : String = binding.surnameEditText.text.toString()

            if(status == 2){
                viewModel.logIn(this, login, password)
                viewModel.logInState.observe(this){ account ->
                    waitForError()
                    if(account != null){
                        returnData(login, account)
                    }
                }
            }else if(status == 1 && inputIsValid()){
                viewModel.signIn(this, login, password, name, surname, localURI)
                viewModel.signInState.observe(this){ state ->
                    when(state){
                        "ENGAGED" -> Snackbar.make(binding.root, "This login is engaged", Snackbar.LENGTH_LONG).show()
                        null -> waitForError()
                        else -> returnData(login, Account(password, name, surname, state))
                    }
                }
            }else{
                Snackbar.make(binding.root, "Error", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.registrationToolbar.setNavigationOnClickListener{
            finish()
        }

        binding.avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.changeRegistration.setOnClickListener{
            when(status) {
                1 -> {
                    status = 2
                    binding.registrationToolbar.title = getString(R.string.log_in)
                    binding.button.text = getString(R.string.log_in)
                    binding.changeRegistration.text = getString(R.string.create_account)

                    binding.nameText.visibility = View.GONE
                    binding.nameTextInputLayout.visibility = View.GONE

                    binding.surnameText.visibility = View.GONE
                    binding.surnameTextInputLayout.visibility = View.GONE

                    binding.avatar.visibility = View.GONE
                    binding.avatarText.visibility = View.GONE
                }
                2 -> {
                    status = 1
                    binding.registrationToolbar.title = getString(R.string.sign_up)
                    binding.button.text = getString(R.string.sign_up)
                    binding.changeRegistration.text = getString(R.string.already_have_an_account)

                    binding.nameText.visibility = View.VISIBLE
                    binding.nameTextInputLayout.visibility = View.VISIBLE

                    binding.surnameText.visibility = View.VISIBLE
                    binding.surnameTextInputLayout.visibility = View.VISIBLE

                    binding.avatar.visibility = View.VISIBLE
                    binding.avatarText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun returnData(login: String, account: Account){
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.KEY_LOGIN, login)
            putExtra(MainActivity.KEY_PASSWORD, account.password)
            putExtra(MainActivity.KEY_NAME, account.name)
            putExtra(MainActivity.KEY_SURNAME, account.surname)
            putExtra(MainActivity.KEY_AVATAR, account.urlAvatar)
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun waitForError(){
        thread {
            Thread.sleep(5000)
            Snackbar.make(binding.root, "Something went wrong", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun inputIsValid(): Boolean{
        val namesRegex = Regex("^\\p{L}+$")
        val name = binding.nameEditText.text.toString().trim()
        val surname = binding.surnameEditText.text.toString().trim()
        return (name.length >= 2 && name.matches(namesRegex)) && (surname.trim().length >= 2 && surname.matches(namesRegex))
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        }
    }

    private fun setImage(uri: String) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.avatar)
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 100
    }
}