package com.example.findme.presentation.forms

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.findme.R
import com.example.findme.databinding.ActivityEditFormBinding
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditForm : AppCompatActivity(), TagsAdapter.OnButtonClickListener {

    private lateinit var binding: ActivityEditFormBinding
    private val viewModel : FormsVM by viewModels()

    private var id : Int = 0
    private var author: String? = null
    private var tagList : MutableList<String> = mutableListOf()

    private lateinit var mapResultLauncher: ActivityResultLauncher<Intent>
    private var longitude : String? = null
    private var latitude : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tagsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        viewModel.formEditingResult.observe(this){ result ->
            if(result == true){
                finish()
            }else if(result == false){
                stopLoading()
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.formDeletingResult.observe(this){ result ->
            if(result == true){
                finish()
            }else if(result == false){
                stopLoading()
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }

        mapResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                longitude = result.data?.getStringExtra(MapActivity.LONGITUDE_KEY)
                latitude = result.data?.getStringExtra(MapActivity.LATITUDE_KEY)
            }

            updateUI()
        }

        id = intent.getIntExtra(FormActivity.KEY_ID, 0)
        author = intent.getStringExtra(FormActivity.KEY_AUTHOR)
        val login = intent.getStringExtra(FormActivity.KEY_LOGIN)

        if(author != login) {
            binding.nameSwitch.isChecked = true
        }

        binding.author.text = author
        binding.titleEditText.setText(intent.getStringExtra(FormActivity.KEY_TITLE))
        binding.descriptionEditText.setText(intent.getStringExtra(FormActivity.KEY_DESCRIPTION))

        val avatar = intent.getStringExtra(FormActivity.KEY_AVATAR)
        if(avatar != null){
            Glide.with(binding.root)
                .load(avatar)
                .circleCrop()
                .into(binding.avatar)
        }

        val name = intent.getStringExtra(MainActivity.KEY_NAME)
        val surname = intent.getStringExtra(MainActivity.KEY_SURNAME)
        binding.nameSwitch.setOnCheckedChangeListener{ _, isActivated ->
            if(isActivated){
                binding.author.text = "$name $surname"
                this.author = "$name $surname"
            }else{
                binding.author.text = login
                this.author = login
            }
        }

        //ЗДЕСЬ ВСЕ СВЯЗАННОЕ С ТЭГАМИ
        tagList = intent.getStringExtra(FormActivity.KEY_TAGS)!!.split(" ").filter { it.isNotEmpty() } as MutableList<String>
        binding.tagsRV.adapter = TagsAdapter(tagList, this)
        updateUI()

        binding.addTagButton.setOnClickListener{
            if(tagList.size < 5) { showInputDialog() }
        }
        binding.clearTagsButton.setOnClickListener{
            tagList.clear()
            updateUI()
        }


        //ЗДЕСЬ ВСЕ СВЯЗАННОЕ С ЛОКАЦИЕЙ
        val location = intent.getStringExtra(FormActivity.KEY_LOCATION)
        latitude = location?.substringBefore(" ")
        longitude = location?.substringAfter(" ")
        if(location != null){
            updateUI()
        }
        binding.clearLocationButton.setOnClickListener{
            longitude = null; latitude = null
            updateUI()
        }
        binding.addLocationButton.setOnClickListener{
            mapResultLauncher.launch(Intent(this, MapActivity::class.java))
        }

        binding.saveButton.setOnClickListener{
            editForm()
        }

        binding.deleteButton.setOnClickListener{
            showAlertDialog()
        }
    }

    override fun onButtonClick(position: Int) {
        tagList.removeAt(position)
        binding.tagsRV.adapter = TagsAdapter(tagList, this)
        updateUI()
    }

    private fun updateUI(){
        if(tagList.isNotEmpty()){
            binding.clearTagsButton.isEnabled = true
            binding.tagsRV.visibility = View.VISIBLE
        }else{
            binding.clearTagsButton.isEnabled = false
            binding.tagsRV.visibility = View.GONE
        }

        if(longitude != null && latitude != null){
            binding.clearLocationButton.isEnabled = true
            binding.selectedCoordinates.visibility = View.VISIBLE
            binding.selectedCoordinates.text = getString(R.string.longitude_latitude, longitude, latitude)
        }else{
            binding.clearLocationButton.isEnabled = false
            binding.selectedCoordinates.visibility = View.GONE
        }
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

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(getString(R.string.you_want_to_delete_this_form))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            viewModel.deleteForm(this, id)
            startLoading()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.back)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun editForm(){
        if(binding.titleEditText.text!!.trim().isNotEmpty() && binding.descriptionEditText.text!!.trim().isNotEmpty()){
            startLoading()
            viewModel.editForm(this, id, binding.titleEditText.text.toString().trim(),
                binding.descriptionEditText.text.toString().trim(), tagList, longitude, latitude, author)
        }else{
            Snackbar.make(binding.root,
                getString(R.string.please_fill_in_title_and_description), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startLoading(){
        binding.loadingBar.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false
        binding.deleteButton.isEnabled = false
    }

    private fun stopLoading(){
        binding.loadingBar.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.deleteButton.isEnabled = true
    }
}