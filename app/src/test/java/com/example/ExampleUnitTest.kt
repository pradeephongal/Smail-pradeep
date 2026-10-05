package com.example

import com.example.ml.NaiveBayesClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testNaiveBayesSpamDetection() {
    val classifier = NaiveBayesClassifier()

    // Test obvious spam
    val spamResult = classifier.classify(
      subject = "CONGRATULATIONS! Claim your $1,000,000 lottery cash prize now!",
      body = "Click here urgently to verify your bank account and receive immediate wire transfer."
    )
    assertTrue("Should classify obvious spam correctly", spamResult.isSpam)
    assertTrue("Spam probability should be > 0.8", spamResult.spamProbability > 0.80f)
    assertTrue("Should extract trigger words", spamResult.topTriggerWords.isNotEmpty())

    // Test obvious ham (clean email)
    val hamResult = classifier.classify(
      subject = "Weekly team sprint retro and architecture sync",
      body = "Hi team, please review the notes attached for our sync tomorrow at 2 PM. Great work this week!"
    )
    assertFalse("Should classify legit email as ham", hamResult.isSpam)
    assertTrue("Ham probability should be > 0.7", hamResult.hamProbability > 0.70f)
  }

  @Test
  fun testActiveLearningIncrementalRetrain() {
    val classifier = NaiveBayesClassifier()

    // Custom ambiguous sentence
    val text = "Special internal update regarding quarterly project bonuses"
    val initialResult = classifier.classify("Bonus notification", text)

    // Train heavily with this as ham
    repeat(10) {
      classifier.train(text, isSpam = false)
    }

    val updatedResult = classifier.classify(subject = "Bonus notification", body = text)
    assertTrue(
      "Ham probability should increase after training",
      updatedResult.hamProbability >= initialResult.hamProbability
    )
  }

  @Test
  fun testFakeEmailSenderDetection() {
    val classifier = NaiveBayesClassifier()

    // 1. Spoofed Google Security alert from suspicious domain
    val fakeGoogleResult = classifier.classify(
      senderName = "Google Security Team",
      senderEmail = "no-reply@security-google-verify.top",
      subject = "Your account was compromised",
      body = "Please login to verify your identity"
    )
    assertTrue("Should detect spoofed brand as spam", fakeGoogleResult.isSpam)
    assertFalse("Should not be marked as original", fakeGoogleResult.isOriginal)

    // 2. Disposable temporary email
    val disposableResult = classifier.classify(
      senderName = "Support Agent",
      senderEmail = "user99@tempmail.com",
      subject = "Hello friend",
      body = "Check out this document"
    )
    assertTrue("Disposable email should be flagged as spam", disposableResult.isSpam)
    assertFalse("Disposable should not be marked as original", disposableResult.isOriginal)

    // 3. Genuine real email from official domain
    val legitResult = classifier.classify(
      senderName = "Google Account",
      senderEmail = "no-reply@accounts.google.com",
      subject = "Security alert",
      body = "A new sign-in on Android device"
    )
    assertFalse("Genuine sender should not be flagged as spam", legitResult.isSpam)
    assertTrue("Genuine sender should be marked as original", legitResult.isOriginal)

    // 4. Friend discussing Google Docs or Netflix (subject mentions brand, but personal sender)
    val casualFriendResult = classifier.classify(
      senderName = "Liam Miller",
      senderEmail = "liam.miller@gmail.com",
      subject = "Google Doc link for project notes",
      body = "Hey! Here are the meeting notes from yesterday. Let me know what you think."
    )
    assertFalse("Personal email mentioning brand in subject should be safe", casualFriendResult.isSpam)
    assertTrue("Personal email should be marked as original", casualFriendResult.isOriginal)
  }
}
