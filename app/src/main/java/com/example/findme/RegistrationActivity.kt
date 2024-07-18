package com.example.findme

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.findme.databinding.ActivityCreateFormBinding
import com.example.findme.databinding.ActivityRegistrationBinding
import com.google.android.material.snackbar.Snackbar

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegistrationBinding
    private var status = 1 // 1 - sign up; 2- log in

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            if(inputIsValid()) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("Name", binding.nameEditText.text.toString())
                    putExtra("Surname", binding.surnameEditText.text.toString())
                })
                finish()
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
                }
                2 -> {
                    status = 1
                    binding.registrationToolbar.title = getString(R.string.sign_up)
                    binding.button.text = getString(R.string.sign_up)
                    binding.changeRegistration.text = getString(R.string.already_have_an_account)
                }
            }
        }
    }

    fun inputIsValid(): Boolean{
        val regex = Regex("[a-zA-Z]+(-[a-zA-Z]+)?")
        val name = binding.nameEditText.text.toString().trim()
        val surname = binding.surnameEditText.text.toString().trim()
        return (name.length >= 2 && name.matches(regex)) && (surname.trim().length >= 2 && surname.matches(regex))
    }
}