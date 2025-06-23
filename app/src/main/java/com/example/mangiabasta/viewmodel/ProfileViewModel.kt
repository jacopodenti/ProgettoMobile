package com.example.mangiabasta.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiabasta.model.OrderStatusResponse
import com.example.mangiabasta.model.UserProfile
import com.example.mangiabasta.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    fun updateUserDetails(
        firstName: String,
        lastName: String,
        cardFullName: String,
        cardNumber: String,
        cardExpireMonth: Int,
        cardExpireYear: Int,
        cardCVV: String,
        uid: String,
        sid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userProfile = UserProfile(
                    firstName = firstName,
                    lastName = lastName,
                    cardFullName = cardFullName,
                    cardNumber = cardNumber,
                    cardExpireMonth = cardExpireMonth,
                    cardExpireYear = cardExpireYear,
                    cardCVV = cardCVV,
                    sid = sid
                )
                // Effettua la chiamata API
                val response = RetrofitClient.apiService.updateUserDetails(uid, userProfile)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Errore durante l'aggiornamento: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                // Logga l'errore per debug
                Log.e("ProfileViewModel", "Errore durante l'aggiornamento dei dati: ${e.message}", e)

                // Esegui l'azione di errore
                onError("Errore durante l'aggiornamento dei dati: ${e.message}")
            }
        }
    }

    fun fetchOrderStatus(
        oid: Int,
        sid: String,
        onSuccess: (OrderStatusResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrderStatus(oid, sid)
                onSuccess(response)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore durante il recupero dello stato dell'ordine: ${e.message}", e)
                onError("Errore durante il recupero dello stato dell'ordine: ${e.message}")
            }
        }
    }
}
