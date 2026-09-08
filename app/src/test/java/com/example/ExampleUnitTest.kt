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

    val updatedResult = classifier.classify("Bonus notification", text)
    assertTrue(
      "Ham probability should increase after training",
      updatedResult.hamProbability >= initialResult.hamProbability
    )
  }
}
