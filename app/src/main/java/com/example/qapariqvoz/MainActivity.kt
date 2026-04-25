package com.example.qapariqvoz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)

        val seekBarSpeed = findViewById<SeekBar>(R.id.seekBarSpeed)
        val seekBarPitch = findViewById<SeekBar>(R.id.seekBarPitch)
        val btnPrueba = findViewById<MaterialCardView>(R.id.btnPrueba)
        val btnPermisos = findViewById<MaterialCardView>(R.id.btnPermisos)

        // 1. Abrimos la "libreta de notas" de Android
        val sharedPref = getSharedPreferences("QapariqPrefs", Context.MODE_PRIVATE)

        // 2. Cargamos los valores guardados (o 1.0 por defecto) para que las barras inicien donde las dejaste
        val savedSpeed = sharedPref.getFloat("velocidad", 1.0f)
        val savedPitch = sharedPref.getFloat("tono", 1.0f)

        seekBarSpeed.progress = (savedSpeed * 50).toInt()
        seekBarPitch.progress = (savedPitch * 50).toInt()

        // Lógica de Velocidad
        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentSpeed = (progress.toFloat() / 50f).coerceAtLeast(0.1f)
                // Guardamos en la libreta
                sharedPref.edit().putFloat("velocidad", currentSpeed).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Lógica de Tono
        seekBarPitch.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentPitch = (progress.toFloat() / 50f).coerceAtLeast(0.1f)
                // Guardamos en la libreta
                sharedPref.edit().putFloat("tono", currentPitch).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnPrueba.setOnClickListener {
            // Leemos la libreta justo antes de probar
            val speed = sharedPref.getFloat("velocidad", 1.0f)
            val pitch = sharedPref.getFloat("tono", 1.0f)

            tts?.setSpeechRate(speed)
            tts?.setPitch(pitch)

            tts?.speak("¡Cuchau! La voz está configurada correctamente.", TextToSpeech.QUEUE_FLUSH, null, null)
            Toast.makeText(this, "Probando voz...", Toast.LENGTH_SHORT).show()
        }

        btnPermisos.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}