package com.example.findme.presentation.forms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.FragmentSearchBinding
import com.example.findme.presentation.FavouritesVM
import com.example.findme.presentation.forms.adapter.FormsAdapter
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
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
    private var favouritesList : MutableList<Int>? = null

    private lateinit var mapResultLauncher: ActivityResultLauncher<Intent>

    private var longitude : String? = null
    private var latitude : String? = null
    private var radius : Int = 10

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
                binding.selectedCoordinates.text =
                    getString(R.string.longitude_latitude, longitude, latitude)
            }else{
                binding.selectedCoordinates.text = getString(R.string.you_haven_t_selected_coordinates)
            }
            search()
        }
    }

    private val viewModel : FormsVM by viewModels()

    private val favVM : FavouritesVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        favVM.getAllList()
        favVM.favForms.observe(viewLifecycleOwner){ result ->
            if(result != null){
                favouritesList = result.toMutableList()
            }
        }

        _binding = FragmentSearchBinding.inflate(layoutInflater)

        binding.searchResultView.layoutManager = LinearLayoutManager(requireContext())
        binding.tagsRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.getAllForms(requireContext())

        viewModel.forms.observe(viewLifecycleOwner){ result ->
            if (result != null) {
                binding.loadingBar.visibility = View.GONE
                if (result.isEmpty()) {
                    binding.nothingText.visibility = View.VISIBLE
                }

                binding.searchResultView.adapter = FormsAdapter(requireContext(), result) { selectedItem ->
                    var isFavourite = false
                    if (favouritesList != null) {
                        for (id: Int in favouritesList) {
                            if (id == selectedItem.id) {
                                isFavourite = true
                                break
                            }
                        }
                    }
                    val intent = Intent(requireContext(), FormActivity::class.java)
                    intent.putExtra(FormActivity.KEY_ID, selectedItem.id)
                    intent.putExtra(FormActivity.KEY_TITLE, selectedItem.title)
                    intent.putExtra(FormActivity.KEY_DESCRIPTION, selectedItem.description)
                    intent.putExtra(FormActivity.KEY_TAGS, listFormToBasic(selectedItem.tags))
                    intent.putExtra(FormActivity.KEY_AUTHOR, selectedItem.author)
                    intent.putExtra(FormActivity.KEY_AVATAR, selectedItem.authorAvatar)
                    intent.putExtra(FormActivity.KEY_LOCATION, selectedItem.location)

                    intent.putExtra(FormActivity.KEY_FAVOURITE, isFavourite)
                    startActivity(intent)
                }
            }
        }


        binding.searchEditText.afterChangeWithDebounce {
            search()
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

        binding.clearLocationButton.setOnClickListener{
            longitude = null
            latitude = null
            binding.selectedCoordinates.text = getString(R.string.you_haven_t_selected_coordinates)
            search()
        }

        binding.addLocationButton.setOnClickListener{
            mapResultLauncher.launch(Intent(requireContext(), MapActivity::class.java))
        }

        binding.locationFilterSwitch.setOnCheckedChangeListener{ _, isActivated ->
            search()

            binding.addLocationButton.visibility = if(isActivated) View.VISIBLE else View.GONE
            binding.clearLocationButton.visibility = if(isActivated) View.VISIBLE else View.GONE
            binding.selectedCoordinates.visibility = if(isActivated) View.VISIBLE else View.GONE
            binding.radiusButton.visibility = if(isActivated) View.VISIBLE else View.GONE
            binding.radiusText.visibility = if(isActivated) View.VISIBLE else View.GONE
            binding.radiusText2.visibility = if(isActivated) View.VISIBLE else View.GONE
        }

        binding.radiusButton.setOnClickListener{
            showRadiusPickerDialog()
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    fun showRadiusPickerDialog() {
        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = 100
        }

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.choose_radius))
            .setView(numberPicker)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                radius = numberPicker.value
                binding.radiusButton.text = radius.toString()
                search()
            }
            .setNegativeButton(getString(R.string.back), null)
            .create()
            .show()
    }

    private fun toggleViewSize() {
        binding.filtersLayout.visibility = if(isExpanded) View.GONE else View.VISIBLE
        isExpanded = !isExpanded
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

    private fun showInputDialog() {
        val input = EditText(requireContext())
        input.filters = arrayOf(InputFilter.LengthFilter(30))
        input.inputType = InputType.TYPE_CLASS_TEXT

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_tag))
            .setMessage(getString(R.string.please_write_tag))
            .setView(input)
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                val result = input.text.toString().trim().lowercase().replace(" ", "_")
                if (result.isNotEmpty()) {
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
        search()
    }

    private fun search() {
        val input = binding.searchEditText.text.toString().replace(",", "")
        if (isInternetAvailable()) {
            binding.nothingText.text = getString(R.string.nothing_was_found_for_your_request)
            binding.searchEditText.isEnabled = true
            binding.filtersButton.isEnabled = true
            binding.nothingText.visibility = View.GONE
            binding.loadingBar.visibility = View.VISIBLE
            if (input.isNotEmpty() || tagList.isNotEmpty() || (longitude != null && latitude != null)) {
                if (binding.locationFilterSwitch.isChecked && longitude != null && latitude != null) {
                    viewModel.getWithCoordinates(requireContext(), binding.searchEditText.text.toString(), tagList, longitude.toString(), latitude.toString(), (radius*1000).toString())
                } else {
                    viewModel.getByText(requireContext(),binding.searchEditText.text.toString(), tagList)
                }
            } else {
                viewModel.getAllForms(requireContext())
            }
        }else{
            binding.loadingBar.visibility = View.GONE
            binding.nothingText.text = getString(R.string.no_internet_connection)
            binding.nothingText.visibility = View.VISIBLE
            binding.searchEditText.isEnabled = false
            binding.filtersButton.isEnabled = false
            binding.searchResultView.adapter = FormsAdapter(requireContext(), listOf()){}
            viewModel.viewModelScope.launch{
                delay(1500)
                search()
            }
        }
    }

    private fun listFormToBasic(input: List<String>): String{
        if (input.isEmpty()) return ""
        return input.joinToString(", ")
    }

    override fun onResume() {
        super.onResume()
        favVM.getAllList()
        search()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val network = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}
