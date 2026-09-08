package com.example.security

import android.content.Context
import android.content.SharedPreferences

class UserAccountManager(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("smail_user_account_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_USER_NAME = "key_user_name"
    const val DEFAULT_EMAIL = "pradeephongal17@gmail.com"
    const val DEFAULT_NAME = "Pradeep"
  }

  var isLoggedIn: Boolean
    get() = true
    set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

  var userEmail: String
    get() = prefs.getString(KEY_USER_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL
    set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

  var userName: String
    get() = prefs.getString(KEY_USER_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
    set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

  fun login(email: String, name: String) {
    userEmail = email
    userName = name.ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
    isLoggedIn = true
  }

  fun logout() {
    isLoggedIn = false
  }
}
