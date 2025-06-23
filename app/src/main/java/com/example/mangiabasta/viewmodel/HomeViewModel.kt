package com.example.mangiabasta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiabasta.model.Menu
import com.example.mangiabasta.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import com.example.mangiabasta.model.MenuDetailsResponse
import com.example.mangiabasta.model.MenuIngredients
import retrofit2.Retrofit


class HomeViewModel : ViewModel() {

    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>> get() = _menus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage



    fun fetchMenuDetails(mid: Int, sid: String, lat: Double, lng: Double, callback: (MenuDetailsResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                // Recupera i dettagli del menu
                val details = RetrofitClient.apiService.getMenuDetails(mid, sid, lat, lng)

                try {
                    // Recupera l'immagine del menu
                    val imageResponse = RetrofitClient.apiService.getMenuImage(mid, sid)
                    val detailsWithImage = details.copy(imageBase64 = imageResponse.base64) // Aggiungi l'immagine ai dettagli
                    callback(detailsWithImage)
                } catch (imageException: Exception) {
                    println("Error fetching menu image: ${imageException.message}")
                    callback(details) // Restituisci i dettagli senza immagine se la chiamata fallisce
                }
            } catch (e: Exception) {
                println("Error fetching menu details: ${e.message}")
                _errorMessage.postValue("Errore durante il recupero dei dettagli del menu: ${e.message}")
                callback(null)
            }
        }
    }




    fun fetchMenusWithImages(sid: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // Recupera la lista dei menu
                val menuList = RetrofitClient.apiService.getMenus(sid, lat, lng)

                // Recupera le immagini in parallelo
                val menusWithImages = menuList.map { menu ->
                    async {
                        try {
                            val imageResponse = RetrofitClient.apiService.getMenuImage(menu.mid, sid)
                            menu.copy(imageBase64 = imageResponse.base64) // Aggiungi l'immagine al menu
                        } catch (e: Exception) {
                            menu // Ritorna il menu senza immagine se fallisce
                        }
                    }
                }.map { it.await() } // Aspetta che tutte le chiamate completino

                _menus.value = menusWithImages
            } catch (e: Exception) {
                _errorMessage.value = "Errore durante il recupero dei menu: ${e.message}"
            }
        }
    }

}
