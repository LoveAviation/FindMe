package com.example.findme.presentation.forms

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.example.findme.databinding.ActivityCreateFormBinding
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.forms.adapter.TagsAdapter
import com.example.findme.presentation.locationMap.MapActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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

        binding.tagsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

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
            }

            if(longitude != "0.0" && latitude != "0.0"){
                binding.selectedCoordinates.visibility = View.VISIBLE
                binding.selectedCoordinates.text = "Longitude: $longitude; Latitude: $latitude"
            }else{
                binding.selectedCoordinates.text = ""
                binding.selectedCoordinates.visibility = View.GONE
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

        binding.nameSwitch.setOnCheckedChangeListener{ _, isActivated ->
            if(isActivated){
                binding.author.text = "$accName $accSurname"
                author = "$accName $accSurname"
            }else{
                binding.author.text = accLogin
                author = accLogin
            }
        }

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }

        binding.addTagButton.setOnClickListener{
            if(tagList.size < 5){
                showInputDialog()
            }else{
                Toast.makeText(this, "Only 5 tags!", Toast.LENGTH_SHORT).show()
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
            binding.selectedCoordinates.text = ""
            binding.selectedCoordinates.visibility = View.GONE
            updateUI()
        }

        binding.saveButton.setOnClickListener{
            if(binding.titleEditText.text!!.isNotEmpty() && binding.descriptionEditText.text!!.isNotEmpty()){
                viewModel.addForm(this, binding.titleEditText.text.toString(),
                    binding.descriptionEditText.text.toString(),
                    tagList.toList(), longitude.toString(), latitude.toString(),
                    author, accAvatar, accLogin)
                viewModel.formAddingResult.observe(this){ result ->
                    if(result == true){
                        finish()
                    }else if(result == false){
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Snackbar.make(binding.root, "Please fill in title and description", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showInputDialog() {
        val input = EditText(this)

        val dialog = AlertDialog.Builder(this)
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
            binding.clearTagsButton.isEnabled = true
        }else{
            binding.tagsRV.visibility = View.GONE
            binding.clearTagsButton.isEnabled = false
        }
        if(longitude != null && latitude != null){
            binding.clearLocationButton.isEnabled = true
        }else{
            binding.clearLocationButton.isEnabled = false
        }
    }
}