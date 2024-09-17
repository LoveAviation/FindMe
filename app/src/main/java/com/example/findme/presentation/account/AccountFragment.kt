package com.example.findme.presentation.account

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.FragmentAccountBinding
import com.example.findme.domain.OnDataClearListener
import com.example.findme.presentation.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = MainActivity.KEY_NAME
private const val ARG_PARAM2 = MainActivity.KEY_SURNAME
private const val ARG_PARAM3 = MainActivity.KEY_AVATAR

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var param3: String? = null

    private lateinit var _binding : FragmentAccountBinding
    private val binding get() = _binding

    private var dataClearListener: OnDataClearListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dataClearListener = activity as OnDataClearListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnDataClearListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            param3 = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.accountName.text = param1
        binding.accountSurname.text = param2
        Glide.with(this)
            .load(param3)
            .circleCrop()
            .into(binding.AvatarImage)

        binding.exitAccount.setOnClickListener {
            showAlertDialog()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}