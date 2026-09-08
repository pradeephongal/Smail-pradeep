package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ml.NaiveBayesClassifier
import com.example.security.AppLockManager
import com.example.security.BiometricAuthHelper
import com.example.security.UserAccountManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smail", appName)
  }

  @Test
  fun `user account login and persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val accountManager = UserAccountManager(context)
    accountManager.login("pradeephongal17@gmail.com", "Pradeep")

    assertTrue(accountManager.isLoggedIn)
    assertEquals("pradeephongal17@gmail.com", accountManager.userEmail)
    assertEquals("Pradeep", accountManager.userName)
  }

  @Test
  fun `app lock manager default pin and verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val lockManager = AppLockManager(context)

    assertTrue(lockManager.verifyPin("1234"))
    assertFalse(lockManager.verifyPin("9999"))
  }

  @Test
  fun `naive bayes detects spam and ham correctly`() {
    val classifier = NaiveBayesClassifier()
    val spamResult = classifier.classify(
      "URGENT: Claim your $1,000,000 lottery cash prize now!",
      "Congratulations! Your email has won cash lottery. Click here to verify bank account wire transfer."
    )
    assertTrue(spamResult.isSpam)
    assertTrue(spamResult.spamProbability > 0.6)

    val hamResult = classifier.classify(
      "Team meeting agenda for Thursday",
      "Hi everyone, please find attached the slide deck and notes for our sprint retro."
    )
    assertFalse(hamResult.isSpam)
  }

  @Test
  fun `biometric auth helper checks availability safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // In Robolectric environment, this executes safely without throwing exceptions
    val canAuth = BiometricAuthHelper.canAuthenticate(context)
    assertNotNull(canAuth)
  }
}
