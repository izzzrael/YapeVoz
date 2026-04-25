package com.example.qapariqvoz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class YapeService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val testReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            hablar("¡Cuchau! QapariqVoz está configurado correctamente.")
        }
    }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)

        val filter = IntentFilter("TEST_VOZ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(testReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(testReceiver, filter)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "PE")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: ""

        if (packageName.contains("com.bcp.innovabcp") || packageName.contains("yape")) {
            val extras = sbn?.notification?.extras
            val texto = extras?.getString("android.text") ?: ""

            // 1. Extraemos el NOMBRE (lo que está antes de " te envió")
            val nombre = texto.substringBefore(" te envió", "Alguien")

            // 2. Extraemos el MONTO usando una Regex que detecta números con decimales
            // Busca lo que sigue después de "S/ " y permite puntos decimales
            val regexMonto = "S/ ([0-9]+(?:\\.[0-9]+)?)".toRegex()
            val match = regexMonto.find(texto)
            val monto = match?.groupValues?.get(1) ?: ""

            // 3. Formato de voz solicitado: "Nombre + te envió un pago por + cantidad"
            if (monto.isNotEmpty()) {
                hablar("$nombre te envió un pago por $monto soles")
            } else {
                // Si algo falla, lee el mensaje básico para no perder la info
                hablar("$nombre te envió un pago. Revisa el monto.")
            }
        }
    }

    private fun hablar(mensaje: String) {
        // Abrimos la misma libreta de notas
        val sharedPref = getSharedPreferences("QapariqPrefs", Context.MODE_PRIVATE)

        // Leemos cómo quiere Israel que suene hoy
        val speed = sharedPref.getFloat("velocidad", 1.0f)
        val pitch = sharedPref.getFloat("tono", 1.0f)

        // Aplicamos los ajustes al locutor del servicio
        tts?.setSpeechRate(speed)
        tts?.setPitch(pitch)

        // ¡A hablar!
        tts?.speak(mensaje, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        unregisterReceiver(testReceiver)
    }
}