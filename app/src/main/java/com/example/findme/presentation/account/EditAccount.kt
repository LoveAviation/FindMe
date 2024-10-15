package com.example.findme.presentation.account

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.example.account_fb.entity.Account
import com.example.findme.databinding.ActivityEditAccountBinding
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.MainActivity.Companion.KEY_AVATAR
import com.example.findme.presentation.MainActivity.Companion.KEY_LOGIN
import com.example.findme.presentation.MainActivity.Companion.KEY_NAME
import com.example.findme.presentation.MainActivity.Companion.KEY_PASSWORD
import com.example.findme.presentation.MainActivity.Companion.KEY_SURNAME
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditAccount : AppCompatActivity() {

    private lateinit var binding: ActivityEditAccountBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var localURI: Uri? = null

    private val viewModel : AccountVM by viewModels()

    private lateinit var login : String
    private lateinit var password : String
    private lateinit var name : String
    private lateinit var surname : String
    private lateinit var avatar : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadOldAccount()
        checkPermissions()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    localURI = uri
                    setImage(uri.toString())
                }
            }
        }

        binding.editToolbar.setOnClickListener{
            finish()
        }

        binding.avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.submitButton.setOnClickListener{
            if (inputIsValid()) {
                var nameHere : String = ""
                var surnameHere : String = ""
                var passwordHere : String = ""

                with(binding) {
                    nameHere = if (nameEditText.text!!.isNotEmpty()) { nameEditText.text.toString() } else { name }
                    surnameHere = if (surnameEditText.text!!.isNotEmpty()) { surnameEditText.text.toString() } else { surname }
                    passwordHere = if (passwordEditText.text!!.isNotEmpty()) { passwordEditText.text.toString() } else { password }
                }
                if(localURI != null){
                    viewModel.edit(this, login, nameHere, surnameHere, passwordHere, localURI.toString())
                }else{
                    viewModel.edit(this, login, nameHere, surnameHere, passwordHere, null)
                }

                viewModel.editState.observe(this){ avatarResult ->
                    if(avatarResult == "null"){
                        returnData(login, Account(passwordHere, nameHere, surnameHere, avatar))
                    }else{
                        returnData(login, Account(passwordHere, nameHere, surnameHere, avatarResult))
                    }
                }

            }else{
                Toast.makeText(this, "Invalid input", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun loadOldAccount(){
        if (intent?.getStringExtra(KEY_AVATAR) != "" && intent?.getStringExtra(KEY_AVATAR) != "null")
            Glide.with(binding.root)
                .load(intent.getStringExtra(KEY_AVATAR))
                .circleCrop()
                .into(binding.avatar)

        login = intent?.getStringExtra(KEY_LOGIN).toString()
        password = intent?.getStringExtra(KEY_PASSWORD).toString()
        name = intent?.getStringExtra(KEY_NAME).toString()
        surname = intent?.getStringExtra(KEY_SURNAME).toString()
        avatar = intent?.getStringExtra(KEY_AVATAR).toString()

        with(binding){
            nameEditText.hint = name
            surnameEditText.hint = surname
            passwordEditText.hint = password
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        }
    }

    private fun inputIsValid():Boolean{
        return (binding.passwordEditText.text!!.length >= 6 || binding.passwordEditText.text!!.isEmpty()
                && namesAreValid())
    }

    private fun namesAreValid(): Boolean{
        val namesRegex = Regex("^\\p{L}+$")
        val name = binding.nameEditText.text.toString().trim()
        val surname = binding.surnameEditText.text.toString().trim()
        return ((name.length >= 3 && name.matches(namesRegex)) || name.isEmpty()) && ((surname.trim().length >= 3 && surname.matches(namesRegex)) || surname.isEmpty())
    }


    private fun returnData(login: String, account: Account){
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(KEY_LOGIN, login)
            putExtra(KEY_PASSWORD, account.password)
            putExtra(KEY_NAME, account.name)
            putExtra(KEY_SURNAME, account.surname)
            putExtra(KEY_AVATAR, account.urlAvatar)
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
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