package com.dubhe.simple

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dubhe.headerselector.R
import kotlinx.android.synthetic.main.main.*

@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        btnJava.setOnClickListener {
            startActivity(Intent(this, MainActivity_java::class.java))
        }
        btnKotlin.setOnClickListener {
            startActivity(Intent(this, MainActivity_kotlin::class.java))
        }
    }
}