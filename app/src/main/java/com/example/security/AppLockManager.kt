package com.example.security

import android.content.Context
import android.content.SharedPreferences

class AppLockManager(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("smail_app_lock_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_LOCK_ENABLED = "key_lock_enabled"
    private const val KEY_PIN = "key_pin"
    const val DEFAULT_PIN = "1234"
  }

  var isAppLockEnabled: Boolean
    get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)
    set(value) = prefs.edit().putBoolean(KEY_LOCK_ENABLED, value).apply()

  var pin: String
    get() = prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    set(value) = prefs.edit().putString(KEY_PIN, value).apply()

  fun verifyPin(enteredPin: String): Boolean {
    return enteredPin == pin
  }
}
