package com.example.mangiabasta.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mangiabasta.R
import com.example.mangiabasta.network.RetrofitClient
import com.example.mangiabasta.utils.PreferencesHelper
import com.example.mangiabasta.viewmodel.OrderState
import com.example.mangiabasta.viewmodel.OrderViewModel
import com.example.mangiabasta.viewmodel.OrderViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OrderFragment : Fragment(), OnMapReadyCallback {

    private lateinit var orderViewModel: OrderViewModel
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura il ViewModel con la factory
        val factory = OrderViewModelFactory(RetrofitClient.apiService)
        orderViewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]

        // Inizializza PreferencesHelper
        preferencesHelper = PreferencesHelper(requireContext())

        // Recupera il SID e l'OID salvati
        val sid = preferencesHelper.getSid()
        val oid = preferencesHelper.getOid()

        if (sid.isEmpty() || oid == -1) {
            // Mostra un messaggio e termina l'esecuzione
            view.findViewById<TextView>(R.id.tvOrderStatus).text = "Non hai effettuato ancora nessun ordine."
            return
        }

        // Associa i componenti UI
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val orderStatusTextView = view.findViewById<TextView>(R.id.tvOrderStatus)
        val deliveryLocationTextView = view.findViewById<TextView>(R.id.tvDeliveryLocation)
        val expectedDeliveryTextView = view.findViewById<TextView>(R.id.tvExpectedDelivery)

        // Configura la mappa
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Avvia aggiornamenti periodici dello stato dell'ordine
        lifecycleScope.launch {
            while (true) {
                orderViewModel.fetchOrderStatus(oid, sid)
                delay(5000) // Aggiorna ogni 5 secondi
            }
        }

        // Osserva lo stato degli ordini
        orderViewModel.orderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OrderState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is OrderState.Success -> {
                    progressBar.visibility = View.GONE
                    val order = state.order
                    orderStatusTextView.text = "Stato ordine: ${order.status}"
                    deliveryLocationTextView.text =
                        "Posizione consegna: ${order.deliveryLocation.lat}, ${order.deliveryLocation.lng}"
                    expectedDeliveryTextView.text = "Consegna prevista: ${order.expectedDeliveryTimestamp}"

                    if (order.status == "ON_DELIVERY") {
                        val deliveryLocation = LatLng(order.deliveryLocation.lat, order.deliveryLocation.lng)
                        val currentDroneLocation = LatLng(order.currentPosition.lat, order.currentPosition.lng)

                        // Aggiorna la mappa
                        updateMap(deliveryLocation, currentDroneLocation)
                    }
                }
                is OrderState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun updateMap(
        deliveryLocation: LatLng,
        currentDroneLocation: LatLng
    ) {
        googleMap.clear() // Rimuovi i marker precedenti

        // Marker per la posizione di consegna
        googleMap.addMarker(
            MarkerOptions().position(deliveryLocation).title("Posizione di consegna")
        )

        // Marker per la posizione attuale del drone
        googleMap.addMarker(
            MarkerOptions().position(currentDroneLocation).title("Posizione attuale del drone")
        )

        // Centra la mappa per mostrare entrambi i marker
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(deliveryLocation)
        boundsBuilder.include(currentDroneLocation)
        val bounds = boundsBuilder.build()

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }
}
