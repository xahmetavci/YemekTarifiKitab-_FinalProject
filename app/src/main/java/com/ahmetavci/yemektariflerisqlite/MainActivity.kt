package com.ahmetavci.yemektariflerisqlite

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.Navigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater //bir xml ile kod içersindeki bir elemanı bağlamak için "Inflater" yapısını kullanıyorduk.
        menuInflater.inflate(R.menu.yemek_ekle, menu) //buradaki önemli püf nokta res dizini altında oluşturduğumuz klasörün !"menu" olmasına dikkat ediniz.

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Options Menuden bişey seçilirse ne yapayım.
        if (item.itemId == R.id.yemek_ekleme_item){ //id kontrolü yapıyoruz. Ve kontorol başırışlı ile sonuçlanırse seçilen item üzerinden yemek tarifleri fragment sayfasına gideceğiz
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim",0) //gideceğim yeri action içinde tanımladık
            Navigation.findNavController(this, R.id.fragment).navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }

}