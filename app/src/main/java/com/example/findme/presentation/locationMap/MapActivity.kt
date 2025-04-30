package com.example.findme.presentation.locationMap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

/**
 * Откройте этот Activity при помощи registerForActivityResult
 * чтобы получить координаты выбранные пользователем.
 * Находит нынешнюю локацию пользователя
 */

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    private lateinit var fusedClient: FusedLocationProviderClient

    private var latitude = 0.0
    private var longitude = 0.0
    private var marker: Marker? = null

    private var permis = false

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if (map.values.isNotEmpty() && map.values.all { it }) {
            permis = true
        }
    }

    private val overlay = object : Overlay() {
        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
            val projection = mapView.projection
            val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint

            if (marker == null) {
                marker = Marker(mapView).apply {
                    icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin, null)
                }
                mapView.overlays.add(marker)
            }

            marker?.position = geoPoint
            marker?.title = getString(R.string.selected_location)
            marker?.snippet = getString(R.string.click_select_to_confirm)

            longitude = geoPoint.longitude
            latitude = geoPoint.latitude
            return true
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

        checkPermissions()

        binding.centerButton.setOnClickListener {
            if (permis) {
                startLocation()
                if (isLocationEnabled()) {
                    if (latitude == 0.0 && longitude == 0.0) {
                        binding.mapView.controller.setZoom(5.0)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.turn_on_geolocation), Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }else{
                Toast.makeText(this, getString(R.string.allow_use_of_geolocation), Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        }

        binding.mapView.overlays.add(overlay)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.selectButton.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra(LONGITUDE_KEY, longitude.toString())
            resultIntent.putExtra(LATITUDE_KEY, latitude.toString())
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
           if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
               stopLoading()
           }
    }

    fun isLocationEnabled(): Boolean {
        return Settings.Secure.getInt(
            this.contentResolver,
            Settings.Secure.LOCATION_MODE,
            0
        ) != Settings.Secure.LOCATION_MODE_OFF
    }

    private fun checkPermissions() {
        if (REQUIRED_PERMISSION.all { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            launcher.launch(REQUIRED_PERMISSION)
        }else{
            permis = true
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            latitude = result.lastLocation!!.latitude
            longitude = result.lastLocation!!.longitude

            if (marker == null) {
                marker = Marker(binding.mapView).apply {
                    position = GeoPoint(latitude, longitude)
                    title = getString(R.string.your_location)
                    snippet = getString(R.string.app_founded_you)
                    icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin, null)
                }
                binding.mapView.overlays.add(marker)
            }else{
                marker?.position = GeoPoint(latitude, longitude)
                marker?.title = getString(R.string.your_location)
                marker?.snippet = getString(R.string.app_founded_you)
            }
            binding.mapView.controller.setZoom(20.0)
            binding.mapView.controller.setCenter(GeoPoint(latitude, longitude))
            stopLoading()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocation() {
        startLoading()
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

    private fun startLoading() {
        binding.loadingBar.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        binding.loadingBar.visibility = View.GONE
    }

    companion object {
        private val REQUIRED_PERMISSION: Array<String> = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        const val LONGITUDE_KEY = "longitude"
        const val LATITUDE_KEY = "latitude"
    }
}