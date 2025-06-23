package com.example.mangiabasta

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mangiabasta.network.RetrofitClient
import com.example.mangiabasta.utils.PreferencesHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Trova il NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment non trovato. Verifica il layout XML.")

        val navController = navHostFragment.navController

        // Configura il BottomNavigationView
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavView.setupWithNavController(navController)

        preferencesHelper = PreferencesHelper(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                onLocationPermissionGranted()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionExplanation()
            }

            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onLocationPermissionGranted()
            } else {
                onLocationPermissionDenied()
            }
        }

    private fun onLocationPermissionGranted() {
        fetchSidUid()
        startPeriodicLocationUpdates()
    }

    private fun onLocationPermissionDenied() {
        showMessage("Permesso di posizione negato. Alcune funzionalità potrebbero non essere disponibili.")
    }

    private fun showPermissionExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Permesso di Posizione Richiesto")
            .setMessage("L'app necessita del permesso di posizione per funzionare correttamente. Concedilo per continuare.")
            .setPositiveButton("Concedi") { _, _ ->
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun fetchSidUid() {
        val savedSid = preferencesHelper.getSid()
        val savedUid = preferencesHelper.getUid()

        if (savedSid.isNotEmpty() && savedUid.isNotEmpty()) {
            Log.d("MainActivity", "SID e UID già salvati: SID: $savedSid, UID: $savedUid")
            fetchAndSaveLocation()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.getSidUid()
                    preferencesHelper.saveSidUid(response.sid, response.uid)
                    Log.d("MainActivity", "Nuovo SID: ${response.sid}, UID: ${response.uid}")
                    withContext(Dispatchers.Main) { fetchAndSaveLocation() }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Errore durante il recupero di SID e UID", e)
                }
            }
        }
    }

    private fun startPeriodicLocationUpdates() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                fetchAndSaveLocation()
                delay(5000) // Aggiorna ogni 5 secondi
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndSaveLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationProviderClient.removeLocationUpdates(this)
                val location = locationResult.lastLocation ?: return

                val newLatitude = location.latitude
                val newLongitude = location.longitude

                val savedLocation = preferencesHelper.getLocation()

                if (savedLocation == null ||
                    areLocationsDifferent(savedLocation.first, savedLocation.second, newLatitude, newLongitude)) {
                    preferencesHelper.saveOrUpdateLocation(newLatitude, newLongitude)
                    Log.d("MainActivity", "Posizione aggiornata: Lat=$newLatitude, Lng=$newLongitude")
                    Toast.makeText(
                        this@MainActivity,
                        "Posizione aggiornata: Lat=$newLatitude, Lng=$newLongitude",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d("MainActivity", "La posizione è invariata: Lat=$newLatitude, Lng=$newLongitude")
                }
            }
        }, mainLooper)
    }

    private fun areLocationsDifferent(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Boolean {
        val threshold = 0.0001 // Soglia per considerare le coordinate uguali
        return kotlin.math.abs(lat1 - lat2) > threshold || kotlin.math.abs(lng1 - lng2) > threshold
    }
}
