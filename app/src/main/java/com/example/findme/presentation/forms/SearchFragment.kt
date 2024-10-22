package com.example.findme.presentation.forms

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.FragmentSearchBinding
import com.example.findme.presentation.forms.adapter.FormsAdapter
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import com.example.forms_sup.entity.Form
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(), TagsAdapter.OnButtonClickListener {
    private lateinit var _binding : FragmentSearchBinding
    private val binding get() = _binding

    private var isExpanded = false

    private val tagList: MutableList<String> = mutableListOf()

    private lateinit var mapResultLauncher: ActivityResultLauncher<Intent>

    private var longitude : String? = null
    private var latitude : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                longitude = result.data?.getStringExtra(MapActivity.LONGITUDE_KEY)
                latitude = result.data?.getStringExtra(MapActivity.LATITUDE_KEY)
            }

            if(longitude != null && latitude != null){
                binding.selectedCoordinates.text = "Longitude: $longitude; Latitude: $latitude"
            }else{
                binding.selectedCoordinates.text = getString(R.string.you_haven_t_selected_coordinates)
            }
            search(binding.searchEditText.text.toString())
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
            binding.searchResultView.adapter = FormsAdapter(result) { selectedItem ->
                val intent = Intent(requireContext(), FormActivity::class.java)
                intent.putExtra(FormActivity.KEY_TITLE, selectedItem.title)
                intent.putExtra(FormActivity.KEY_DESCRIPTION, selectedItem.description)
                intent.putExtra(FormActivity.KEY_TAGS, listFormToBasic(selectedItem.tags))
                intent.putExtra(FormActivity.KEY_AUTHOR, selectedItem.author)
                intent.putExtra(FormActivity.KEY_AVATAR, selectedItem.author_avatar)
                startActivity(intent)
            }
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
            mapResultLauncher.launch(Intent(requireContext(), MapActivity::class.java))
        }

        binding.locationFilterSwitch.setOnCheckedChangeListener{ _, isActivated ->
            search(binding.searchEditText.text.toString())

            binding.addLocationButton.visibility = if(isActivated) View.VISIBLE else View.GONE
            binding.selectedCoordinates.visibility = if(isActivated) View.VISIBLE else View.GONE
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
        if(input!!.isNotEmpty() || tagList.isNotEmpty() || (longitude != null && latitude != null)){
            if(binding.locationFilterSwitch.isChecked){
                viewModel.getWithCoordinates(binding.searchEditText.text.toString(), tagList, longitude.toString(), latitude.toString(), "200000")
            }else{
                viewModel.getByText(binding.searchEditText.text.toString(), tagList)
            }
        }else{
            viewModel.getAllForms()
        }
    }

    private fun listFormToBasic(input: List<String>): String{
        if (input.isEmpty()) return ""
        return input.joinToString(", ")
    }

}
