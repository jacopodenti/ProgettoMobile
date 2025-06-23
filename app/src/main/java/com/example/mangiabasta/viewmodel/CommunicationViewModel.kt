package com.example.mangiabasta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.mangiabasta.model.SidUidResponse
import com.example.mangiabasta.repository.CommunicationRepository

class CommunicationViewModel(private val repository: CommunicationRepository) : ViewModel() {

    private val _sidUid = MutableLiveData<SidUidResponse>()
    val sidUid: LiveData<SidUidResponse> get() = _sidUid

    fun getSidUid() {
        viewModelScope.launch {
            try {
                val response: SidUidResponse = repository.getSidUid()
                _sidUid.postValue(response) // Aggiorna LiveData
            } catch (e: Exception) {
                // Puoi mostrare un messaggio di errore nella UI in futuro
                e.printStackTrace()
            }
        }
    }
}