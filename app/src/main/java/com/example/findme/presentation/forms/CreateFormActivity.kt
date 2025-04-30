package com.example.findme.presentation.forms

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.ActivityCreateFormBinding
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity в котором создаются анкеты
 */

@AndroidEntryPoint
class CreateFormActivity : AppCompatActivity(), TagsAdapter.OnButtonClickListener  {
    private lateinit var binding: ActivityCreateFormBinding
    private val viewModel : FormsVM by viewModels()

    private lateinit var accName: String
    private lateinit var accSurname: String
    private lateinit var accLogin: String
    private lateinit var accAvatar: String

    private val tagList = mutableListOf<String>()

    private lateinit var mapResultLauncher: ActivityResultLauncher<Intent>

    private var longitude : String? = null
    private var latitude : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tagsRV.layoutManager = FlexboxLayoutManager(this).apply {
            flexWrap = FlexWrap.WRAP // Перенос элементов на новую строку
            flexDirection = FlexDirection.ROW // Расположение по строкам
            justifyContent = JustifyContent.FLEX_START // Выравнивание слева направо
        }

        accName = intent.getStringExtra(MainActivity.KEY_NAME).toString()
        accSurname = intent.getStringExtra(MainActivity.KEY_SURNAME).toString()
        accLogin = intent.getStringExtra(MainActivity.KEY_LOGIN).toString()
        accAvatar = intent.getStringExtra(MainActivity.KEY_AVATAR).toString()

        var author = accLogin

        mapResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                longitude = result.data?.getStringExtra(MapActivity.LONGITUDE_KEY)
                latitude = result.data?.getStringExtra(MapActivity.LATITUDE_KEY)
                if (longitude == "0.0") { longitude = null }
                if (latitude == "0.0") { latitude = null }
            }
            updateUI()
        }

        if(accAvatar.isNotEmpty()) {
            Glide.with(binding.root)
                .load(accAvatar)
                .circleCrop()
                .into(binding.avatar)
        }

        binding.author.text = accLogin


        binding.nameCheckbox.setOnClickListener {
            if (binding.nameCheckbox.isChecked){
                binding.author.text = "$accName $accSurname"
                author = "$accName $accSurname"
            }else{
                binding.author.text = accLogin
                author = accLogin
            }
        }

        binding.titleEditText.afterChangeWithDebounce{ input ->
            if(input.length < 3){
                binding.titleEditText.error =
                    getString(R.string.title_can_not_have_less_then_3_symbols)
            }
        }

        binding.descriptionEditText.afterChangeWithDebounce{ input ->
            if(input.length < 3){
                binding.descriptionEditText.error =
                    getString(R.string.description_can_not_have_less_then_3_symbols)
            }
        }

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }

        binding.addTagButton.setOnClickListener{
            if(tagList.size < 5){
                showInputDialog()
            }else{
                Toast.makeText(this, getString(R.string.only_5_tags), Toast.LENGTH_SHORT).show()
            }
        }

        binding.clearTagsButton.setOnClickListener{
            tagList.clear()
            binding.tagsRV.adapter = TagsAdapter(tagList, this)
            updateUI()
        }

        binding.addLocationButton.setOnClickListener{
            mapResultLauncher.launch(Intent(this, MapActivity::class.java))
        }

        binding.clearLocationButton.setOnClickListener{
            longitude = null
            latitude = null
            updateUI()
        }

        binding.selectedCoordinates.setOnClickListener {
            openMaps(latitude!!.toDouble(), longitude!!.toDouble())
        }

        binding.createButton.setOnClickListener{
            if(binding.titleEditText.text!!.trim().isNotEmpty()
                && binding.titleEditText.text!!.trim().length >= 3
                && binding.descriptionEditText.text!!.trim().isNotEmpty()
                && binding.descriptionEditText.text!!.trim().length >= 3){
                viewModel.addForm(this, this, binding.titleEditText.text.toString().trim(),
                    binding.descriptionEditText.text.toString().trim(),
                    tagList.toList(), longitude.toString(), latitude.toString(),
                    author, accAvatar, accLogin)
                viewModel.formAddingResult.observe(this){ result ->
                    if(result == true){
                        finish()
                    }else if(result == false){
                        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                if(binding.titleEditText.text!!.trim().isEmpty()){
                    binding.titleEditText.error = getString(R.string.please_fill_in_title)
                }
                if(binding.descriptionEditText.text!!.trim().isEmpty()){
                    binding.descriptionEditText.error =
                        getString(R.string.please_fill_in_description)
                }
            }
        }
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
        val input = EditText(this)
        input.filters = arrayOf(InputFilter.LengthFilter(30))
        input.inputType = InputType.TYPE_CLASS_TEXT

        val dialog = AlertDialog.Builder(this)
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
            binding.clearTagsButton.isEnabled = true
        }else{
            binding.tagsRV.visibility = View.GONE
            binding.clearTagsButton.isEnabled = false
        }
        if((longitude != "0.0" && latitude != "0.0") && (longitude != null && latitude != null)){
            binding.clearLocationButton.isEnabled = true
            binding.selectedCoordinates.visibility = View.VISIBLE
        }else{
            binding.clearLocationButton.isEnabled = false
            binding.selectedCoordinates.visibility = View.GONE
        }
    }

    private fun openMaps(latitude: Double, longitude: Double) {
        val googleMapsUri = "geo:$latitude,$longitude?q=$latitude,$longitude".toUri()
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri)
        googleMapsIntent.setPackage("com.google.android.apps.maps")

        if (googleMapsIntent.resolveActivity(packageManager) != null) {
            startActivity(googleMapsIntent)
        } else {
            openYandexMaps(latitude, longitude)
        }
    }

    private fun openYandexMaps(latitude: Double, longitude: Double) {
        val yandexMapsUri = "yandexmaps://maps.yandex.ru/?pt=$longitude,$latitude&z=12".toUri()
        val yandexMapsIntent = Intent(Intent.ACTION_VIEW, yandexMapsUri)

        if (yandexMapsIntent.resolveActivity(packageManager) != null) {
            startActivity(yandexMapsIntent)
        } else {
            Toast.makeText(this, getString(R.string.download_google_maps), Toast.LENGTH_SHORT).show()
        }
    }
}