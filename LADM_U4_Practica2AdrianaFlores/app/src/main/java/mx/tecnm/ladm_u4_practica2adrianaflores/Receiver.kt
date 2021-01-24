package mx.tecnm.ladm_u4_practica2adrianaflores

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class Receiver : BroadcastReceiver() {
    var baseRemota = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context?, intent: Intent) {
        var cadMSJ = ""
        val extras = intent.extras
        if(extras != null) {
            var sms = extras.get("pdus") as Array<Any>
            for (indice in sms.indices) {
                var formato = extras.getString("format")

                var smsMensaje = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray, formato)
                } else {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                var contacto = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()
                var cadena = contenidoSMS.split(" ")
                var envio = ""
                Toast.makeText(context, "Recibiste mensaje de: " + contacto, Toast.LENGTH_LONG).show()

                if (cadena.size != 2) {

                } else {
                    if (cadena[1] != "precio") {
                        SmsManager.getDefault().sendTextMessage(
                            contacto, null,
                            "ERROR!! la sintaxis incluye los parentesis: (precio)(Hielito a consultar)", null, null
                        )
                    } else {
                        if (validarHielito(cadena[2])) {
                            SmsManager.getDefault().sendTextMessage(
                                contacto, null,
                                "ERROR: El hielito indicado no existe", null, null
                            )
                        } else {
                            try {
                                baseRemota.collection("hielitos").whereEqualTo("hielito",cadena[2]).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    for(document in querySnapshot!!){
                                        cadMSJ = document.getString("hielito")+(" $")+document.getString("precio")
                                    }
                                    SmsManager.getDefault().sendTextMessage(
                                        contacto,null,
                                        ""+cadMSJ,null,null)
                                }
                            }catch (e: FirebaseFirestoreException){
                                SmsManager.getDefault().sendTextMessage(
                                    contacto,null,
                                    e.message,null,null)

                            }

                        }
                    }
                }
            }
        }
    }

    private fun validarHielito(s: String): Boolean {
        var hielitoValido = true
        baseRemota.collection("hielitos").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            for(document in querySnapshot!!){
                if(document.getString("hielito").toString() != s){
                    hielitoValido = false
                }
            }
        }
        return hielitoValido
    }
}