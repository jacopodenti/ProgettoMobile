package com.example.mangiabasta.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUserProfile(
        firstName: String,
        lastName: String,
        cardFullName: String,
        cardNumber: String,
        cardExpireMonth: Int,
        cardExpireYear: Int,
        cardCVV: String
    ) {
        sharedPreferences.edit()
            .putString("first_name", firstName)
            .putString("last_name", lastName)
            .putString("card_full_name", cardFullName)
            .putString("card_number", cardNumber)
            .putInt("expire_month", cardExpireMonth)
            .putInt("expire_year", cardExpireYear)
            .putString("card_cvv", cardCVV)
            .apply()
    }

    fun getUserProfile(): Map<String, Any> {
        return mapOf(
            "first_name" to (sharedPreferences.getString("first_name", "") ?: ""),
            "last_name" to (sharedPreferences.getString("last_name", "") ?: ""),
            "card_full_name" to (sharedPreferences.getString("card_full_name", "") ?: ""),
            "card_number" to (sharedPreferences.getString("card_number", "") ?: ""),
            "expire_month" to sharedPreferences.getInt("expire_month", 0),
            "expire_year" to sharedPreferences.getInt("expire_year", 0),
            "card_cvv" to (sharedPreferences.getString("card_cvv", "") ?: "")
        )
    }

    fun saveSidUid(sid: String, uid: String) {
        sharedPreferences.edit()
            .putString("sid", sid)
            .putString("uid", uid)
            .apply()
    }
    fun saveOrUpdateLocation(lat: Double, lng: Double) {
        val editor = sharedPreferences.edit()
        editor.putFloat("latitude", lat.toFloat())
        editor.putFloat("longitude", lng.toFloat())
        editor.apply()
    }


    fun saveLocation(lat: Double, lng: Double) {
        sharedPreferences.edit()
            .putFloat("latitude", lat.toFloat())
            .putFloat("longitude", lng.toFloat())
            .apply()
    }

    fun getLocation(): Pair<Double, Double>? {
        val lat = sharedPreferences.getFloat("latitude", Float.MIN_VALUE)
        val lng = sharedPreferences.getFloat("longitude", Float.MIN_VALUE)

        return if (lat != Float.MIN_VALUE && lng != Float.MIN_VALUE) {
            Pair(lat.toDouble(), lng.toDouble())
        } else {
            null // Nessuna posizione salvata
        }
    }

    fun saveOid(oid: Int) {
        sharedPreferences.edit()
            .putInt("oid", oid)
            .apply()
    }

    fun getOid(): Int {
        return sharedPreferences.getInt("oid", -1) // -1 come valore predefinito se non esiste
    }

    fun getSid(): String = sharedPreferences.getString("sid", "") ?: ""
    fun getUid(): String = sharedPreferences.getString("uid", "") ?: ""
}
