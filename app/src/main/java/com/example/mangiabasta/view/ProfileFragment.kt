package com.example.mangiabasta.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mangiabasta.R
import com.example.mangiabasta.utils.PreferencesHelper
import com.example.mangiabasta.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var tvOrderStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesHelper = PreferencesHelper(requireContext())

        val etFirstName = view.findViewById<EditText>(R.id.etFirstName)
        val etLastName = view.findViewById<EditText>(R.id.etLastName)
        val etCardFullName = view.findViewById<EditText>(R.id.etCardFullName)
        val etCardNumber = view.findViewById<EditText>(R.id.etCardNumber)
        val etExpireMonth = view.findViewById<EditText>(R.id.etExpireMonth)
        val etExpireYear = view.findViewById<EditText>(R.id.etExpireYear)
        val etCardCVV = view.findViewById<EditText>(R.id.etCardCVV)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val tvLastOrderStatus = view.findViewById<TextView>(R.id.tvLastOrderStatus)
        val tvLastOrderLocation = view.findViewById<TextView>(R.id.tvLastOrderLocation)
        val tvLastOrderDeliveryTime = view.findViewById<TextView>(R.id.tvLastOrderDeliveryTime)

        loadSavedData(etFirstName, etLastName, etCardFullName, etCardNumber, etExpireMonth, etExpireYear, etCardCVV)
        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val cardFullName = etCardFullName.text.toString()
            val cardNumber = etCardNumber.text.toString()
            val expireMonth = etExpireMonth.text.toString().toIntOrNull() ?: 0
            val expireYear = etExpireYear.text.toString().toIntOrNull() ?: 0
            val cardCVV = etCardCVV.text.toString()

            if (validateInputs(firstName, lastName, cardFullName, cardNumber, expireMonth, expireYear, cardCVV)) {
                val uid = preferencesHelper.getUid()
                val sid = preferencesHelper.getSid()

                if (uid.isEmpty() || sid.isEmpty()) {
                    Toast.makeText(requireContext(), "UID o SID non validi.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                profileViewModel.updateUserDetails(
                    firstName, lastName, cardFullName, cardNumber, expireMonth, expireYear, cardCVV, uid, sid,
                    onSuccess = {
                        preferencesHelper.saveUserProfile(
                            firstName, lastName, cardFullName, cardNumber, expireMonth, expireYear, cardCVV
                        )
                        Toast.makeText(requireContext(), "Dati aggiornati con successo!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { errorMessage ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Per favore, compila tutti i campi correttamente.", Toast.LENGTH_SHORT).show()
            }
        }


        // Fetch and display last order details
        val sid = preferencesHelper.getSid()
        val oid = preferencesHelper.getOid()

        if (sid.isEmpty() || oid == -1) {
            tvLastOrderStatus.text = "Non hai effettuato ancora nessun ordine."
            tvLastOrderLocation.text = ""
            tvLastOrderDeliveryTime.text = ""
        } else {
            profileViewModel.fetchOrderStatus(
                oid = oid,
                sid = sid,
                onSuccess = { order ->
                    tvLastOrderStatus.text = "Stato ordine: ${order.status}"
                    tvLastOrderLocation.text = "Posizione consegna: ${order.deliveryLocation.lat}, ${order.deliveryLocation.lng}"
                    tvLastOrderDeliveryTime.text = "Consegna prevista: ${order.expectedDeliveryTimestamp}"
                },
                onError = { errorMessage ->
                    tvLastOrderStatus.text = "Errore nel recupero dell'ordine"
                    tvLastOrderLocation.text = errorMessage
                    tvLastOrderDeliveryTime.text = ""
                }
            )
        }
    }


    private fun loadSavedData(
        etFirstName: EditText, etLastName: EditText, etCardFullName: EditText,
        etCardNumber: EditText, etExpireMonth: EditText, etExpireYear: EditText, etCardCVV: EditText
    ) {
        val userProfile = preferencesHelper.getUserProfile()
        etFirstName.setText(userProfile["first_name"] as String)
        etLastName.setText(userProfile["last_name"] as String)
        etCardFullName.setText(userProfile["card_full_name"] as String)
        etCardNumber.setText(userProfile["card_number"] as String)
        etExpireMonth.setText((userProfile["expire_month"] as Int).toString())
        etExpireYear.setText((userProfile["expire_year"] as Int).toString())
        etCardCVV.setText(userProfile["card_cvv"] as String)
    }

    private fun fetchLastOrderStatus() {
        val sid = preferencesHelper.getSid()
        val oid = preferencesHelper.getOid()

        if (sid.isEmpty() || oid == -1) {
            tvOrderStatus.text = "Non hai effettuato ancora nessun ordine."
            return
        }

        profileViewModel.fetchOrderStatus(
            oid = oid,
            sid = sid,
            onSuccess = { orderStatus ->
                tvOrderStatus.text = """
                    Ultimo ordine:
                    Stato: ${orderStatus.status}
                    Posizione consegna: ${orderStatus.deliveryLocation.lat}, ${orderStatus.deliveryLocation.lng}
                    Consegna prevista: ${orderStatus.expectedDeliveryTimestamp}
                """.trimIndent()
            },
            onError = { errorMessage ->
                tvOrderStatus.text = "Errore nel recupero dello stato dell'ordine: $errorMessage"
            }
        )
    }

    private fun validateInputs(
        firstName: String, lastName: String, cardFullName: String,
        cardNumber: String, expireMonth: Int, expireYear: Int, cardCVV: String
    ): Boolean {
        return firstName.isNotBlank() && lastName.isNotBlank() &&
                cardFullName.isNotBlank() && cardNumber.length == 16 &&
                expireMonth in 1..12 && expireYear > 2000 &&
                cardCVV.length == 3
    }
}
