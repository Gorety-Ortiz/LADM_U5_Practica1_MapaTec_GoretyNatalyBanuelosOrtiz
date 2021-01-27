package mx.tecnm.tepic.ladm_u5_practica1_mapatec2


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    var REQUEST_PERMISOS = 111
    lateinit var locacion : LocationManager
    var pos1 : Location = Location("")
    var pos2 : Location = Location("")

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Permisos()

        baseRemota.collection("tecnologico")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if(firebaseFirestoreException != null){
                        TVUbicaciones.setText("ERROR: "+firebaseFirestoreException.message)
                        return@addSnapshotListener
                    }

                    var resultado = ""
                    posicion.clear()
                    for(document in querySnapshot!!){
                        var data = Data()
                        data.nombre = document.getString("nombre").toString()
                        data.posicion1 = document.getGeoPoint("posicion1")!!
                        data.posicion2 = document.getGeoPoint("posicion2")!!
                        data.contiene = document.getString("contiene").toString()

                        resultado += data.toString()+"\n\n"
                        posicion.add(data)
                    }

                    TVUbicaciones.setText("Ubicaciones:\n"+resultado)
                }


        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, oyente)

        btnBuscar.setOnClickListener {
            baseRemota.collection("tecnologico")
                    .whereEqualTo("nombre", txtBuscar.getText().toString())
                    .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                        if(firebaseFirestoreException != null){
                            TVBuscar.setText("ERROR, NO HAY CONEXIÓN CON LA BD")
                            return@addSnapshotListener
                        }


                        var contiene = ""

                        for(document in  querySnapshot!!){
                            pos1.longitude = document.getGeoPoint("posicion1")!!.longitude
                            pos1.latitude = document.getGeoPoint("posicion1")!!.latitude

                            pos2.longitude = document.getGeoPoint("posicion2")!!.longitude
                            pos2.latitude = document.getGeoPoint("posicion2")!!.latitude
                            contiene = document.getString("contiene")!!
                        }

                        var r = "Coordenadas:\n(${(pos1.latitude)}, ${pos1.longitude}),(${pos2.latitude}, ${pos2.longitude})"
                        r = r + "\nDentro: ${contiene}"
                        TVBuscar.setText(r)
                    }
        }

    }

    // SOLICITAR PERMISOS
    private fun Permisos() {
        var permisoAccessFind = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)

        if(permisoAccessFind != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_PERMISOS)
        }
    }
}

class Oyente(puntero:MainActivity) : LocationListener {
    var p = puntero

    override fun onLocationChanged(location: Location) {
        p.Pactual.setText("Ubicación actual:\n${location.latitude}, ${location.longitude}")
        p.Estasen.setText("")
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)

        for (item in p.posicion) {
            if (item.estoyEn(geoPosicionGPS)) {
                p.Estasen.setText("Te encuentras en: ${item.nombre}")
            }else{
                p.Estasen.setText("No estas en un sitio registrado")
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }
}