package com.example.findme.presentation.account

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.example.account_fb.entity.Account
import com.example.account_fb.other.ErrorStates
import com.example.findme.R
import com.example.findme.databinding.ActivityRegistrationBinding
import com.example.findme.presentation.CompressorImage
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.MainActivity.Companion.KEY_AVATAR
import com.example.findme.presentation.MainActivity.Companion.KEY_LOGIN
import com.example.findme.presentation.MainActivity.Companion.KEY_NAME
import com.example.findme.presentation.MainActivity.Companion.KEY_PASSWORD
import com.example.findme.presentation.MainActivity.Companion.KEY_SURNAME
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Activity, который открывается и отвечает за регистрацию И вход в аккаунт
 */

@AndroidEntryPoint
class RegistrationActivity: AppCompatActivity() {
    private lateinit var binding : ActivityRegistrationBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var status = 1
    private var registrationIs = false
    private val viewModel: AccountVM by viewModels()
    private var localURI: Uri? = null
    private val compressor = CompressorImage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    localURI = uri
                    setImage(uri.toString())
                }
            }
        }

        binding.loginEditText.afterChangeWithDebounce{ input ->
            if(input.length < 5){
                binding.loginEditText.error =
                    getString(R.string.login_can_not_have_less_then_3_symbols)
            }
        }

        binding.passwordEditText.afterChangeWithDebounce{ input ->
            if(input.length < 6){
                binding.passwordEditText.error =
                    getString(R.string.password_can_not_have_less_then_6_symbols)
            }
        }

        binding.nameEditText.afterChangeWithDebounce{ input ->
            if(input.length in 1..2){
                binding.nameEditText.error =
                    getString(R.string.name_can_not_have_less_then_3_symbols)
            } else if(input.contains(Regex("\\d"))){
                binding.nameEditText.error = getString(R.string.name_can_t_have_numbers)
            } else{
                binding.nameEditText.error = null
            }
        }

        binding.surnameEditText.afterChangeWithDebounce{ input ->
            if(input.length in 1..2){
                binding.surnameEditText.error =
                    getString(R.string.surname_can_not_have_less_then_3_symbols)
            } else if(input.contains(Regex("\\d"))){
                binding.surnameEditText.error = getString(R.string.surname_can_t_have_numbers)
            } else{
                binding.surnameEditText.error = null
            }
        }

        binding.button.setOnClickListener {
            val login : String = binding.loginEditText.text.toString().trim()
            val password : String = binding.passwordEditText.text.toString().trim()
            val name : String = binding.nameEditText.text.toString().trim()
            val surname : String = binding.surnameEditText.text.toString().trim()

            if(status == 2){
                if(password.isNotEmpty()
                    && password.length >= 6
                    && login.length >= 5
                    && login.isNotEmpty()){
                    startLoading()
                    viewModel.logIn(this, login, password)
                    viewModel.logInState.observe(this){ account ->
                        waitForError()
                        if(account != null){
                            returnData(login, account)
                        }
                    }
                }else{
                    Snackbar.make(binding.root, getString(R.string.please_enter_your_login_and_password), Snackbar.LENGTH_LONG).show()
                }
            }else if(status == 1 && inputIsValid() &&
                password.isNotEmpty()
                && password.length >= 6
                && login.length >= 5
                && login.isNotEmpty()){
                startLoading()
                registrationIs = true
                viewModel.signIn(this, this, login, password, name, surname, localURI)
                viewModel.signInState.observe(this){ state ->
                    when(state){
                        "ENGAGED" -> {
                            registrationIs = false
                            stopLoading()
                            Snackbar.make(binding.root,
                                getString(R.string.this_login_is_engaged), Snackbar.LENGTH_LONG).show()
                        }
                        null -> waitForError()
                        else -> {
                            registrationIs = false
                            returnData(login, Account(password, name, surname, state))
                        }
                    }
                }
            }else{
                Snackbar.make(binding.root, getString(R.string.invalid_input), Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.registrationToolbar.setNavigationOnClickListener{
            finish()
        }

        binding.clearButton.setOnClickListener {
            localURI = null
            binding.avatar.setImageResource(R.drawable.profile_button)
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
                    binding.toolbarTitle.text = getString(R.string.log_in)
                    binding.button.text = getString(R.string.log_in)
                    binding.changeRegistration.text = getString(R.string.create_account)

                    binding.nameText.visibility = View.GONE
                    binding.nameTextInputLayout.visibility = View.GONE

                    binding.surnameText.visibility = View.GONE
                    binding.surnameTextInputLayout.visibility = View.GONE

                    binding.avatar.visibility = View.GONE
                    binding.avatarText.visibility = View.GONE
                    binding.clearButton.visibility = View.GONE
                }
                2 -> {
                    status = 1
                    binding.toolbarTitle.text = getString(R.string.sign_up)
                    binding.button.text = getString(R.string.sign_up)
                    binding.changeRegistration.text = getString(R.string.already_have_an_account)

                    binding.nameText.visibility = View.VISIBLE
                    binding.nameTextInputLayout.visibility = View.VISIBLE

                    binding.surnameText.visibility = View.VISIBLE
                    binding.surnameTextInputLayout.visibility = View.VISIBLE

                    binding.avatar.visibility = View.VISIBLE
                    binding.avatarText.visibility = View.VISIBLE
                    binding.clearButton.visibility = View.VISIBLE
                }
            }
        }
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

    private fun waitForError(){
        viewModel.error.observe(this){ e ->
            if(e == ErrorStates.WRONG_PASSWORD) {
                    Snackbar.make(binding.root,
                        getString(R.string.wrong_password), Snackbar.LENGTH_LONG).show()
                    stopLoading()
            }else if(e == ErrorStates.ERROR) {
                    Snackbar.make(binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG).show()
                    registrationIs = false
                    stopLoading()
            }
        }
    }

    private fun inputFieldsStatus(status: Boolean){
        binding.loginEditText.isEnabled = status
        binding.passwordEditText.isEnabled = status
        binding.nameEditText.isEnabled = status
        binding.surnameEditText.isEnabled = status
    }

    private fun startLoading(){
        binding.loadingBar.visibility = View.VISIBLE
        binding.button.isEnabled = false
        inputFieldsStatus(false)
    }

    private fun stopLoading(){
        binding.loadingBar.visibility = View.GONE
        binding.button.isEnabled = true
        inputFieldsStatus(true)
    }

    private fun inputIsValid(): Boolean{
        val namesRegex = Regex("^\\p{L}+$")
        val name = binding.nameEditText.text.toString().trim()
        val surname = binding.surnameEditText.text.toString().trim()

        if (!(name.length >= 3 && name.matches(namesRegex) && !name.contains(Regex("\\d"))) && (surname.trim().length >= 3 && surname.matches(namesRegex) && !surname.contains(Regex("\\d")))){
            Snackbar.make(binding.root, getString(R.string.invalid_input), Snackbar.LENGTH_LONG).show()
        }
        return (name.length >= 3 && name.matches(namesRegex)) && (surname.trim().length >= 3 && surname.matches(namesRegex))
    }


    private fun setImage(uri: String) {
        Glide.with(this)
            .load(compressor.compressImage(this, uri.toUri()))
            .circleCrop()
            .into(binding.avatar)
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


    override fun onDestroy() {
        super.onDestroy()
        if (registrationIs){
            val inputData = workDataOf(
                RegCloseWorker.LOGIN_FOR_WORKER to binding.loginEditText.text?.trim().toString(),)

            val workRequest = OneTimeWorkRequestBuilder<RegCloseWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workRequest)
        }
    }
}