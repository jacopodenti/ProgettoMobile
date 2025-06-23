package com.example.mangiabasta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiabasta.model.ApiResponse
import com.example.mangiabasta.model.BuyMenuRequest
import com.example.mangiabasta.model.BuyMenuResponse
import com.example.mangiabasta.model.Menu
import com.example.mangiabasta.model.MenuIngredients
import com.example.mangiabasta.network.ApiService
import com.example.mangiabasta.network.RetrofitClient
import com.example.mangiabasta.utils.PreferencesHelper
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class MenuDetailsState {
    object Loading : MenuDetailsState()
    data class Success(val response: BuyMenuResponse) : MenuDetailsState()
    data class Error(val message: String) : MenuDetailsState()
}

class MenuDetailsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _menuDetailsState = MutableLiveData<MenuDetailsState>()
    val menuDetailsState: LiveData<MenuDetailsState> get() = _menuDetailsState

    private val _ingredients = MutableLiveData<List<MenuIngredients>>()
    val ingredients: LiveData<List<MenuIngredients>> get() = _ingredients

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage



    // Function to buy a menu
    fun buyMenu(menuId: Int, request: BuyMenuRequest, preferencesHelper: PreferencesHelper) {
        viewModelScope.launch {
            _menuDetailsState.value = MenuDetailsState.Loading
            try {
                val response = apiService.buyMenu(menuId, request) // Restituisce BuyMenuResponse
                preferencesHelper.saveOid(response.oid) // Salva l'oid
                _menuDetailsState.value = MenuDetailsState.Success(response)
            } catch (e: Exception) {
                _menuDetailsState.value = MenuDetailsState.Error("Errore: ${e.message}")
            }
        }
    }

    fun fetchMenuIngredients(mid:Int,sid:String){
        viewModelScope.launch {
            try {
                val ingredients = RetrofitClient.apiService.getMenuIngredients(mid, sid)
                if (ingredients.isNotEmpty()) {
                    _ingredients.value = ingredients
                } else {
                    _errorMessage.value = "Nessun ingrediente trovato per il menu selezionato."
                }
            }catch (e:Exception){
                _errorMessage.value = "Errore durante il recupero degli ingredienti del menu: ${e.message}"
            }
        }
    }


}
