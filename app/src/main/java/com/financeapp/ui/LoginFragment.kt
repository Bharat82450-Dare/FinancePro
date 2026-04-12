package com.financeapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.financeapp.MainActivity
import com.financeapp.databinding.FragmentLoginBinding
import com.financeapp.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

/**
 * LoginFragment handles user credentials and initiates login via Firebase.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())

        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.loginBtn.isEnabled = false
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        binding.loginBtn.isEnabled = true
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            sessionManager.createLoginSession(email, user?.uid ?: "")
                            
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            (activity as? AuthActivity)?.showPin()
                        } else {
                            Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupLink.setOnClickListener {
            (activity as? AuthActivity)?.showSignup()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
