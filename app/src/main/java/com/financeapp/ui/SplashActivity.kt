package com.financeapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.financeapp.MainActivity
import com.financeapp.R
import com.financeapp.utils.SessionManager

/**
 * SplashActivity serves as the initial entry point.
 * It checks for an active session and routes the user accordingly.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sessionManager = SessionManager(this)
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        // Artificial delay for splash experience
        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null) {
                // If logged in via Firebase, go to PIN verification in AuthActivity
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("SHOW_PIN", true)
                startActivity(intent)
            } else {
                // If not logged in, go to Authentication (Login)
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
