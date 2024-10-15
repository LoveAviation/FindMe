package com.example.findme.presentation.locationMap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.caverock.androidsvg.BuildConfig
import com.example.findme.R
import com.example.findme.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import kotlin.getValue

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    private lateinit var fusedClient : FusedLocationProviderClient

    private var latitude = 0.0
    private var longitude = 0.0

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if(map.values.isNotEmpty() && map.values.all { it }){
            startLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        setContentView(binding.root)

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)

        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        binding.mapView.controller.setZoom(5.0)

        binding.centerButton.setOnClickListener {
            if(isLocationEnabled()){
                if(latitude == 0.0 && longitude == 0.0){
                    binding.mapView.controller.setZoom(5.0)
                }else{
                    binding.mapView.controller.setZoom(15.0)
                    binding.mapView.controller.setCenter(GeoPoint(latitude, longitude))
                }
            }else{
                Toast.makeText(this, "Turn on geolocation", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }

        binding.backButton.setOnClickListener{
            finish()
        }
        checkPermissions()
    }

    fun isLocationEnabled(): Boolean {
        return Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, 0) != Settings.Secure.LOCATION_MODE_OFF
    }

    private fun checkPermissions(){
        if(REQUIRED_PERMISSION.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }){
            startLocation()
        }else{
            launcher.launch(REQUIRED_PERMISSION)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            latitude = result.lastLocation!!.latitude
            longitude = result.lastLocation!!.longitude
            val marker = Marker(binding.mapView).apply {
                position = GeoPoint(latitude, longitude)
                title = "Your Location"
                snippet = "App founded you!"
                icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin, null)
            }
            binding.mapView.overlays.add(marker)
            binding.mapView.controller.setZoom(15.0)
            binding.mapView.controller.setCenter(GeoPoint(latitude, longitude))
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocation(){
        val locationRequest = LocationRequest.Builder(0)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setIntervalMillis(0L)
            .setMinUpdateIntervalMillis(0L)
            .setMaxUpdates(1)
            .build()

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    companion object{
        private val REQUIRED_PERMISSION: Array<String> = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}