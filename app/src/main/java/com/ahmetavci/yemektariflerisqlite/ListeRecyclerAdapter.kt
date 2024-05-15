package com.ahmetavci.yemektariflerisqlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class ListeRecyclerAdapter(val yemekListesi : ArrayList<String>, val idListesi : ArrayList<Int>) : RecyclerView.Adapter<ListeRecyclerAdapter.YemekHolder>() {

    class YemekHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder { //Hangi Row'u hangi tasarımla oluşturacağımızı belirttiğimiz fonkiyondur.
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return YemekHolder(view)
    }

    override fun getItemCount(): Int { //Kaç adet Recycler Row oluşturucağımızı söyleyecektir.
        return yemekListesi.size
    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) { //yukarıda aldığım liste her seferinde ilgili pozisyona yerleştirilmektedir.
        holder.itemView.findViewById<TextView>(R.id.recyclerRowText).text = yemekListesi[position]
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("recyclerdangeldim",idListesi[position])
            Navigation.findNavController(it).navigate(action)
        }

    }

}