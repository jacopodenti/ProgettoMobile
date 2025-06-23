package com.example.mangiabasta.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mangiabasta.R

class IngredientsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        // 1) Recupera la lista di nomi passata dall'Intent
        val names = intent.getStringArrayListExtra("INGREDIENTS") ?: arrayListOf()

        // 2) Trova il TextView e stampala
        val tv = findViewById<TextView>(R.id.tv_ingredients)
        tv.text = if (names.isNotEmpty()) {
            names.joinToString("\n") { "• $it" }
        } else {
            "Nessun ingrediente disponibile"
        }
        // Trova il pulsante e gestisci il click
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Chiude l'Activity e torna indietro
        }
    }
}
