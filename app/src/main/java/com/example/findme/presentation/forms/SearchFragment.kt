package com.example.findme.presentation.forms

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.FragmentSearchBinding
import com.example.findme.presentation.forms.adapter.FormsAdapter
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class SearchFragment : Fragment(), TagsAdapter.OnButtonClickListener {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var _binding : FragmentSearchBinding
    private val binding get() = _binding

    private var isExpanded = false

    private val tagList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val viewModel : FormsVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater)

        binding.searchResultView.layoutManager = LinearLayoutManager(requireContext())
        binding.tagsRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.getAllForms()

        viewModel.forms.observe(viewLifecycleOwner){ result ->
            binding.searchResultView.adapter = FormsAdapter(result)
        }

        binding.searchEditText.doAfterTextChanged {
            //viewModel.getByText(binding.searchEditText.text.toString(), tagList)
            viewModel.getByCoordinates(binding.searchEditText.text.toString(), tagList, "2.3522", "48.8566", "5")
        }

        binding.filtersButton.setOnClickListener{
            toggleViewSize()
        }

        binding.addTagButton.setOnClickListener{
            showInputDialog()
        }

        binding.clearTagsButton.setOnClickListener{
            tagList.clear()
            binding.tagsRV.adapter = TagsAdapter(tagList, this)
            updateUI()
        }

        binding.addLocationButton.setOnClickListener{
            startActivity(Intent(requireContext(), MapActivity::class.java)) // запросить разрешения на геолокацию
        }

        return binding.root
    }

    private fun toggleViewSize() {
        if (isExpanded) {
            binding.filtersLayout.visibility = View.GONE
        } else {
            binding.filtersLayout.visibility = View.VISIBLE
        }
        isExpanded = !isExpanded
    }



    private fun showInputDialog() {
        val input = EditText(requireContext())

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_tag))
            .setMessage(getString(R.string.please_write_tag))
            .setView(input)
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                val result = input.text.toString()
                if(result.isNotEmpty()){
                    tagList.add(result)
                    binding.tagsRV.adapter = TagsAdapter(tagList, this)
                    updateUI()
                }
            }
            .setNegativeButton(getString(R.string.back)) { dialog, which ->
                dialog.cancel()
            }
            .create()

        dialog.show()
    }

    override fun onButtonClick(position: Int) {
        tagList.removeAt(position)
        binding.tagsRV.adapter = TagsAdapter(tagList, this)
        updateUI()
    }

    private fun updateUI(){
        if(tagList.isNotEmpty()){
            binding.tagsRV.visibility = View.VISIBLE
        }else{
            binding.tagsRV.visibility = View.GONE
        }
        //viewModel.getByText(binding.searchEditText.text.toString(), tagList)
        viewModel.getByCoordinates(binding.searchEditText.text.toString(), tagList, "2.3522", "48.8566", "5")
    }



//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment Search.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SearchFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}