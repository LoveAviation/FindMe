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
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.account_fb.other.ErrorStates
import com.example.findme.R
import com.example.findme.databinding.FragmentAccountBinding
import com.example.findme.other.OnDataClearListener
import com.example.findme.presentation.FavouritesVM
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.forms.FormsVM
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.getValue
import androidx.core.content.edit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.findme.presentation.forms.ExtraFormsActivity

/**
 * Fragment для просмотра и действия с аккаунтом пользователя
 */

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
    private val viewModel2 : AccountVM by viewModels()

    private val favVM : FavouritesVM by viewModels()

    private var dataClearListener: OnDataClearListener? = null

    private var isDeleting = false

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
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountName.text = "$accName $accSurname"
        binding.accountLogin.text = accLogin

        viewModel2.error.observe(viewLifecycleOwner){ result ->
            if(result == ErrorStates.ERROR){
                Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                binding.deleteAccount.isEnabled = true
                binding.deletingProgressBar.visibility = View.GONE
            }
        }

        binding.favouriteForms.setOnClickListener{
            val intent = Intent(requireContext(), ExtraFormsActivity::class.java)
            intent.putExtra(ExtraFormsActivity.KEY, ExtraFormsActivity.FAVOURITE)
            startActivity(intent)
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

        binding.myForms.setOnClickListener{
            val intent = Intent(requireContext(), ExtraFormsActivity::class.java)
            intent.putExtra(ExtraFormsActivity.KEY, ExtraFormsActivity.MY_FORMS)
            intent.putExtra(ExtraFormsActivity.LOGIN, accLogin)
            intent.putExtra(ExtraFormsActivity.NAME, accName)
            intent.putExtra(ExtraFormsActivity.SURNAME, accSurname)
            startActivity(intent)
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

        binding.deleteAccount.setOnClickListener{
            showDangerousWarn()
        }

        viewModel2.deletingState.observe(viewLifecycleOwner){ result ->
            if(result == true){
                viewModel.deleteAllForms(requireContext(), accLogin.toString())
            }
        }

        viewModel.formDeletingResult.observe(viewLifecycleOwner){ result ->
            if(result == true){
                favVM.deleteAll()
                dataClearListener?.clearUserData()
            }else if(result == false){
                binding.deleteAccount.isEnabled = true
                binding.deletingProgressBar.visibility = View.GONE
                Snackbar.make(binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_SHORT).show()
            }
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

    private fun showDangerousWarn() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.are_you_sure_want_to_delete_account))
        builder.setMessage(getString(R.string.this_action_will_delete_account_and_all_forms_of_it))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            viewModel2.deleteAccount(viewLifecycleOwner, accLogin.toString())
            binding.deleteAccount.isEnabled = false
            binding.exitAccount.isEnabled = false
            binding.myForms.isEnabled = false
            binding.favouriteForms.isEnabled = false
            binding.editButton.isEnabled = false
            binding.deletingProgressBar.visibility = View.VISIBLE
            isDeleting = true
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.back)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAccountForms(requireContext(), accLogin.toString())
        favVM.getAllList()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isDeleting){
            val inputData = workDataOf(
                DelContinueWorker.LOGIN_FOR_WORKER to accLogin,
            )

            val workRequest = OneTimeWorkRequestBuilder<DelContinueWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(requireContext()).enqueue(workRequest)
        }
    }
}
