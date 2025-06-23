package com.example.mangiabasta.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mangiabasta.R
import com.example.mangiabasta.model.Menu

class MenuAdapter(
    private var menuList: List<Menu>,
    private val onMenuClick: (Menu) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menuName: TextView = view.findViewById(R.id.tvMenuName)
        val menuCost: TextView = view.findViewById(R.id.tvMenuCost)
        val menuDescription: TextView = view.findViewById(R.id.tvMenuDescription)
        val menuDeliveryTime: TextView = view.findViewById(R.id.tvDeliveryTime)
        val menuImage: ImageView = view.findViewById(R.id.ivMenuImage)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMenuClick(menuList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.menuName.text = menu.name
        holder.menuCost.text = "€${menu.price}"
        holder.menuDescription.text = menu.shortDescription
        // Calcola il tempo di consegna in minuti
        // Assicurati che deliveryTime sia convertito in Double per la divisione
        val deliveryTimeInMinutes = kotlin.math.ceil(menu.deliveryTime / 60.0).toInt()
        holder.menuDeliveryTime.text = "Tempo di consegna: $deliveryTimeInMinutes minuti"


        if (!menu.imageBase64.isNullOrEmpty()) {
            val decodedString = Base64.decode(menu.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            holder.menuImage.setImageBitmap(bitmap)
        } else {
            holder.menuImage.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newData: List<Menu>) {
        menuList = newData
        notifyDataSetChanged()
    }
}
