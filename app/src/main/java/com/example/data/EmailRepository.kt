package com.example.data

import android.content.Context
import com.example.ml.ClassificationResult
import com.example.ml.ModelMetrics
import com.example.ml.NaiveBayesClassifier
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EmailRepository(
  private val emailDao: EmailDao,
  private val classifier: NaiveBayesClassifier,
  private val notificationHelper: NotificationHelper
) {

  val inboxEmails: Flow<List<EmailEntity>> = emailDao.getEmailsByFolder("INBOX")
  val spamEmails: Flow<List<EmailEntity>> = emailDao.getEmailsByFolder("SPAM")
  val sentEmails: Flow<List<EmailEntity>> = emailDao.getEmailsByFolder("SENT")
  val trashEmails: Flow<List<EmailEntity>> = emailDao.getEmailsByFolder("TRASH")
  val allEmails: Flow<List<EmailEntity>> = emailDao.getAllEmails()

  val spamCount: Flow<Int> = emailDao.getSpamCount()
  val hamCount: Flow<Int> = emailDao.getHamCount()
  val totalCount: Flow<Int> = emailDao.getTotalCount()
  val unreadInboxCount: Flow<Int> = emailDao.getUnreadInboxCount()
  val unreadSpamCount: Flow<Int> = emailDao.getUnreadSpamCount()

  fun getEmailById(id: Long): Flow<EmailEntity?> = emailDao.getEmailById(id)

  fun getModelMetrics(): ModelMetrics = classifier.getMetrics()

  fun testPredict(subject: String, body: String): ClassificationResult {
    return classifier.classify(subject, body)
  }

  suspend fun receiveIncomingEmail(
    senderName: String,
    senderEmail: String,
    subject: String,
    body: String,
    triggerNotification: Boolean = true
  ): EmailEntity = withContext(Dispatchers.IO) {
    val result = classifier.classify(subject, body)
    val triggerWordsString = result.topTriggerWords.joinToString(", ") { it.word }

    val email = EmailEntity(
      senderName = senderName,
      senderEmail = senderEmail,
      subject = subject,
      body = body,
      timestamp = System.currentTimeMillis(),
      isSpam = result.isSpam,
      spamProbability = result.spamProbability,
      topTriggerWords = triggerWordsString,
      folder = if (result.isSpam) "SPAM" else "INBOX",
      isRead = false,
      isStarred = false,
      userFeedback = null
    )

    val id = emailDao.insertEmail(email)
    val savedEmail = email.copy(id = id)

    if (triggerNotification) {
      notificationHelper.showEmailNotification(savedEmail)
    }

    savedEmail
  }

  suspend fun sendOutgoingEmail(
    senderName: String,
    senderEmail: String,
    recipientEmail: String,
    subject: String,
    body: String
  ): EmailEntity = withContext(Dispatchers.IO) {
    val result = classifier.classify(subject, body)
    val triggerWordsString = result.topTriggerWords.joinToString(", ") { it.word }

    val email = EmailEntity(
      senderName = "$senderName → $recipientEmail",
      senderEmail = senderEmail,
      subject = subject,
      body = body,
      timestamp = System.currentTimeMillis(),
      isSpam = result.isSpam,
      spamProbability = result.spamProbability,
      topTriggerWords = triggerWordsString,
      folder = "SENT",
      isRead = true,
      isStarred = false,
      userFeedback = null
    )

    val id = emailDao.insertEmail(email)
    val savedEmail = email.copy(id = id)

    // Notify user of successful real-time mail dispatch
    notificationHelper.showDispatchNotification(
      title = "✉️ Sent: $subject",
      message = "Dispatched in real-time via Smail to $recipientEmail",
      targetFolder = "SENT"
    )

    // If sent to self or loopback address, deliver in real-time to incoming inbox!
    if (recipientEmail.equals(senderEmail, ignoreCase = true) || recipientEmail.contains("smail", ignoreCase = true)) {
      receiveIncomingEmail(
        senderName = senderName,
        senderEmail = senderEmail,
        subject = subject,
        body = body,
        triggerNotification = true
      )
    }

    savedEmail
  }

  suspend fun markAsSpam(email: EmailEntity) = withContext(Dispatchers.IO) {
    // Retrain Naive Bayes incrementally with user correction
    classifier.train("${email.subject} ${email.body}", isSpam = true)

    val updated = email.copy(
      isSpam = true,
      folder = "SPAM",
      userFeedback = "MANUAL_SPAM"
    )
    emailDao.updateEmail(updated)
  }

  suspend fun markAsNotSpam(email: EmailEntity) = withContext(Dispatchers.IO) {
    // Retrain Naive Bayes incrementally with user correction
    classifier.train("${email.subject} ${email.body}", isSpam = false)

    val updated = email.copy(
      isSpam = false,
      folder = "INBOX",
      userFeedback = "MANUAL_HAM"
    )
    emailDao.updateEmail(updated)
  }

  suspend fun markAsRead(id: Long) = withContext(Dispatchers.IO) {
    emailDao.markAsRead(id)
  }

  suspend fun toggleStar(id: Long) = withContext(Dispatchers.IO) {
    emailDao.toggleStar(id)
  }

  suspend fun moveToTrash(id: Long) = withContext(Dispatchers.IO) {
    emailDao.moveToTrash(id)
  }

  suspend fun deleteEmail(id: Long) = withContext(Dispatchers.IO) {
    emailDao.deleteEmail(id)
  }

  suspend fun emptyTrash() = withContext(Dispatchers.IO) {
    emailDao.emptyTrash()
  }

  suspend fun resetModelAndSeedData() = withContext(Dispatchers.IO) {
    classifier.reset()
    seedInitialEmails(force = true)
  }

  suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
    val count = emailDao.getDirectCount()
    if (count == 0) {
      seedInitialEmails(force = false)
    }
  }

  private suspend fun seedInitialEmails(force: Boolean = false) {
    val now = System.currentTimeMillis()
    val hour = 3600_000L

    val seedEmails = listOf(
      // Legitimate inbox messages
      Triple(
        "Sarah Lin",
        "sarah.lin@company.org",
        "Q3 Project Roadmap & Sprint Planning" to
            "Hi everyone, attached are the discussion points for tomorrow's architecture sync. Please review the database schema updates before 10:00 AM."
      ),
      Triple(
        "Marcus Vance",
        "marcus@techpress.io",
        "Coffee catchup this Friday?" to
            "Hey! Long time no see. Are you free for a quick espresso near the innovation hub this Friday around 3 PM? Let me know!"
      ),
      Triple(
        "GitHub Notifications",
        "notifications@github.com",
        "[bayes-ml] Pull request #14 merged: Add Laplace smoothing" to
            "Pull request #14 'Add Laplace smoothing to multinomial classifier' was merged into main. Automated test suite passed."
      ),
      Triple(
        "Elena Rostova",
        "elena.rostova@designlab.net",
        "Design assets: Brand guide & typography spec v2.1" to
            "Hi team, the final high-resolution mobile UI mockups and component color variables have been uploaded to our Figma workspace."
      ),
      Triple(
        "Stripe Billing",
        "receipts@stripe.com",
        "Your monthly API subscription receipt" to
            "Thank you for your payment of $49.00 for the Cloud Analytics Pro tier. Your invoice #STR-8921 is available for download."
      ),

      // Classic spam emails caught by Naïve Bayes
      Triple(
        "Global Lottery Rewards",
        "claim-dept@fast-jackpot-winner.xyz",
        "URGENT CLAIM: You won $1,500,000 cash prize bonus!!!" to
            "CONGRATULATIONS LUCKY WINNER! Your email address was selected for our $1,500,000 lottery cash payout! Click http://claim-winner.xyz immediately to verify your bank details and claim instant wire transfer."
      ),
      Triple(
        "Security Alert Center",
        "noreply-paypal-auth@suspicious-portal.biz",
        "Urgent: Your account is suspended due to unauthorized login" to
            "Attention user! We detected suspicious login attempts from unknown location. Your account has been temporarily restricted. Verify your password and credit card immediately at http://login-verify-auth.biz or risk termination."
      ),
      Triple(
        "Crypto Whale Club",
        "giveaway@btc-double-instant.club",
        "Double your Bitcoin in 60 minutes - 200% Guarantee!" to
            "Exclusive promotional giveaway! Send between 0.05 BTC and 2 BTC to our automated smart contract and receive 2x back instantly. Zero risk, limited slots remaining!"
      ),
      Triple(
        "Dr. Mark Herbal Labs",
        "offers@instant-miracle-cure.online",
        "Doctors hate this secret pill: Lose 25 lbs in 5 days with zero diet" to
            "Miracle natural fat burning formula backed by secret research! 100% natural, no exercise needed. Buy 2 bottles get 3 free with urgent 75% discount coupon."
      )
    )

    val entities = seedEmails.mapIndexed { index, (sender, email, content) ->
      val (subject, body) = content
      val classification = classifier.classify(subject, body)
      val triggers = classification.topTriggerWords.joinToString(", ") { it.word }

      EmailEntity(
        senderName = sender,
        senderEmail = email,
        subject = subject,
        body = body,
        timestamp = now - ((index + 1) * hour * 2),
        isSpam = classification.isSpam,
        spamProbability = classification.spamProbability,
        topTriggerWords = triggers,
        folder = if (classification.isSpam) "SPAM" else "INBOX",
        isRead = index > 1,
        isStarred = index == 0,
        userFeedback = null
      )
    }

    emailDao.insertEmails(entities)
  }

  companion object {
    @Volatile
    private var INSTANCE: EmailRepository? = null

    fun getInstance(context: Context): EmailRepository {
      return INSTANCE ?: synchronized(this) {
        val db = AppDatabase.getDatabase(context)
        val classifier = NaiveBayesClassifier()
        val notificationHelper = NotificationHelper(context)
        val instance = EmailRepository(db.emailDao(), classifier, notificationHelper)
        INSTANCE = instance
        instance
      }
    }
  }
}
