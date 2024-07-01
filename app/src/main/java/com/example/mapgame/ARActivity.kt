package com.example.mapgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ARActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnAR: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        btnAR = findViewById(R.id.button4)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        btnAR.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            setResult(RESULT_OK, intent);
            finish();
        }

    }
}