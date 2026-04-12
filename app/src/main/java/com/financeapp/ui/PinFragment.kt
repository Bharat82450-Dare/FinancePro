package com.financeapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.financeapp.MainActivity
import com.financeapp.R
import com.financeapp.databinding.FragmentPinBinding
import com.financeapp.utils.SessionManager
import com.google.android.material.button.MaterialButton
import java.util.concurrent.Executor

/**
 * PinFragment handles PIN setup and authentication.
 */
class PinFragment : Fragment() {

    private var _binding: FragmentPinBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private var enteredPin = ""
    private var isSetupMode = false
    private var isConfirming = false
    private var firstPin = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Determine mode
        isSetupMode = !sessionManager.isPinSet()
        updateTitle()

        setupKeypad()
        
        if (!isSetupMode && sessionManager.isBiometricEnabled()) {
            binding.btnBiometric.visibility = View.VISIBLE
            showBiometricPrompt()
        }

        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        binding.btnDelete.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updateDots()
            }
        }
    }

    private fun updateTitle() {
        binding.pinTitle.text = if (isSetupMode) {
            if (isConfirming) "Confirm PIN" else "Set Security PIN"
        } else {
            "Enter PIN"
        }
        binding.pinSubtitle.text = if (isSetupMode) {
            "Create a 4-digit PIN for security"
        } else {
            "Authenticate to access your finances"
        }
    }

    private fun setupKeypad() {
        // Collect all MaterialButtons from the nested LinearLayout structure
        val allButtons = mutableListOf<MaterialButton>()
        
        fun collectButtons(view: View) {
            if (view is MaterialButton) {
                allButtons.add(view)
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    collectButtons(view.getChildAt(i))
                }
            }
        }
        
        collectButtons(binding.keypad)

        allButtons.forEach { button ->
            button.setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin += button.text.toString()
                    updateDots()
                    if (enteredPin.length == 4) {
                        handleFullPin()
                    }
                }
            }
        }
    }

    private fun handleFullPin() {
        if (isSetupMode) {
            if (!isConfirming) {
                firstPin = enteredPin
                enteredPin = ""
                isConfirming = true
                updateDots()
                updateTitle()
                Toast.makeText(context, "Please confirm your PIN", Toast.LENGTH_SHORT).show()
            } else {
                if (enteredPin == firstPin) {
                    sessionManager.setPin(enteredPin)
                    sessionManager.setBiometricEnabled(true) // Enable by default if hardware supports it
                    onAuthSuccess()
                } else {
                    Toast.makeText(context, "PINs do not match. Start over.", Toast.LENGTH_SHORT).show()
                    enteredPin = ""
                    isConfirming = false
                    updateDots()
                    updateTitle()
                }
            }
        } else {
            if (enteredPin == sessionManager.getPin()) {
                onAuthSuccess()
            } else {
                Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                enteredPin = ""
                updateDots()
            }
        }
    }

    private fun updateDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, view ->
            if (index < enteredPin.length) {
                view.setBackgroundResource(R.drawable.dot_on)
            } else {
                view.setBackgroundResource(R.drawable.dot_off)
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (isAdded) {
                        onAuthSuccess()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Keep PIN as fallback
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use fingerprint to unlock FinancePro")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun onAuthSuccess() {
        if (!isAdded) return
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
