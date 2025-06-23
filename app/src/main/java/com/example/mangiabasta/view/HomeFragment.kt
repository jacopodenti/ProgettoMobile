package com.example.mangiabasta.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mangiabasta.R
import com.example.mangiabasta.adapters.MenuAdapter
import com.example.mangiabasta.model.Menu
import com.example.mangiabasta.model.MenuDetailsResponse
import com.example.mangiabasta.model.MenuIngredients
import com.example.mangiabasta.utils.PreferencesHelper
import com.example.mangiabasta.viewmodel.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.rvMenus)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MenuAdapter(emptyList()) { selectedMenu ->
            fetchLiveLocation { lat, lng ->
                val preferencesHelper = PreferencesHelper(requireContext())
                val sid = preferencesHelper.getSid() ?: ""
                if (sid.isEmpty() || sid.length < 64) {
                    showError("SID non valido.")
                    return@fetchLiveLocation
                }

                homeViewModel.fetchMenuDetails(
                    mid = selectedMenu.mid,
                    sid = sid,
                    lat = lat,
                    lng = lng
                ) { details ->
                    if (details != null) {
                        openMenuDetails(details)
                    } else {
                        showError("Impossibile recuperare i dettagli del menu.")
                    }
                }
            }
        }

        recyclerView.adapter = adapter

        // Osserva i dati dal ViewModel
        homeViewModel.menus.observe(viewLifecycleOwner) { menuList ->
            adapter.updateData(menuList)
        }

        checkLocationPermissionAndFetch()
    }

    private fun openMenuDetails(details: MenuDetailsResponse) {
        val intent = Intent(requireContext(), MenuDetailsActivity::class.java)
        intent.putExtra("menuDetails", details)
        startActivity(intent)
    }


    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLiveLocation { lat, lng ->
                val preferencesHelper = PreferencesHelper(requireContext())
                val sid = preferencesHelper.getSid() ?: ""
                if (sid.length < 64) {
                    showError("Il SID non è valido o è troppo corto. Assicurati di avere un SID corretto.")
                    return@fetchLiveLocation
                }

                homeViewModel.fetchMenusWithImages(sid, lat, lng)
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLiveLocation(onLocationFetched: (Double, Double) -> Unit) {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude

                // Recupera la posizione salvata
                val preferencesHelper = PreferencesHelper(requireContext())
                val savedLocation = preferencesHelper.getLocation()

                // Verifica se la posizione è cambiata
                if (savedLocation == null || savedLocation.first != lat || savedLocation.second != lng) {
                    // Salva la nuova posizione
                    preferencesHelper.saveLocation(lat, lng)
                }

                onLocationFetched(lat, lng)
            } else {
                showError("Impossibile ottenere la posizione corrente.")
            }
        }.addOnFailureListener { exception ->
            showError("Errore durante il recupero della posizione: ${exception.message}")
        }
    }





    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
