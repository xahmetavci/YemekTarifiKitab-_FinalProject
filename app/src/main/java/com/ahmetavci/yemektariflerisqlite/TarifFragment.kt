package com.ahmetavci.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Telephony.Mms.Intents
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { //Bu fonksiyon görünümler oluşturuldukdan sonra çağırılmaktaydı.
        super.onViewCreated(view, savedInstanceState)
        //!Fragment lara özel bir durum var butona bir fonskiyon atıyorsak bu nu setOnClick Listener ile yapmamız gerekmektedir
        var butonum = view.findViewById<Button>(R.id.button)
        butonum.setOnClickListener {
            kaydet(it)
        }
        var image = view.findViewById<ImageView>(R.id.imageView)
        image.setOnClickListener {
            gorselSec(it)
        }

        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (gelenBilgi.equals("menudengeldim")){
                //Kullanıcı, yeni bir yemek eklemeye geldi
                var yemekText = requireView().findViewById<EditText>(R.id.yemekIsmiText)
                var malzemeText = requireView().findViewById<EditText>(R.id.yemekMalzemeleriText)
                var butonum = requireView().findViewById<Button>(R.id.button)
                yemekText.setText("")
                malzemeText.setText("")
                butonum.visibility = View.VISIBLE //buton görünür

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.yenigorselsecimi)
                var image = requireView().findViewById<ImageView>(R.id.imageView)
                image.setImageBitmap(gorselSecmeArkaPlani)

            } else {
                //Kullanıcı, daha önce oluşturulan yemeğin verilerini okumaya geldi
                butonum.visibility = View.INVISIBLE //kullanıcı yeni yemek verisi girmeyeceği için button görünmez konuma geçti

                val secilenId = TarifFragmentArgs.fromBundle(it).id

                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString())) //sadece seçilen yemeğin verilerini çekebilirim.

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            var yemekIsmi = requireView().findViewById<EditText>(R.id.yemekIsmiText)
                            var yemekMalzeme = requireView().findViewById<EditText>(R.id.yemekMalzemeleriText)
                            var gorsel = requireView().findViewById<ImageView>(R.id.imageView)

                            yemekIsmi.setText(cursor.getString(yemekIsmiIndex))
                            yemekMalzeme.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi = cursor.getBlob(yemekGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size) //Aldığımız byte dizisini tekrar "bitmap'e" çevirme işlemi.
                            gorsel.setImageBitmap(bitmap)
                        }

                        cursor.close() //imlecimizi kapattık

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }

            }

        }

    }

    fun kaydet(view: View){
        //SQLite'a Kaydetme
        var yemekIsmiGirdisi = requireView().findViewById<EditText>(R.id.yemekIsmiText)
        val yemekIsmi = yemekIsmiGirdisi.text.toString()

        var yemekMalzemeGirdisi = requireView().findViewById<EditText>(R.id.yemekMalzemeleriText)
        val yemekMalzemeleri = yemekMalzemeGirdisi.text.toString()

        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try{
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")

                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,yemekMalzemeleri)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()
                }

            } catch (e: Exception){
                e.printStackTrace()
            }

            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }

    }

    /*
    fun kaydet(view: View) { //OnViewCreated altında görünümümüzü tanımladığımız zamam kaydet fonksiyonumuz artık kullanılır hale gelmiştir.
        //println("Yemek Verisi Kaydedildi") //logcat üzerinde "Yemek Verisi Kaydedildi" logunu görülmüştür.

        //SQLite'a Kaydetme
        var yemekIsmiGirdisi = view.findViewById<EditText>(R.id.yemekIsmiText)
        val yemekIsmi = yemekIsmiGirdisi.text.toString()

        var yemekMalzemeGirdisi = view.findViewById<EditText>(R.id.yemekMalzemeleriText)
        val yemekMalzemeleri = yemekMalzemeGirdisi.text.toString()

        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(
                secilenBitmap!!,
                300
            ) //Maksimum boyutu 300 değilde 400 veya 500 yaptığımızda "1mb" altında kalacaktır.
            //Bir "bitmap" veriye çevirmek için gereken kod satırları
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            //yemekIsmi, yemekMalzemeleri ve byteDizimizi oluşturduk. SQLite a görsel kaydetmek için 1mb'ın üzerinde görsel kabul etmediği için bunu küçülttük ve byteDizisi'ne ekledik şimdi tablo oluşturup veri tabanımıza veri kaydetme işlemlerine geçelim.
            try {
                context?.let {
                    val database =
                        it.openOrCreateDatabase("YemekTarifiKitabi", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")

                    val sqlString =
                        "INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES (?, ?, ?)"
                    val statement =
                        database.compileStatement(sqlString) //Burdaki 'statement' bir üst satırdaki string bir ifadeyi SQL kodu gibi çalışmasına olanak sağlamaktadır.

                    statement.bindString(
                        1,
                        yemekIsmi
                    ) // Kafa karışıklığı oluşmasın! buradaki index '0' dan değil '1' den başlamaktadır
                    statement.bindString(2, yemekMalzemeleri)
                    statement.bindBlob(3, byteDizisi)
                    statement.execute()//SQLite üzerinde çalıştırmak için kod satırı.
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Liste fragramanına dönmek için Action oluşturacağım
            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }
    */

    fun gorselSec(view: View){
        //println("Görsel Seçmek İçin Tıklandı") //logcat üzerinde "Görsel Seçmek İçin Tıklandı" logunu görülmüştür.

        activity?.let{ // activity var ise var, yok ise yok manasına gelmektedir
            if (ContextCompat.checkSelfPermission(it.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ //izin verilmedi izin istememiz gerekmektedir.
                //checkSelfPermission acticity den çağırmak yerine ContextCompat den çağırmak daha sağlıklı olacaktır. Sebebi ise Compatible(uyumlu) olması API kaç olursa olsun uyumlu olucaktır.
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            } else  {
                // izin zaten verilmiş, tekrar istemeden galeriye git.
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //kullanıcının galersine erişip görsel alacağız
                startActivityForResult(galeriIntent,2)
            }

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izni aldık. İzni aldığımıza göre galeriye gidebiliriz
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //kullanıcının galersine erişip görsel alacağız
                startActivityForResult(galeriIntent,2)
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            secilenGorsel = data.data //Uri nullable vermektedir

            try {

                context?.let {
                    if (secilenGorsel != null) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(it.contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            var image = requireView().findViewById<ImageView>(R.id.imageView)
                            image.setImageBitmap(secilenBitmap)

                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver, secilenGorsel)
                            var image = requireView().findViewById<ImageView>(R.id.imageView)
                            image.setImageBitmap(secilenBitmap)
                        }

                    }

                }

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur (kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap {
        var width = kullanicininSectigiBitmap.width //güncel genişlik boyutu (kullanıcı versine göre)
        var height = kullanicininSectigiBitmap.height //güncel yükseklik boyutu (kullanıcı versine göre)

        val bitmapOrani : Double = width.toDouble()/height.toDouble()

        if (bitmapOrani > 1) {
            //Görselimiz 'yatay' konumdadır.
            width = maximumBoyut
            val kısaltilmisHeight = width / bitmapOrani
            height = kısaltilmisHeight.toInt()

        } else {
            //Görselimiz 'dikey' konumdadır.
            height = maximumBoyut
            val kısaltilmisWidth = height * bitmapOrani
            width = kısaltilmisWidth.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true) //kaynak bitmap'i alıp daha küçük bir boyuta döndürmeyi sağlamaktadır.
    }

}
