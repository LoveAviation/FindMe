package com.example.findme.presentation.account

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.account_fb.entity.Account
import com.example.findme.databinding.ActivityEditAccountBinding
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.MainActivity.Companion.KEY_AVATAR
import com.example.findme.presentation.MainActivity.Companion.KEY_LOGIN
import com.example.findme.presentation.MainActivity.Companion.KEY_NAME
import com.example.findme.presentation.MainActivity.Companion.KEY_PASSWORD
import com.example.findme.presentation.MainActivity.Companion.KEY_SURNAME
import com.example.findme.R
import com.example.findme.presentation.forms.FormsVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity, которое открывается при изменении аккаунта
 */

@AndroidEntryPoint
class EditAccount : AppCompatActivity() {

    private lateinit var binding: ActivityEditAccountBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var localURI: Uri? = null

    private val viewModel : AccountVM by viewModels()
    private val supabaseViewModel : FormsVM by viewModels()

    private lateinit var login : String
    private lateinit var password : String
    private lateinit var name : String
    private lateinit var surname : String
    private var avatar : String? = null

    private var avatarIsChanged = false

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

        binding.clearButton.setOnClickListener{
            avatar = null
            avatarIsChanged = true
            binding.avatar.setImageResource(R.drawable.profile_button)
        }

        binding.avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            avatarIsChanged = true
            imagePickerLauncher.launch(intent)
        }

        binding.passwordEditText.afterChangeWithDebounce{ input ->
            if(input.length in 1..5){
                binding.passwordEditText.error = getString(R.string.password_can_not_have_less_then_6_symbols)
            }else{
                binding.passwordEditText.error = null
            }
        }

        binding.nameEditText.afterChangeWithDebounce{ input ->
            if(input.length in 1..2){
                binding.nameEditText.error = getString(R.string.name_can_not_have_less_then_3_symbols)
            }else{
                binding.passwordEditText.error = null
            }
        }

        binding.surnameEditText.afterChangeWithDebounce{ input ->
            if(input.length in 1..2){
                binding.surnameEditText.error = getString(R.string.surname_can_not_have_less_then_3_symbols)
            }else{
                binding.passwordEditText.error = null
            }
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

                if (avatarIsChanged || avatar == "") {
                    startLoading()
                    viewModel.edit(this, login, nameHere, surnameHere, passwordHere, localURI.toString())
                } else {
                    startLoading()
                    viewModel.edit(this, login, nameHere, surnameHere, passwordHere, avatar)
                }

                viewModel.editState.observe(this){ avatarResult ->
                    if(avatarResult == "null"){
                        returnData(login, Account(passwordHere, nameHere, surnameHere, avatar))
                    }else{
                        returnData(login, Account(passwordHere, nameHere, surnameHere, avatarResult))
                    }
                }

            }else{
                Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show()
                stopLoading()
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
        avatar = intent?.getStringExtra(KEY_AVATAR)

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
        updateAccInfoInSupabase(account.urlAvatar)
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
    private fun updateAccInfoInSupabase(authorAvatar: String?){
        var newName = name
        var newSurname = surname
        var newAvatar: String? = null
        if(binding.nameEditText.text!!.isNotEmpty()){
            newName = binding.nameEditText.text.toString()
        }
        if(binding.surnameEditText.text!!.isNotEmpty()){
            newSurname = binding.surnameEditText.text.toString()
        }
        if(authorAvatar!!.isNotEmpty()){
            newAvatar = authorAvatar
        }
        supabaseViewModel.updateAccInfo(this,login, "$newName $newSurname", newAvatar)
    }

    private fun startLoading(){
        binding.submitButton.isEnabled = false
        binding.loadingBar.visibility = View.VISIBLE
        binding.clearButton.isEnabled = false
    }

    private fun stopLoading() {
        binding.submitButton.isEnabled = true
        binding.loadingBar.visibility = View.GONE
    }

    private fun EditText.afterChangeWithDebounce(debounceTime: Long = 500L, onDebouncedInput: (String) -> Unit) {
        var debounceJob: Job? = null

        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                debounceJob?.cancel()
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(debounceTime)
                    s?.toString()?.let { onDebouncedInput(it) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 100
    }
}