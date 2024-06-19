package com.example.worker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.worker.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

class Splashactivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashactivity)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        },3000)
    }
}