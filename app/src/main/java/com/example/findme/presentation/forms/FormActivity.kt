package com.example.findme.presentation.forms

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.findme.databinding.ActivityFormBinding
import com.example.findme.R
import com.example.findme.presentation.FavouritesVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBinding

    private var location : String? = null

    private var isFavourite = false

    private var id: Int = 0

    private val viewModel : FavouritesVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getIntExtra(KEY_ID, 0)

        isFavourite = intent.getBooleanExtra(KEY_FAVOURITE, false)
        if(isFavourite){
            binding.favouriteButton.setColorFilter(ContextCompat.getColor(this, R.color.red))
        }
        binding.favouriteButton.setOnClickListener{
            if(isFavourite){
                binding.favouriteButton.clearColorFilter()
                isFavourite = false
                viewModel.deleteFavourite(id)
            }else{
                binding.favouriteButton.setColorFilter(ContextCompat.getColor(this, R.color.red))
                isFavourite = true
                viewModel.addFavourite(id)
            }
        }

        binding.toolbar.title = intent.getStringExtra(KEY_TITLE)
        binding.title.text = intent.getStringExtra(KEY_TITLE)
        binding.description.text = intent.getStringExtra(KEY_DESCRIPTION)
        binding.tagsList.text = getString(R.string.tags, intent.getStringExtra(KEY_TAGS))
        binding.author.text = intent.getStringExtra(KEY_AUTHOR)
        location = intent.getStringExtra(KEY_LOCATION)

        if (location != null){
            binding.locationButton.visibility = View.VISIBLE
            binding.locationButton.setOnClickListener{
                val latAndLong = location!!.split(" ")
                val latitude = latAndLong[0].toDouble()
                val longitude = latAndLong[1].toDouble()

                openMaps(latitude, longitude)
            }
        }

        val avatar = intent.getStringExtra(KEY_AVATAR)
        if(avatar != null) {
            Glide.with(binding.root)
                .load(intent.getStringExtra(KEY_AVATAR))
                .circleCrop()
                .into(binding.authorAvatar)
        }

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }
    }

    private fun openMaps(latitude: Double, longitude: Double) {
        val googleMapsUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri)
        googleMapsIntent.setPackage("com.google.android.apps.maps")

        if (googleMapsIntent.resolveActivity(packageManager) != null) {
            startActivity(googleMapsIntent)
        } else {
            openYandexMaps(latitude, longitude)
        }
    }

    private fun openYandexMaps(latitude: Double, longitude: Double) {
        val yandexMapsUri = Uri.parse("yandexmaps://maps.yandex.ru/?pt=$longitude,$latitude&z=12")
        val yandexMapsIntent = Intent(Intent.ACTION_VIEW, yandexMapsUri)

        if (yandexMapsIntent.resolveActivity(packageManager) != null) {
            startActivity(yandexMapsIntent)
        } else {
            Toast.makeText(this, getString(R.string.download_google_maps), Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        const val KEY_ID = "id"
        const val KEY_TITLE = "title"
        const val KEY_DESCRIPTION = "description"
        const val KEY_TAGS = "tags"
        const val KEY_LOCATION = "location"
        const val KEY_AUTHOR = "author"
        const val KEY_AVATAR = "author_avatar"
        const val KEY_LOGIN = "login"

        const val KEY_FAVOURITE = "isFavourite"
    }
}