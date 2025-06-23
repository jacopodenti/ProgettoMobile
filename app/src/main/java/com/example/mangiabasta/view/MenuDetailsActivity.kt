package com.example.mangiabasta.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mangiabasta.R
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.mangiabasta.model.BuyMenuRequest
import com.example.mangiabasta.model.BuyMenuResponse
import com.example.mangiabasta.model.DeliveryLocation
import com.example.mangiabasta.model.MenuDetailsResponse
import com.example.mangiabasta.network.RetrofitClient
import com.example.mangiabasta.network.RetrofitClient.apiService
import com.example.mangiabasta.utils.PreferencesHelper
import com.example.mangiabasta.viewmodel.MenuDetailsState
import com.example.mangiabasta.viewmodel.MenuDetailsViewModel
import com.example.mangiabasta.viewmodel.MenuDetailsViewModelFactory

class MenuDetailsActivity : AppCompatActivity() {

    private lateinit var menuDetailsViewModel: MenuDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_details)

        // Configura il ViewModel con la factory
        val factory = MenuDetailsViewModelFactory(RetrofitClient.apiService)
        menuDetailsViewModel = ViewModelProvider(this, factory)[MenuDetailsViewModel::class.java]

        // Abilita il pulsante "Indietro" e imposta il titolo
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dettagli Menu"

        // Recupera i dettagli del menu passati come intent
        val details = intent.getSerializableExtra("menuDetails") as? MenuDetailsResponse

        // Associa i componenti UI
        val menuNameTextView = findViewById<TextView>(R.id.tvMenuName)
        val menuPriceTextView = findViewById<TextView>(R.id.tvMenuPrice)
        val menuDeliveryTimeTextView = findViewById<TextView>(R.id.tvMenuDeliveryTime)
        val menuDescriptionTextView = findViewById<TextView>(R.id.tvLongDescription)
        val menuImageView = findViewById<ImageView>(R.id.ivMenuImage)
        val buyButton = findViewById<Button>(R.id.btnBuy)
        val ingButton = findViewById<Button>(R.id.btnIng)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Popola i dati del menu, se disponibili
        details?.let {
            menuNameTextView.text = it.name
            menuPriceTextView.text = "€${it.price}"
            menuDeliveryTimeTextView.text = "Tempo di consegna: ${kotlin.math.ceil(it.deliveryTime / 60.0).toInt()} minuti"
            menuDescriptionTextView.text = it.longDescription

            if (!it.imageBase64.isNullOrEmpty()) {
                val decodedString = Base64.decode(it.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                menuImageView.setImageBitmap(bitmap)
            } else {
                menuImageView.setImageResource(R.drawable.ic_launcher_background) // Immagine di default
            }
        } ?: run {
            Toast.makeText(this, "Errore: Dettagli del menu mancanti", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Recupera il SID e la posizione da Preferences
        val preferencesHelper = PreferencesHelper(this)
        val sid = preferencesHelper.getSid()
        val location = preferencesHelper.getLocation()
        val uid = preferencesHelper.getUid()
        if (sid.isEmpty() || location == null) {
            Toast.makeText(this, "Errore: SID o posizione mancanti", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val (latitude, longitude) = location

        // Osserva lo stato del ViewModel
        menuDetailsViewModel.menuDetailsState.observe(this) { state ->
            when (state) {
                is MenuDetailsState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is MenuDetailsState.Success -> {
                    progressBar.visibility = View.GONE
                    val response = state.response as BuyMenuResponse
                    Toast.makeText(
                        this,
                        "Ordine creato con successo! Stato: ${response.status}",
                        Toast.LENGTH_SHORT
                    ).show()
                }



                is MenuDetailsState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Gestione del click sul pulsante "Acquista"
        buyButton.setOnClickListener {
            val request = BuyMenuRequest(
                sid = sid,
                deliveryLocation = DeliveryLocation(lat = latitude, lng = longitude)
            )
            menuDetailsViewModel.buyMenu(details.mid, request, preferencesHelper)
        }
        // Gestione del click sul pulsante "Acquista"



        menuDetailsViewModel.ingredients.observe(this) { ingredients ->
            if (ingredients != null && ingredients.isNotEmpty()) {
                Log.d("ESAME", "menù ${details.mid} numero di ingredienti ${ingredients?.size ?: 0}")

                val names = ArrayList(ingredients.map { it.name })
                val intent = Intent(this, IngredientsActivity::class.java)
                intent.putStringArrayListExtra("INGREDIENTS", names)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Ingredienti non disponibili", Toast.LENGTH_SHORT).show()
            }
        }

        ingButton.setOnClickListener {
            menuDetailsViewModel.fetchMenuIngredients(details.mid, sid)

        }


    }




    // Gestisci il click sul pulsante "Indietro"
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
