package com.example.mangiabasta.network

import com.example.mangiabasta.model.ApiResponse
import retrofit2.http.*
import com.example.mangiabasta.model.SidUidResponse
import com.example.mangiabasta.model.Menu
import com.example.mangiabasta.model.MenuDetailsResponse
import com.example.mangiabasta.model.MenuImageResponse
import com.example.mangiabasta.model.UserProfile
import com.example.mangiabasta.model.BuyMenuRequest
import com.example.mangiabasta.model.BuyMenuResponse
import com.example.mangiabasta.model.MenuIngredients
import com.example.mangiabasta.model.OrderStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

import retrofit2.Response

interface ApiService {
    // Ottieni SID e UID
    @POST("user")
    suspend fun getSidUid(): SidUidResponse

    // Ottieni i menu vicini
    @GET("menu")
    suspend fun getMenus(
        @Query("sid") sid: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): List<Menu>

    // Ottieni l'immagine di un menu
    @GET("menu/{mid}/image")
    suspend fun getMenuImage(
        @Path("mid") mid: Int, // ID del menu
        @Query("sid") sid: String // SID
    ): MenuImageResponse

    @GET("menu/{mid}")
    suspend fun getMenuDetails(
        @Path("mid") mid: Int,
        @Query("sid") sid: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): MenuDetailsResponse

    @POST("menu/{menuId}/buy")
    suspend fun buyMenu(
        @Path("menuId") menuId: Int,
        @Body request: BuyMenuRequest
    ): BuyMenuResponse

    @GET("order/{orderId}")
    suspend fun getOrderStatus(
        @Path("orderId") orderId: Int,
        @Query("sid") sid: String
    ): OrderStatusResponse




    @PUT("user/{uid}")
    suspend fun updateUserDetails(
        @Path("uid") uid: String,
        @Body userProfile: UserProfile
    ): Response<Unit>

 @GET("menu/gennaio/{mid}/ingredients")
    suspend fun getMenuIngredients(
        @Path("mid") mid : Int,
        @Query("sid") sid: String
    ):List<MenuIngredients>




}
