package com.example.findme.presentation.forms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.FragmentSearchBinding
import com.example.findme.presentation.FavouritesVM
import com.example.findme.presentation.forms.adapter.FormsAdapter
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Fragment в котором идет поиск анкет
 */

@AndroidEntryPoint
class SearchFragment : Fragment(), TagsAdapter.OnButtonClickListener {
    private lateinit var _binding : FragmentSearchBinding
    private val binding get() = _binding

    private var isExpanded = false

    private var tagList: MutableList<String> = mutableListOf()
    private var favouritesList : MutableList<Int>? = null

    private lateinit var mapResultLauncher: ActivityResultLauncher<Intent>

    private var longitude : String? = null
    private var latitude : String? = null
    private var radius : Int = 10

    private lateinit var sharPref: SharedPreferences
    private lateinit var sharePrefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                longitude = result.data?.getStringExtra(MapActivity.LONGITUDE_KEY)
                latitude = result.data?.getStringExtra(MapActivity.LATITUDE_KEY)
            }

            sharePrefEditor.putString(SHAR_PREF_LONG, longitude)
            sharePrefEditor.putString(SHAR_PREF_LATIT, latitude)
            sharePrefEditor.apply()
            updateUI()
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


        binding.settingsButton.setOnClickListener{
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        val preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val theme = preferences.getString("theme", "def")
        AppCompatDelegate.setDefaultNightMode(
            if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES
            else if (theme == "light") AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )


        binding.btnDarkTheme.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            preferences.edit() { putString("theme", "dark") }
        }

        binding.btnLightTheme.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            preferences.edit() { putString("theme", "light") }
        }

        binding.defaultTheme.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            preferences.edit() { putString("theme", "def") }
        }

        binding.btnEnglish.setOnClickListener {
            setLocale("en") // Устанавливаем английский как пользовательский выбор
        }

        binding.btnRussian.setOnClickListener {
            setLocale("ru") // Устанавливаем русский как пользовательский выбор
        }


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

        binding.selectedCoordinates.setOnClickListener {
            openMaps(latitude!!.toDouble(), longitude!!.toDouble())
        }

        binding.searchEditText.afterChangeWithDebounce {
            sharePrefEditor.putString(SHAR_PREF_SEARCH, binding.searchEditText.text.toString()).apply()
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
            sharePrefEditor.putStringSet(SHAR_PREF_TAGS, tagList.toSet<String>()).apply()
            updateUI()
        }

        binding.clearLocationButton.setOnClickListener{
            longitude = null
            latitude = null
            binding.selectedCoordinates.text = getString(R.string.you_haven_t_selected_coordinates)
            sharePrefEditor.putString(SHAR_PREF_LONG, longitude)
            sharePrefEditor.putString(SHAR_PREF_LATIT, latitude)
            sharePrefEditor.apply()
            waitSearch()
        }

        binding.addLocationButton.setOnClickListener{
            mapResultLauncher.launch(Intent(requireContext(), MapActivity::class.java))
        }

        binding.locationFilterSwitch.setOnCheckedChangeListener{ _, isActivated ->
            sharePrefEditor.putBoolean(SHAR_PREF_SELECTED_CORDS, isActivated).apply()

            waitSearch()

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

        sharPref = requireActivity().getSharedPreferences(SHAR_PREF_KEY, MODE_PRIVATE)
        sharePrefEditor = sharPref.edit()

        binding.searchEditText.setText(sharPref.getString(SHAR_PREF_SEARCH, ""))
        tagList = sharPref.getStringSet(SHAR_PREF_TAGS, setOf<String>())!!.toMutableList<String>()
        binding.tagsRV.adapter = TagsAdapter(tagList, this)
        latitude = sharPref.getString(SHAR_PREF_LATIT, null)
        longitude = sharPref.getString(SHAR_PREF_LONG, null)
        radius = sharPref.getInt(SHAR_PREF_RADIUS, 10)
        binding.locationFilterSwitch.isChecked = sharPref.getBoolean(SHAR_PREF_SELECTED_CORDS, false)
        if(sharPref.getBoolean(SHAR_PREF_FILTERS, false)){ toggleViewSize() }
        updateUI()

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
                sharePrefEditor.putInt(SHAR_PREF_RADIUS, radius).apply()
                binding.radiusButton.text = radius.toString()
                search()
            }
            .setNegativeButton(getString(R.string.back), null)
            .create()
            .show()
    }

    private fun toggleViewSize() {
        if (isExpanded) {
            // Скрываем layout обратно вверх
            binding.filtersLayout.animate()
                .translationY(-binding.filtersLayout.height.toFloat())
                .setDuration(300)
                .withEndAction {
                    binding.filtersLayout.visibility = View.GONE
                }
        } else {
            // Показываем layout снизу вверх
            binding.filtersLayout.visibility = View.VISIBLE
            binding.filtersLayout.post {
                binding.filtersLayout.animate()
                    .translationY(0f)
                    .setDuration(300)
            }
        }
        isExpanded = !isExpanded
        sharePrefEditor.putBoolean(SHAR_PREF_FILTERS, isExpanded).apply()
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
                    sharePrefEditor.putStringSet(SHAR_PREF_TAGS, tagList.toSet<String>()).apply()
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
        sharePrefEditor.putStringSet(SHAR_PREF_TAGS, tagList.toSet<String>()).apply()
        binding.tagsRV.adapter = TagsAdapter(tagList, this)
        updateUI()
    }

    private fun updateUI(){
        if(tagList.isNotEmpty()){
            binding.tagsRV.visibility = View.VISIBLE
        }else{
            binding.tagsRV.visibility = View.GONE
        }
        if(longitude != null && latitude != null){
            binding.selectedCoordinates.text = getString(R.string.tap_to_see_chosen_location)
        }else{
            binding.selectedCoordinates.text = getString(R.string.you_haven_t_selected_coordinates)
        }
        binding.radiusButton.text = radius.toString()
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

    companion object{
        private const val SHAR_PREF_KEY = "search_key"
        private const val SHAR_PREF_FILTERS = "search_is_filters_expanded"
        private const val SHAR_PREF_SEARCH = "search_input"
        private const val SHAR_PREF_TAGS = "search_tags"
        private const val SHAR_PREF_LONG = "search_longitude"
        private const val SHAR_PREF_LATIT = "search_latitude"
        private const val SHAR_PREF_RADIUS = "search_radius"
        private const val SHAR_PREF_SELECTED_CORDS = "search_is_cords_selected"
    }

    private fun openMaps(latitude: Double, longitude: Double) {
        val googleMapsUri = "geo:$latitude,$longitude?q=$latitude,$longitude".toUri()
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri)
        googleMapsIntent.setPackage("com.google.android.apps.maps")

        if (googleMapsIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(googleMapsIntent)
        } else {
            openYandexMaps(latitude, longitude)
        }
    }

    private fun openYandexMaps(latitude: Double, longitude: Double) {
        val yandexMapsUri = "yandexmaps://maps.yandex.ru/?pt=$longitude,$latitude&z=12".toUri()
        val yandexMapsIntent = Intent(Intent.ACTION_VIEW, yandexMapsUri)

        if (yandexMapsIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(yandexMapsIntent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.download_google_maps), Toast.LENGTH_SHORT).show()
        }
    }

    private fun waitSearch(){
        val handler = Handler(Looper.getMainLooper())
        CoroutineScope(Dispatchers.IO).launch{
            delay(350)
            handler.post {
                search()
            }
        }
    }

    private fun setLocale(lang: String) {
        val myLocale = Locale(lang)
        Locale.setDefault(myLocale)

        val resources = resources
        val configuration = resources.configuration
        configuration.setLocale(myLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        val preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        preferences.edit { putString("language", lang) }

        requireActivity().recreate()
    }
}