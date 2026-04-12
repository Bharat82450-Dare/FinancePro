package com.financeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.financeapp.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * SignupFragment handles user registration using Firebase Authentication.
 */
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                binding.signupBtn.isEnabled = false
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        binding.signupBtn.isEnabled = true
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            (activity as? AuthActivity)?.showPin(true)
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginLink.setOnClickListener {
            (activity as? AuthActivity)?.showLogin()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
