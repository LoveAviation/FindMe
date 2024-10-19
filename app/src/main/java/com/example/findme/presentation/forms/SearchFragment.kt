package com.example.findme.presentation.forms

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.FragmentSearchBinding
import com.example.findme.presentation.forms.adapter.FormsAdapter
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            binding.loadingBar.visibility = View.GONE
            if(result.isEmpty()){
               binding.nothingText.visibility = View.VISIBLE
            }
            binding.searchResultView.adapter = FormsAdapter(result)
        }

        binding.searchEditText.afterChangeWithDebounce { string ->
            search(string)
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
            startActivity(Intent(requireContext(), MapActivity::class.java))
        }

        binding.locationFilterSwitch.setOnCheckedChangeListener{ _, isActivated ->
            search(binding.searchEditText.text.toString())

            binding.addLocationButton.visibility = if(isActivated) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    private fun toggleViewSize() {
        binding.filtersLayout.visibility = if(isExpanded) View.GONE else View.VISIBLE
        isExpanded = !isExpanded
    }

    fun EditText.afterChangeWithDebounce(debounceTime: Long = 500L, onDebouncedInput: (String) -> Unit) {
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
        search(binding.searchEditText.text.toString())
    }

    private fun search(input: String?) {
        binding.nothingText.visibility = View.GONE
        binding.loadingBar.visibility = View.VISIBLE
        if(input!!.isNotEmpty()){
            if(binding.locationFilterSwitch.isActivated){
                viewModel.getWithCoordinates(binding.searchEditText.text.toString(), tagList, "2.3522", "48.8566", "5")
            }else{
                viewModel.getByText(binding.searchEditText.text.toString(), tagList)
            }
        }else{
            viewModel.getAllForms()
        }
    }
}
