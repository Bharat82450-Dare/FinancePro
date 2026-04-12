package com.financeapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.financeapp.R
import com.financeapp.databinding.ActivityAuthBinding

/**
 * AuthActivity handles the Login and Signup flows.
 * It manages switches between LoginFragment and SignupFragment.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle routing based on intent (e.g. from Signup or Splash)
        val showPin = intent.getBooleanExtra("SHOW_PIN", false)
        
        if (savedInstanceState == null) {
            if (showPin) {
                showPin(false)
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }
        }
    }

    fun showSignup() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.slide_out_right)
            .replace(R.id.fragment_container, SignupFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showPin(isSetup: Boolean = false) {
        val fragment = PinFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isSetup", isSetup)
            }
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun showLogin() {
        supportFragmentManager.popBackStack()
    }
}
