package com.example.ml

import kotlin.math.exp
import kotlin.math.ln

data class ClassificationResult(
  val isSpam: Boolean,
  val spamProbability: Float, // 0.0 to 1.0
  val hamProbability: Float,
  val logOddsRatio: Double,
  val topTriggerWords: List<WordImpact>,
  val extractedTokensCount: Int,
  val isOriginal: Boolean = !isSpam,
  val authenticityLabel: String = if (isSpam) "FAKE / SPAM DETECTED" else "ORIGINAL & SAFE EMAIL",
  val detectionDetails: String = if (isSpam) "Flagged by AI security analysis as fake or suspicious" else "Verified by AI security analysis as genuine and safe"
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

    val DISPOSABLE_OR_FAKE_DOMAINS = setOf(
      "fake.com", "fakemail.com", "tempmail.com", "temp-mail.org", "10minutemail.com",
      "guerrillamail.com", "trashmail.com", "yopmail.com", "mailinator.com", "throwawaymail.com",
      "dispostable.com", "sharklasers.com", "getairmail.com", "fakemail.net", "test.com",
      "spambox.us", "mytemp.email", "nada.ltd", "mohmal.com", "crazymailing.com", "fakemail.org"
    )

    val HIGH_RISK_TLDS = setOf(
      "tk", "ml", "ga", "cf", "gq", "xyz", "top", "click", "buzz", "club",
      "work", "loan", "cam", "stream", "win", "bid", "download", "racing",
      "accountant", "date", "faith", "party", "trade", "webcam"
    )

    val SUSPICIOUS_EMAIL_WORDS = listOf(
      "fake", "scam", "spoof", "phish", "hacker", "urgent-verify", "claim-prize",
      "prize-pool", "winner", "reward", "lottery", "security-update", "acc-suspended"
    )

    val BRAND_VERIFICATIONS = listOf(
      Pair(listOf("google", "gmail", "youtube", "android"), setOf("google.com", "gmail.com", "youtube.com", "android.com")),
      Pair(listOf("paypal"), setOf("paypal.com")),
      Pair(listOf("amazon", "prime"), setOf("amazon.com", "amazon.co.uk", "amazon.in", "amazon.de", "amazon.fr")),
      Pair(listOf("apple", "icloud"), setOf("apple.com", "icloud.com")),
      Pair(listOf("microsoft", "outlook", "hotmail", "office365"), setOf("microsoft.com", "outlook.com", "hotmail.com")),
      Pair(listOf("netflix"), setOf("netflix.com")),
      Pair(listOf("facebook", "meta", "instagram", "whatsapp"), setOf("facebookmail.com", "meta.com", "instagram.com")),
      Pair(listOf("chase", "wells fargo", "citibank", "bank of america"), setOf("chase.com", "wellsfargo.com", "citi.com", "bankofamerica.com")),
      Pair(listOf("fedex", "dhl", "ups"), setOf("fedex.com", "dhl.com", "ups.com")),
      Pair(listOf("irs", "tax refund"), setOf("irs.gov"))
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
   * Evaluates sender authenticity and domain signals to detect fake, spoofed, or phishing addresses
   */
  private fun analyzeSenderAuthenticity(
    senderName: String,
    senderEmail: String,
    subject: String,
    body: String
  ): AuthenticityEvaluation {
    val cleanEmail = senderEmail.trim().lowercase()
    val cleanName = senderName.trim().lowercase()
    val cleanSubj = subject.trim().lowercase()
    val cleanBody = body.trim().lowercase()

    val reasons = mutableListOf<String>()
    var fakeScore = 0f

    // 1. Check basic email syntax
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    if (cleanEmail.isNotBlank() && !emailRegex.matches(cleanEmail)) {
      fakeScore += 0.85f
      reasons.add("Invalid email format or syntax")
    }

    val domain = if ("@" in cleanEmail) cleanEmail.substringAfter("@") else ""
    val localPart = if ("@" in cleanEmail) cleanEmail.substringBefore("@") else cleanEmail
    val tld = if ("." in domain) domain.substringAfterLast(".") else ""

    // 2. Check disposable / fake email domains
    if (domain in DISPOSABLE_OR_FAKE_DOMAINS ||
      domain.contains("temp-mail") ||
      domain.contains("fakemail") ||
      domain.contains("throwaway") ||
      domain.contains("dispos")
    ) {
      fakeScore += 0.95f
      reasons.add("Disposable or fake email service domain ($domain)")
    }

    // 3. Check for fake / scam keywords in address
    for (kw in SUSPICIOUS_EMAIL_WORDS) {
      if (cleanEmail.contains(kw)) {
        fakeScore += 0.80f
        reasons.add("Address contains high-risk fraud keyword '$kw'")
        break
      }
    }

    // 4. Check high-risk TLDs commonly used by spammers
    if (tld in HIGH_RISK_TLDS) {
      fakeScore += 0.50f
      reasons.add("High-risk domain extension (.$tld)")
    }

    // 5. Brand Impersonation / Spoofing Check
    // Spoofing occurs when the sender's display name or email address impersonates a known brand,
    // NOT merely because the subject mentions a brand (e.g. discussing Google Docs or Netflix shows).
    var isVerifiedAuthenticBrand = false
    for ((brandKeywords, verifiedDomains) in BRAND_VERIFICATIONS) {
      val senderClaimsBrand = brandKeywords.any { kw ->
        cleanName.contains(kw) || localPart.contains(kw)
      }
      if (senderClaimsBrand) {
        val domainMatches = verifiedDomains.any { domain.endsWith(it) }
        val primaryBrand = brandKeywords.first().replaceFirstChar { it.uppercase() }
        if (!domainMatches && domain.isNotBlank()) {
          fakeScore += 0.95f
          reasons.add("Brand Spoofing: Sender claims to be $primaryBrand but email domain is @$domain")
          break
        } else if (domainMatches) {
          isVerifiedAuthenticBrand = true
          reasons.add("Verified Authentic: Official domain for $primaryBrand ($domain)")
        }
      }
    }

    // 6. Look for urgent phishing and credential harvesting phrases
    val phishingPhrases = listOf(
      "verify your account", "account suspended", "suspended immediately", "confirm password",
      "unauthorized sign in", "unauthorized access", "unusual activity", "within 24 hours",
      "enter your pin", "social security", "card details", "reset password immediately",
      "wire transfer", "cash prize", "crypto giveaway", "won lottery", "lottery bonus"
    )
    val fullContent = "$cleanSubj $cleanBody"
    val foundPhrases = phishingPhrases.filter { fullContent.contains(it) }
    if (foundPhrases.isNotEmpty()) {
      fakeScore += (foundPhrases.size * 0.35f).coerceAtMost(0.90f)
      reasons.add("Phishing triggers: " + foundPhrases.take(2).joinToString(", "))
    }

    val isFakeOrPhishing = fakeScore >= 0.60f
    return AuthenticityEvaluation(
      isFake = isFakeOrPhishing,
      fakeScore = fakeScore.coerceIn(0f, 1f),
      isVerifiedAuthenticBrand = isVerifiedAuthenticBrand && foundPhrases.isEmpty(),
      reasons = reasons
    )
  }

  private data class AuthenticityEvaluation(
    val isFake: Boolean,
    val fakeScore: Float,
    val isVerifiedAuthenticBrand: Boolean = false,
    val reasons: List<String>
  )

  /**
   * Predict spam probability and authenticity using Naïve Bayes + Authenticity AI Heuristics
   */
  fun classify(
    senderName: String = "",
    senderEmail: String = "",
    subject: String,
    body: String
  ): ClassificationResult {
    val authEval = analyzeSenderAuthenticity(senderName, senderEmail, subject, body)

    val syntheticTokens = mutableListOf<String>()
    if (authEval.isFake) {
      syntheticTokens.add("__feature_fake_sender_domain__")
      syntheticTokens.add("__feature_brand_spoofing__")
    }

    // Include sender details, subject weighted, body, and synthetic tokens
    val fullText = "$senderName $senderEmail $subject $subject $body " + syntheticTokens.joinToString(" ")
    val tokens = tokenize(fullText) + syntheticTokens

    val totalDocs = spamDocsCount + hamDocsCount
    val vocabSize = vocabulary.size.coerceAtLeast(1)

    // Prior probabilities
    val priorSpam = (spamDocsCount + laplaceSmoothingAlpha) / (totalDocs + 2 * laplaceSmoothingAlpha)
    val priorHam = (hamDocsCount + laplaceSmoothingAlpha) / (totalDocs + 2 * laplaceSmoothingAlpha)

    var logProbSpam = ln(priorSpam)
    var logProbHam = ln(priorHam)

    val spamDenominator = totalSpamTokens + (laplaceSmoothingAlpha * vocabSize)
    val hamDenominator = totalHamTokens + (laplaceSmoothingAlpha * vocabSize)

    val wordImpacts = mutableListOf<WordImpact>()
    val seenTokens = mutableSetOf<String>()

    for (token in tokens) {
      val spamCount = spamWordCounts[token] ?: 0
      val hamCount = hamWordCounts[token] ?: 0

      // In Naive Bayes, skip unseen words with 0 counts in both classes to avoid bias from denominator difference
      if (spamCount == 0 && hamCount == 0) {
        continue
      }

      val pTokenGivenSpam = (spamCount + laplaceSmoothingAlpha) / spamDenominator
      val pTokenGivenHam = (hamCount + laplaceSmoothingAlpha) / hamDenominator

      logProbSpam += ln(pTokenGivenSpam)
      logProbHam += ln(pTokenGivenHam)

      if (token !in seenTokens) {
        seenTokens.add(token)
        val impact = ln(pTokenGivenSpam) - ln(pTokenGivenHam)
        val displayName = when (token) {
          "__feature_link__" -> "[Web Link]"
          "__feature_currency__" -> "[$ Currency]"
          "__feature_multiple_punctuation__" -> "[!!! Symbols]"
          "__feature_caps_shouting__" -> "[SHOUTING ALL-CAPS]"
          "__feature_fake_sender_domain__" -> "[Fake/Disposable Sender]"
          "__feature_brand_spoofing__" -> "[Brand Impersonation]"
          else -> token
        }
        wordImpacts.add(WordImpact(displayName, impact, pTokenGivenSpam, pTokenGivenHam))
      }
    }

    val delta = logProbSpam - logProbHam
    var bayesianSpamProb = if (delta > 50) {
      0.9999
    } else if (delta < -50) {
      0.0001
    } else {
      1.0 / (1.0 + exp(-delta))
    }

    // Fuse Bayesian Score with Authenticity Intelligence
    val finalSpamProb: Float
    val isSpam: Boolean
    val authenticityLabel: String
    val detectionDetails: String

    if (authEval.isFake) {
      // Strong fake or spoofed sender: enforce minimum 0.92+ spam probability
      finalSpamProb = (bayesianSpamProb.toFloat().coerceAtLeast(0.92f) + (authEval.fakeScore * 0.07f)).coerceAtMost(0.999f)
      isSpam = true
      authenticityLabel = "SPAM DETECTED"
      detectionDetails = "Why is this in Spam? " + authEval.reasons.joinToString("; ")
    } else if (authEval.isVerifiedAuthenticBrand) {
      // Verified legitimate sender domain (e.g. accounts.google.com, paypal.com)
      finalSpamProb = (bayesianSpamProb.toFloat() * 0.15f).coerceIn(0.001f, 0.20f)
      isSpam = false
      authenticityLabel = "SAFE EMAIL"
      detectionDetails = "Verified authentic official domain: " + authEval.reasons.joinToString("; ")
    } else {
      // Clean sender authenticity
      finalSpamProb = bayesianSpamProb.toFloat().coerceIn(0.001f, 0.999f)
      isSpam = finalSpamProb >= spamThreshold
      if (isSpam) {
        authenticityLabel = "SPAM DETECTED"
        detectionDetails = "Why is this in Spam? Message matches high-frequency spam patterns."
      } else {
        authenticityLabel = "SAFE EMAIL"
        detectionDetails = "Verified Safe: Passed automated spam security checks."
      }
    }

    val hamProb = 1.0f - finalSpamProb

    // Sort word impacts by magnitude of contribution
    val sortedImpacts = wordImpacts.sortedByDescending { if (isSpam) it.impactScore else -it.impactScore }
      .take(7)

    return ClassificationResult(
      isSpam = isSpam,
      spamProbability = finalSpamProb,
      hamProbability = hamProb,
      logOddsRatio = delta,
      topTriggerWords = sortedImpacts,
      extractedTokensCount = tokens.size,
      isOriginal = !isSpam,
      authenticityLabel = authenticityLabel,
      detectionDetails = detectionDetails
    )
  }

  /**
   * Overload for backward compatibility
   */
  fun classify(subject: String, body: String): ClassificationResult {
    return classify("", "", subject, body)
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

      val display = when (token) {
        "__feature_link__" -> "Links/URLs"
        "__feature_currency__" -> "$/Currency"
        "__feature_caps_shouting__" -> "ALL-CAPS"
        "__feature_multiple_punctuation__" -> "!!!"
        "__feature_fake_sender_domain__" -> "Fake Sender"
        "__feature_brand_spoofing__" -> "Brand Spoofing"
        else -> token
      }
      display to ratio
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
