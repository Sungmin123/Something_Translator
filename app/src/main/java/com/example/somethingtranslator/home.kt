package com.example.somethingtranslator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log


class home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val speechButton: Button = findViewById(R.id.speech_version)
        val textButton: Button = findViewById(R.id.text_version)

        speechButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        textButton.setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))

        }

    }
}