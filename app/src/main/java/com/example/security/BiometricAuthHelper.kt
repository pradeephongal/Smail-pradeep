package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthHelper {

  fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
      BiometricManager.BIOMETRIC_SUCCESS -> true
      else -> false
    }
  }

  fun canAuthenticate(context: Context): Int {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(
      BiometricManager.Authenticators.BIOMETRIC_STRONG or
          BiometricManager.Authenticators.BIOMETRIC_WEAK or
          BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
  }

  fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val executor = ContextCompat.getMainExecutor(activity)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        // If user cancelled, don't show fatal error
        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
            errorCode != BiometricPrompt.ERROR_CANCELED) {
          onError(errString.toString())
        } else {
          onError("Authentication cancelled")
        }
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        onError("Biometric authentication failed. Try again or enter PIN.")
      }
    }

    val biometricPrompt = BiometricPrompt(activity, executor, callback)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("Smail Biometric Security")
      .setSubtitle("Confirm fingerprint or face to open inbox")
      .setDescription("End-to-end local security • Created by @PRADEEP")
      .setNegativeButtonText("Use Security PIN")
      .setConfirmationRequired(true)
      .build()

    try {
      biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
      onError("Biometric error: ${e.message}")
    }
  }
}
