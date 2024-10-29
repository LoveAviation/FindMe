package com.example.findme.presentation.account

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.FragmentAccountBinding
import com.example.findme.other.OnDataClearListener
import com.example.findme.presentation.FavouritesVM
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.forms.EditForm
import com.example.findme.presentation.forms.FormActivity
import com.example.findme.presentation.forms.FormsVM
import com.example.findme.presentation.forms.adapter.FormsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var accLogin: String? = null
    private var accPassword: String? = null
    private var accName: String? = null
    private var accSurname: String? = null
    private var accAvatar: String? = null

    private lateinit var _binding : FragmentAccountBinding
    private val binding get() = _binding
    private val viewModel : FormsVM by viewModels()

    private val favVM : FavouritesVM by viewModels()

    private var dataClearListener: OnDataClearListener? = null

    private var myIsExpanded = false
    private var favIsExpanded = false

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
        binding.accountName.text = "$accName $accSurname"
        binding.accountLogin.text = accLogin

        favVM.getAllList()
        favVM.favForms.observe(viewLifecycleOwner){ result ->
            Log.d(TAG, "RESULT IS NULL: ${result == null}; RESULT: $result")
            if(result != null) {
                if (result.isNotEmpty()) {
                    viewModel.favouriteForms(requireContext(), result)
                }else{
                    binding.favouriteFormsRV.visibility = View.GONE
                }
            }
        }

        binding.favouriteFormsRV.layoutManager = LinearLayoutManager(requireContext())
        viewModel.favouriteForms.observe(viewLifecycleOwner){ result ->
            if (result != null) {
                binding.favouriteFormsRV.adapter = FormsAdapter(result) { selectedItem ->
                    val intent = Intent(requireContext(), FormActivity::class.java)
                    intent.putExtra(FormActivity.KEY_ID, selectedItem.id)
                    intent.putExtra(FormActivity.KEY_TITLE, selectedItem.title)
                    intent.putExtra(FormActivity.KEY_DESCRIPTION, selectedItem.description)
                    intent.putExtra(FormActivity.KEY_TAGS, listFormToBasic(selectedItem.tags))
                    intent.putExtra(FormActivity.KEY_AUTHOR, selectedItem.author)
                    intent.putExtra(FormActivity.KEY_AVATAR, selectedItem.author_avatar)
                    intent.putExtra(FormActivity.KEY_LOCATION, selectedItem.location)

                    intent.putExtra(FormActivity.KEY_FAVOURITE, true)
                    startActivity(intent)
                }
            }
        }

        binding.favouriteForms.setOnClickListener{
            if(favIsExpanded){
                binding.favouriteFormsRV.visibility = View.GONE
                favIsExpanded = false
            }else{
                binding.favouriteFormsRV.visibility = View.VISIBLE
                favIsExpanded = true
            }
        }

        if (accAvatar != "" && accAvatar != "null"){
            Glide.with(this)
                .load(accAvatar)
                .circleCrop()
                .into(binding.AvatarImage)
        }

        binding.exitAccount.setOnClickListener {
            showAlertDialog()
        }

        viewModel.getAccountForms(requireContext(), accLogin.toString())
        viewModel.forms.observe(viewLifecycleOwner){ forms ->
            if (forms != null) {
                binding.myFormsRV.adapter = FormsAdapter(forms) { selectedItem ->
                    val intent = Intent(requireContext(), EditForm::class.java)
                    intent.putExtra(FormActivity.KEY_ID, selectedItem.id)
                    intent.putExtra(FormActivity.KEY_TITLE, selectedItem.title)
                    intent.putExtra(FormActivity.KEY_DESCRIPTION, selectedItem.description)
                    intent.putExtra(FormActivity.KEY_TAGS, listFormToBasic(selectedItem.tags))
                    intent.putExtra(FormActivity.KEY_AUTHOR, selectedItem.author)
                    intent.putExtra(FormActivity.KEY_AVATAR, selectedItem.author_avatar)
                    intent.putExtra(FormActivity.KEY_LOCATION, selectedItem.location)
                    intent.putExtra(FormActivity.KEY_LOGIN, selectedItem.author_login)
                    intent.putExtra(MainActivity.KEY_NAME, accName)
                    intent.putExtra(MainActivity.KEY_SURNAME, accSurname)
                    startActivity(intent)
                }
            }
        }
        binding.myFormsRV.layoutManager = LinearLayoutManager(requireContext())

        binding.myForms.setOnClickListener{
            if(myIsExpanded){
                binding.myFormsRV.visibility = View.GONE
                myIsExpanded = false
            }else{
                binding.myFormsRV.visibility = View.VISIBLE
                myIsExpanded = true
            }
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
            favVM.deleteAll()
            dataClearListener?.clearUserData()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.back)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun listFormToBasic(input: List<String>): String{
        if (input.isEmpty()) return ""
        return input.joinToString(" ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "ON RESUMEEEE")
        viewModel.getAccountForms(requireContext(), accLogin.toString())
        favVM.getAllList()
    }
}