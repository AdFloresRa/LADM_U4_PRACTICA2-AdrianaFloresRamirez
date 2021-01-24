package mx.tecnm.ladm_u4_practica2adrianaflores

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var dataLista = ArrayList<String>()
    var listaID = ArrayList<String>()

    val siPermiso = 1
    val siPermisoReceiver = 2
    val siPermisoLectura = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("MENU HIELITOS")

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_SMS),siPermisoLectura)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoReceiver)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.SEND_SMS), siPermiso)
        }



        baseRemota.collection("hielitos").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                //Si es diferente de null, hay un error
                mensaje("ERROR: No se puede acceder a consulta")
                return@addSnapshotListener
            }
            dataLista.clear()
            listaID.clear()
            for(document in querySnapshot!!){
                var cadena = document.getString("hielito")+("\n")+document.getString("precio")+ ("\n")

                dataLista.add(cadena)
                listaID.add(document.id)
            }
            if(dataLista.size == 0){
                dataLista.add("No hay datos")
            }
            var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataLista)
            lista.adapter = adapter
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            if(listaID.size == 0){
                return@setOnItemClickListener
            }
            AlertaEliminar(position)
        }

        button.setOnClickListener {
            aggDatos()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == siPermiso){

        }
        if(requestCode == siPermisoReceiver){

        }
        if(requestCode == siPermisoLectura){

        }
    }

    private fun AlertaEliminar(position: Int) {
        AlertDialog.Builder(this).setTitle("ATENCIÓN")
            .setMessage("¿Qué desea hacer con este hielito?")
            .setPositiveButton("Eliminar"){d,w ->
                eliminar(listaID[position])
            }
            .setNeutralButton("Cancelar"){dialog, wich ->

            }.show()
    }


    private fun eliminar(idEliminar: String) {
        baseRemota.collection("hielitos").document(idEliminar).delete()
            .addOnSuccessListener {
                mensaje("Hielito eliminado del menú")
            }
            .addOnFailureListener {
                mensaje("No se pudo eliminar")
            }
    }

    private fun aggDatos() {

        var datos = hashMapOf(
            "hielito" to hielito.text.toString(),
            "precio"   to precio.text.toString()
        )

        baseRemota.collection("hielitos").add(datos)
            .addOnSuccessListener {
                mensaje("Hielito agregado con éxito a la base de datos")
                limpiarCampos()
            }
            .addOnFailureListener {
                mensaje("No se pudo hacer la actualización de los datos")
            }
    }

    private fun limpiarCampos() {
        hielito.setText("")
        precio.setText("")
    }

    private fun mensaje(msj: String){
        Toast.makeText(this, msj, Toast.LENGTH_LONG).show()
    }
}