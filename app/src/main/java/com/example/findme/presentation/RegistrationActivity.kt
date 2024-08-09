package com.example.findme.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.findme.R
import com.example.findme.databinding.ActivityRegistrationBinding
import com.google.android.material.snackbar.Snackbar

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegistrationBinding
    private var status = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            if(inputIsValid()) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("Name", binding.loginEditText.text.toString())
                    putExtra("Surname", binding.passwordEditText.text.toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            else Snackbar.make(binding.root, "Invalid name or surname", Snackbar.LENGTH_SHORT).show()
        }

        binding.registrationToolbar.setNavigationOnClickListener{
            finish()
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
                }
            }
        }
    }

    private fun inputIsValid(): Boolean{
        val regex = Regex("[a-zA-Z]+(-[a-zA-Z]+)?")
        val name = binding.loginEditText.text.toString().trim()
        val surname = binding.passwordEditText.text.toString().trim()
        return (name.length >= 2 && name.matches(regex)) && (surname.trim().length >= 2 && surname.matches(regex))
    }
}