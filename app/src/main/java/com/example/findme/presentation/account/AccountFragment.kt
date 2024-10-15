package com.example.findme.presentation.account

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.FragmentAccountBinding
import com.example.findme.other.OnDataClearListener
import com.example.findme.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var accLogin: String? = null
    private var accPassword: String? = null
    private var accName: String? = null
    private var accSurname: String? = null
    private var accAvatar: String? = null

    private lateinit var _binding : FragmentAccountBinding
    private val binding get() = _binding

    private var dataClearListener: OnDataClearListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
            dataClearListener = activity as OnDataClearListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            accLogin = it.getString(MainActivity.KEY_LOGIN)
            accPassword = it.getString(MainActivity.KEY_PASSWORD)
            accName = it.getString(MainActivity.KEY_NAME)
            accSurname = it.getString(MainActivity.KEY_SURNAME)
            accAvatar = it.getString(MainActivity.KEY_AVATAR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.accountName.text = "$accSurname $accName"
        binding.accountLogin.text = accLogin

        if (accAvatar != "" && accAvatar != "null"){ // НАДО ИСПРАВИТЬ "null"
            Glide.with(this)
                .load(accAvatar)
                .circleCrop()
                .into(binding.AvatarImage)
        }

        binding.exitAccount.setOnClickListener {
            showAlertDialog()
        }

        binding.editButton.setOnClickListener{
            startActivity(Intent(requireContext(), EditAccount::class.java).apply {
                putExtra(MainActivity.KEY_LOGIN, accLogin)
                putExtra(MainActivity.KEY_PASSWORD, accPassword)
                putExtra(MainActivity.KEY_NAME, accName)
                putExtra(MainActivity.KEY_SURNAME, accSurname)
                putExtra(MainActivity.KEY_AVATAR, accAvatar)
            })
        }
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(getString(R.string.warning_message))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dataClearListener?.clearUserData()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.back)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

}