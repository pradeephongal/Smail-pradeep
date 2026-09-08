package com.example.ml

import kotlin.math.exp
import kotlin.math.ln

data class ClassificationResult(
  val isSpam: Boolean,
  val spamProbability: Float, // 0.0 to 1.0
  val hamProbability: Float,
  val logOddsRatio: Double,
  val topTriggerWords: List<WordImpact>,
  val extractedTokensCount: Int
)

data class WordImpact(
  val word: String,
  val impactScore: Double, // positive = spam bias, negative = ham bias
  val spamWordProb: Double,
  val hamWordProb: Double
)

data class TrainingSample(
  val text: String,
  val isSpam: Boolean
)

data class ModelMetrics(
  val totalSpamDocs: Int,
  val totalHamDocs: Int,
  val totalVocabularySize: Int,
  val totalSpamTokens: Long,
  val totalHamTokens: Long,
  val priorSpamProb: Double,
  val priorHamProb: Double,
  val topSpamTokens: List<Pair<String, Double>>,
  val topHamTokens: List<Pair<String, Double>>
)

class NaiveBayesClassifier(
  private val laplaceSmoothingAlpha: Double = 1.0,
  private val spamThreshold: Float = 0.50f
) {

  // Vocabulary frequency counts: word -> count in class
  private val spamWordCounts = mutableMapOf<String, Int>()
  private val hamWordCounts = mutableMapOf<String, Int>()
  private val vocabulary = mutableSetOf<String>()

  var spamDocsCount: Int = 0
    private set
  var hamDocsCount: Int = 0
    private set

  private var totalSpamTokens: Long = 0L
  private var totalHamTokens: Long = 0L

  companion object {
    val STOP_WORDS = setOf(
      "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
      "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being",
      "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't",
      "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
      "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
      "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
      "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
      "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it",
      "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
      "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
      "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
      "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so",
      "some", "such", "than", "that", "that's", "the", "their", "theirs", "them",
      "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll",
      "they're", "they've", "this", "those", "through", "to", "too", "under",
      "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're",
      "we've", "were", "weren't", "what", "what's", "when", "when's", "where",
      "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with",
      "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
      "your", "yours", "yourself", "yourselves"
    )
  }

  init {
    loadDefaultCorpus()
  }

  /**
   * Tokenize and preprocess input text with domain-specific feature engineering
   */
  fun tokenize(text: String): List<String> {
    val tokens = mutableListOf<String>()

    // Feature indicator checks
    if (text.contains("http://") || text.contains("https://") || text.contains("www.")) {
      tokens.add("__feature_link__")
    }
    if (text.contains("$") || text.contains("€") || text.contains("£") || text.contains("₹")) {
      tokens.add("__feature_currency__")
    }
    if (text.contains("!!!") || text.contains("???")) {
      tokens.add("__feature_multiple_punctuation__")
    }

    // Check for ALL CAPS words of length >= 3
    val allCapsWords = text.split("\\s+".toRegex()).count { word ->
      word.length >= 3 && word.all { it.isUpperCase() }
    }
    if (allCapsWords >= 2) {
      tokens.add("__feature_caps_shouting__")
    }

    // Standard word tokenization
    val clean = text.lowercase()
      .replace("[^a-z0-9_]".toRegex(), " ")
    val rawWords = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }

    for (word in rawWords) {
      if (word.length in 2..28 && word !in STOP_WORDS) {
        tokens.add(word)
      }
    }

    return tokens
  }

  /**
   * Train on a single labeled sample (Incremental Active Learning)
   */
  @Synchronized
  fun train(text: String, isSpam: Boolean) {
    val tokens = tokenize(text)
    if (isSpam) {
      spamDocsCount++
      for (token in tokens) {
        spamWordCounts[token] = (spamWordCounts[token] ?: 0) + 1
        totalSpamTokens++
        vocabulary.add(token)
      }
    } else {
      hamDocsCount++
      for (token in tokens) {
        hamWordCounts[token] = (hamWordCounts[token] ?: 0) + 1
        totalHamTokens++
        vocabulary.add(token)
      }
    }
  }

  /**
   * Train on a batch of samples
   */
  @Synchronized
  fun trainBatch(samples: List<TrainingSample>) {
    for (sample in samples) {
      train(sample.text, sample.isSpam)
    }
  }

  /**
   * Predict spam probability using Naïve Bayes with Laplace Smoothing and Log-Likelihood
   */
  fun classify(subject: String, body: String): ClassificationResult {
    val fullText = "$subject $subject $body" // Weigh subject twice
    val tokens = tokenize(fullText)

    if (tokens.isEmpty() || vocabulary.isEmpty()) {
      return ClassificationResult(
        isSpam = false,
        spamProbability = 0.1f,
        hamProbability = 0.9f,
        logOddsRatio = -2.0,
        topTriggerWords = emptyList(),
        extractedTokensCount = 0
      )
    }

    val totalDocs = spamDocsCount + hamDocsCount
    val vocabSize = vocabulary.size.coerceAtLeast(1)

    // Class Prior Probabilities with Laplace smoothing
    val priorSpam = (spamDocsCount + laplaceSmoothingAlpha) / (totalDocs + 2 * laplaceSmoothingAlpha)
    val priorHam = (hamDocsCount + laplaceSmoothingAlpha) / (totalDocs + 2 * laplaceSmoothingAlpha)

    var logProbSpam = ln(priorSpam)
    var logProbHam = ln(priorHam)

    // Denominators for conditional probability P(w|C) with Add-1 Laplace Smoothing
    val spamDenominator = totalSpamTokens + (laplaceSmoothingAlpha * vocabSize)
    val hamDenominator = totalHamTokens + (laplaceSmoothingAlpha * vocabSize)

    val wordImpacts = mutableListOf<WordImpact>()
    val seenTokens = mutableSetOf<String>()

    for (token in tokens) {
      val spamCount = spamWordCounts[token] ?: 0
      val hamCount = hamWordCounts[token] ?: 0

      val pTokenGivenSpam = (spamCount + laplaceSmoothingAlpha) / spamDenominator
      val pTokenGivenHam = (hamCount + laplaceSmoothingAlpha) / hamDenominator

      logProbSpam += ln(pTokenGivenSpam)
      logProbHam += ln(pTokenGivenHam)

      // Calculate word impact (log-odds contribution)
      if (token !in seenTokens) {
        seenTokens.add(token)
        val impact = ln(pTokenGivenSpam) - ln(pTokenGivenHam)
        // Clean display name for feature tokens
        val displayName = when (token) {
          "__feature_link__" -> "[Web Link]"
          "__feature_currency__" -> "[$ Currency]"
          "__feature_multiple_punctuation__" -> "[!!! Symbols]"
          "__feature_caps_shouting__" -> "[SHOUTING ALL-CAPS]"
          else -> token
        }
        wordImpacts.add(WordImpact(displayName, impact, pTokenGivenSpam, pTokenGivenHam))
      }
    }

    // Convert log-likelihood difference to posterior probability using logistic sigmoid
    val delta = logProbSpam - logProbHam
    val spamProb = if (delta > 50) {
      0.9999
    } else if (delta < -50) {
      0.0001
    } else {
      1.0 / (1.0 + exp(-delta))
    }

    val hamProb = 1.0 - spamProb
    val isSpam = spamProb.toFloat() >= spamThreshold

    // Sort word impacts by magnitude of contribution
    val sortedImpacts = wordImpacts.sortedByDescending { if (isSpam) it.impactScore else -it.impactScore }
      .take(7)

    return ClassificationResult(
      isSpam = isSpam,
      spamProbability = spamProb.toFloat(),
      hamProbability = hamProb.toFloat(),
      logOddsRatio = delta,
      topTriggerWords = sortedImpacts,
      extractedTokensCount = tokens.size
    )
  }

  /**
   * Get current internal model parameters and top predictive features
   */
  fun getMetrics(): ModelMetrics {
    val totalDocs = (spamDocsCount + hamDocsCount).coerceAtLeast(1)
    val vocabSize = vocabulary.size.coerceAtLeast(1)

    val priorSpam = (spamDocsCount + laplaceSmoothingAlpha) / (totalDocs + 2 * laplaceSmoothingAlpha)
    val priorHam = (hamDocsCount + laplaceSmoothingAlpha) / (totalDocs + 2 * laplaceSmoothingAlpha)

    val spamDenominator = totalSpamTokens + (laplaceSmoothingAlpha * vocabSize)
    val hamDenominator = totalHamTokens + (laplaceSmoothingAlpha * vocabSize)

    val tokenRatios = vocabulary.map { token ->
      val pSpam = ((spamWordCounts[token] ?: 0) + laplaceSmoothingAlpha) / spamDenominator
      val pHam = ((hamWordCounts[token] ?: 0) + laplaceSmoothingAlpha) / hamDenominator
      val ratio = ln(pSpam) - ln(pHam)
      token to ratio
    }

    val topSpam = tokenRatios.sortedByDescending { it.second }.take(10).map {
      val display = when (it.first) {
        "__feature_link__" -> "Links/URLs"
        "__feature_currency__" -> "$/Currency"
        "__feature_caps_shouting__" -> "ALL-CAPS"
        "__feature_multiple_punctuation__" -> "!!!"
        else -> it.first
      }
      display to it.second
    }

    val topHam = tokenRatios.sortedBy { it.second }.take(10).map {
      val display = when (it.first) {
        "__feature_link__" -> "Links/URLs"
        "__feature_currency__" -> "$/Currency"
        "__feature_caps_shouting__" -> "ALL-CAPS"
        "__feature_multiple_punctuation__" -> "!!!"
        else -> it.first
      }
      display to -it.second
    }

    return ModelMetrics(
      totalSpamDocs = spamDocsCount,
      totalHamDocs = hamDocsCount,
      totalVocabularySize = vocabulary.size,
      totalSpamTokens = totalSpamTokens,
      totalHamTokens = totalHamTokens,
      priorSpamProb = priorSpam,
      priorHamProb = priorHam,
      topSpamTokens = topSpam,
      topHamTokens = topHam
    )
  }

  /**
   * Reset classifier and restore standard baseline corpus
   */
  @Synchronized
  fun reset() {
    spamWordCounts.clear()
    hamWordCounts.clear()
    vocabulary.clear()
    spamDocsCount = 0
    hamDocsCount = 0
    totalSpamTokens = 0L
    totalHamTokens = 0L
    loadDefaultCorpus()
  }

  private fun loadDefaultCorpus() {
    // 20 High-signal spam seed examples covering phishing, lottery, crypto, fake invoice, urgent account lock
    val spamSeeds = listOf(
      "CONGRATULATIONS! You have won $1,000,000 lottery cash prize! Claim your prize now urgently. Click here to verify your bank account and receive immediate transfer.",
      "URGENT: Your PayPal account has been suspended due to unauthorized activity! Verify your password and billing info immediately or your account will be permanently locked.",
      "Double your Bitcoin in 24 hours! Send 0.1 BTC to receive 0.5 BTC guaranteed crypto giveaway bonus. Exclusive limited offer for lucky winners!",
      "Dear beneficiary, I am barrister Johnson from London. You have an unclaimed inheritance fund of $5.5 million waiting for wire transfer. Contact now.",
      "Invoice #93812 overdue! Failure to pay immediately will result in legal action. Download attached malware.zip to view invoice details.",
      "Hot singles in your area want to meet you tonight! Click here for free adult webcam access and instant match. No credit card required.",
      "Work from home and earn $5,000 weekly with zero experience! Quick cash guaranteed. Sign up now with your bank details to get started.",
      "Final Warning: Your Netflix subscription will expire in 2 hours. Update your payment method and credit card number now to prevent termination.",
      "Refinance your mortgage with 0% interest rate! Pre-approved loan offer of $250,000 for immediate cash payout. Apply online today!",
      "Diet secret doctors don't want you to know! Miracle weight loss pill burns 30lbs in 7 days without exercise. Buy 1 get 2 free discount offer.",
      "Amazon Security Alert: We detected an unusual sign-in from Russia. If this wasn't you, click link immediately to secure your credentials and reset password.",
      "You have won an Apple iPhone 15 Pro! Complete this 1-minute survey to claim your free reward voucher. Only 3 units remaining!",
      "IRS Tax Refund Notice: You have a pending tax refund of $1,420.90. Submit your Social Security Number and debit card details to release funds.",
      "Attention! Your computer is infected with 14 trojans. Call Microsoft Technical Support helpline immediately at 1-800-FAKE-NUM for urgent disinfection.",
      "Get rich quick with automated forex trading bots! 99.8% win rate guaranteed profit. Invest $100 today and watch your money multiply overnight.",
      "Action Required: Your mailbox storage is 99% full. Incoming messages are being blocked. Click here to upgrade quota and keep your email active.",
      "Exclusive VIP invitation! Free casino spins and $500 no-deposit chips. Play now and cash out real jackpot earnings instantly!",
      "Bank of America notice: Suspicious wire transaction of $8,400 initiated. Click here to cancel transaction and confirm identity.",
      "Weight loss miracle formula! FDA approved natural supplement eliminates belly fat fast. 50% discount coupon expires tonight.",
      "Claim your unclaimed government relief grant of $12,500. Direct deposit available upon verification of identity. Don't miss out!"
    )

    // 20 High-signal ham (normal) seed examples covering work, scheduling, personal, newsletters, order receipts
    val hamSeeds = listOf(
      "Hi team, let's schedule our sprint planning meeting for Thursday at 10 AM. Please review the updated product backlog before our sync.",
      "Hey Alex, are we still meeting for lunch tomorrow at the cafeteria? Let me know what time works best for you.",
      "Quarterly engineering review notes attached. Please take a look at our architecture diagram and comment on the proposed API changes.",
      "Hi Mom, I arrived safely at the hotel. The flight was smooth. Talk to you over the weekend for Sunday family dinner.",
      "GitHub: pull request #482 merged into main by Sarah. Automated unit test suite passed successfully on CI server.",
      "Your order #38291 has shipped! Track your package via UPS. Estimated delivery date is Tuesday afternoon.",
      "Hi professor, here is my final submission for the Machine Learning research paper on Support Vector Machines. Thank you for your feedback.",
      "Dentist appointment reminder: Your routine teeth cleaning is scheduled for Friday, October 14th at 2:30 PM with Dr. Watson.",
      "Project status update: The database migration completed without downtime. Latency has decreased by 25% across all endpoints.",
      "Hey everyone, we are organizing a team board games night next Wednesday after work. Vote on the poll to pick your favorite pizza toppings!",
      "Google Calendar invite: 1:1 Bi-weekly catchup with manager on Zoom. Agenda items include career progression and Q3 goals.",
      "Hi David, attached is the revised contract with the vendor adjustments discussed during yesterday's meeting. Let me know if you have questions.",
      "Slack notification: Jessica mentioned you in #general: 'Can you take a look at the design mockup when you get a chance?'",
      "Your monthly utility bill is ready. Total amount of $78.40 will be auto-debited on the 15th from your checking account.",
      "Library notice: The book 'Pattern Recognition and Machine Learning' you requested is now ready for pickup at the front desk.",
      "Hey buddy, thanks for sharing the conference slides! The talk on transformer attention mechanisms was super insightful.",
      "HR Announcement: Open enrollment for health and dental insurance benefits begins next Monday. Please review the employee handbook.",
      "Code review requested: Refactored authentication interceptor and added token refresh logic. Please review when convenient.",
      "Weekly newsletter from Kotlin Weekly: What's new in Kotlin 2.0, Jetpack Compose performance tips, and community tutorials.",
      "Hi team, reminder that the office will be closed on Friday for the public holiday. Have a wonderful long weekend with your families!"
    )

    for (text in spamSeeds) {
      train(text, isSpam = true)
    }
    for (text in hamSeeds) {
      train(text, isSpam = false)
    }
  }
}
