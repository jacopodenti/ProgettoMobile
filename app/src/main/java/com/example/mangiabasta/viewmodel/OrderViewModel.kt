package com.example.mangiabasta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiabasta.model.OrderStatusResponse
import com.example.mangiabasta.network.ApiService
import kotlinx.coroutines.launch

sealed class OrderState {
    object Loading : OrderState()
    data class Success(val order: OrderStatusResponse) : OrderState()
    data class Error(val message: String) : OrderState()
}

class OrderViewModel(private val apiService: ApiService) : ViewModel() {

    private val _orderState = MutableLiveData<OrderState>()
    val orderState: LiveData<OrderState> get() = _orderState

    fun fetchOrderStatus(orderId: Int, sid: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = apiService.getOrderStatus(orderId, sid)
                _orderState.value = OrderState.Success(response)
            } catch (e: Exception) {
                _orderState.value = OrderState.Error("Errore: ${e.message}")
            }
        }
    }
}
