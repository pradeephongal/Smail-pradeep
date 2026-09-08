package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EmailEntity
import com.example.data.EmailRepository
import com.example.ml.ClassificationResult
import com.example.ml.ModelMetrics
import com.example.security.AppLockManager
import com.example.security.UserAccountManager
import com.example.ui.components.SpamAlertInfo
import com.example.util.AlertSoundHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
  INBOX,
  SENT,
  SPAM,
  DASHBOARD,
  INSPECTOR
}

enum class SimulationPreset(val label: String, val senderName: String, val senderEmail: String, val subject: String, val body: String) {
  LOTTERY_SCAM(
    "Lottery Jackpot Scam",
    "Global Cash Bureau",
    "claims@international-lottery-award.org",
    "URGENT: $2,000,000 Cash Prize Notification for Immediate Claim!",
    "Congratulations to our lucky winner! Your email was drawn in the European Mega Cash Draw for $2,000,000. Click here to verify your bank details urgently and release wire transfer."
  ),
  PHISHING_BANK(
    "Banking Phishing Attack",
    "Security Department",
    "security-alert@verify-chase-auth.net",
    "Action Required: Your online banking account has been suspended",
    "Dear customer, unusual debit attempts were detected. Please click http://verify-secure-auth.net/login immediately to update your password, social security, and debit card PIN to prevent permanent termination."
  ),
  CRYPTO_GIVEAWAY(
    "Crypto Multiplier Scam",
    "Elon Musk Official",
    "btc-promotions@giveaway-airdrop.xyz",
    "Double your Ethereum & Bitcoin in 1 Hour - Official Giveaway!",
    "To celebrate our space launch, we are giving away 5,000 BTC. Send 0.1 BTC to receive 0.2 BTC bonus instantly. Free money guarantee for first 100 participants!"
  ),
  WORK_SYNC(
    "Work Meeting Notes",
    "Rachel Green",
    "rachel.green@company.com",
    "Sprint retro notes and next week's architecture sync",
    "Hi team, great work during this sprint. The database indexing changes are live in staging. Please check the sprint board and add your feedback before our 2 PM sync tomorrow."
  ),
  PERSONAL_NOTE(
    "Personal Catchup",
    "Liam Miller",
    "liam.miller@gmail.com",
    "Dinner this weekend with the family?",
    "Hey! Are you free this Saturday around 7 PM? We're hosting a barbecue in the backyard. Let me know if you can make it, would love to catch up!"
  ),
  INVOICE_RECEIPT(
    "Vendor Monthly Invoice",
    "Billing Services",
    "invoices@cloudhost-services.io",
    "Monthly Cloud Server Invoice #8491 attached",
    "Hello, your monthly invoice for compute instance usage is now ready for review. Payment of $120.00 will be charged to the card ending in 4102 on Monday. Thank you for your business."
  )
}

class EmailViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = EmailRepository.getInstance(application)
  private val appLockManager = AppLockManager(application)
  private val userAccountManager = UserAccountManager(application)

  companion object {
    const val APP_CREATOR = "@PRADEEP"
  }

  // User Account State
  private val _isLoggedIn = MutableStateFlow(userAccountManager.isLoggedIn)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _userEmail = MutableStateFlow(userAccountManager.userEmail)
  val userEmail: StateFlow<String> = _userEmail.asStateFlow()

  private val _userName = MutableStateFlow(userAccountManager.userName)
  val userName: StateFlow<String> = _userName.asStateFlow()

  // App Lock State (Enforce security check on launch)
  private val _isAppLocked = MutableStateFlow(true)
  val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

  private val _isAppLockEnabled = MutableStateFlow(appLockManager.isAppLockEnabled)
  val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

  private val _currentPin = MutableStateFlow(appLockManager.pin)
  val currentPin: StateFlow<String> = _currentPin.asStateFlow()

  // Tab & Navigation State
  private val _currentTab = MutableStateFlow(AppTab.INBOX)
  val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

  private val _selectedEmail = MutableStateFlow<EmailEntity?>(null)
  val selectedEmail: StateFlow<EmailEntity?> = _selectedEmail.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  // In-app Red Spam Warning Alert
  private val _latestSpamAlert = MutableStateFlow<SpamAlertInfo?>(null)
  val latestSpamAlert: StateFlow<SpamAlertInfo?> = _latestSpamAlert.asStateFlow()

  private val _isAutoSimulatorActive = MutableStateFlow(false)
  val isAutoSimulatorActive: StateFlow<Boolean> = _isAutoSimulatorActive.asStateFlow()

  // Real-time interactive playground state
  private val _playgroundSubject = MutableStateFlow("URGENT: Verify your account to claim $50,000 cash prize!")
  val playgroundSubject: StateFlow<String> = _playgroundSubject.asStateFlow()

  private val _playgroundBody = MutableStateFlow("Click here immediately to confirm your password and receive wire transfer now.")
  val playgroundBody: StateFlow<String> = _playgroundBody.asStateFlow()

  private val _playgroundResult = MutableStateFlow<ClassificationResult?>(null)
  val playgroundResult: StateFlow<ClassificationResult?> = _playgroundResult.asStateFlow()

  // Model Metrics Flow
  private val _modelMetrics = MutableStateFlow(repository.getModelMetrics())
  val modelMetrics: StateFlow<ModelMetrics> = _modelMetrics.asStateFlow()

  val inboxEmails = combine(repository.inboxEmails, _searchQuery) { list, query ->
    if (query.isBlank()) list
    else list.filter {
      it.subject.contains(query, ignoreCase = true) ||
          it.senderName.contains(query, ignoreCase = true) ||
          it.body.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val spamEmails = combine(repository.spamEmails, _searchQuery) { list, query ->
    if (query.isBlank()) list
    else list.filter {
      it.subject.contains(query, ignoreCase = true) ||
          it.senderName.contains(query, ignoreCase = true) ||
          it.body.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val sentEmails = combine(repository.sentEmails, _searchQuery) { list, query ->
    if (query.isBlank()) list
    else list.filter {
      it.subject.contains(query, ignoreCase = true) ||
          it.senderName.contains(query, ignoreCase = true) ||
          it.body.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val totalCount = repository.totalCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
  val spamCount = repository.spamCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
  val hamCount = repository.hamCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
  val unreadInbox = repository.unreadInboxCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
  val unreadSpam = repository.unreadSpamCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private var autoSimJob: Job? = null

  init {
    viewModelScope.launch {
      repository.initializeIfEmpty()
      runPlaygroundPrediction()
      refreshMetrics()
    }
  }

  // Account methods
  fun login(email: String, name: String) {
    userAccountManager.login(email, name)
    _userEmail.value = userAccountManager.userEmail
    _userName.value = userAccountManager.userName
    _isLoggedIn.value = true
    _isAppLocked.value = false
    _statusMessage.value = "Signed in as $email"
  }

  fun logout() {
    userAccountManager.logout()
    _isLoggedIn.value = false
    _isAppLocked.value = true
    _statusMessage.value = "Signed out of Smail"
  }

  fun unlockSuccess() {
    _isAppLocked.value = false
  }

  // App Lock methods
  fun verifyPin(pin: String): Boolean {
    val isValid = appLockManager.verifyPin(pin)
    if (isValid) {
      _isAppLocked.value = false
    }
    return isValid
  }

  fun lockApp() {
    _isAppLocked.value = true
  }

  fun toggleAppLock(enabled: Boolean) {
    appLockManager.isAppLockEnabled = enabled
    _isAppLockEnabled.value = enabled
    _statusMessage.value = if (enabled) "App Lock enabled with PIN ${_currentPin.value}" else "App Lock disabled"
  }

  fun changePin(newPin: String) {
    appLockManager.pin = newPin
    _currentPin.value = newPin
    _statusMessage.value = "Security PIN updated"
  }

  fun playWarningAlertSound() {
    AlertSoundHelper.playSpamWarningAlert(getApplication())
  }

  fun dismissSpamAlert() {
    _latestSpamAlert.value = null
  }

  fun setTab(tab: AppTab) {
    _currentTab.value = tab
    _selectedEmail.value = null
  }

  fun selectEmail(email: EmailEntity?) {
    _selectedEmail.value = email
    if (email != null && !email.isRead) {
      viewModelScope.launch {
        repository.markAsRead(email.id)
      }
    }
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun setPlaygroundSubject(subject: String) {
    _playgroundSubject.value = subject
    runPlaygroundPrediction()
  }

  fun setPlaygroundBody(body: String) {
    _playgroundBody.value = body
    runPlaygroundPrediction()
  }

  fun classifyDraft(subject: String, body: String): ClassificationResult {
    return repository.testPredict(subject, body)
  }

  fun sendEmail(recipientEmail: String, subject: String, body: String) {
    viewModelScope.launch {
      _statusMessage.value = "Sending email in real-time via Smail..."
      val sent = repository.sendOutgoingEmail(
        senderName = _userName.value,
        senderEmail = _userEmail.value,
        recipientEmail = recipientEmail,
        subject = subject,
        body = body
      )
      refreshMetrics()
      _statusMessage.value = "✉️ Dispatched in real-time to $recipientEmail"
    }
  }

  fun runPlaygroundPrediction() {
    val result = repository.testPredict(_playgroundSubject.value, _playgroundBody.value)
    _playgroundResult.value = result
  }

  fun refreshMetrics() {
    _modelMetrics.value = repository.getModelMetrics()
  }

  fun simulateIncomingEmail(preset: SimulationPreset) {
    viewModelScope.launch {
      val email = repository.receiveIncomingEmail(
        senderName = preset.senderName,
        senderEmail = preset.senderEmail,
        subject = preset.subject,
        body = preset.body,
        triggerNotification = true
      )
      refreshMetrics()

      if (email.isSpam) {
        val spamScore = (email.spamProbability * 100).toInt()
        AlertSoundHelper.playSpamWarningAlert(getApplication())
        _latestSpamAlert.value = SpamAlertInfo(
          subject = email.subject,
          senderName = email.senderName,
          spamScore = spamScore,
          triggerWords = email.topTriggerWords
        )
      } else {
        _statusMessage.value = "✉️ Clean email received in Inbox: '${email.subject.take(30)}...'"
      }
    }
  }

  fun simulateCustomEmail(senderName: String, senderEmail: String, subject: String, body: String) {
    viewModelScope.launch {
      val email = repository.receiveIncomingEmail(
        senderName = senderName.ifBlank { "Unknown Sender" },
        senderEmail = senderEmail.ifBlank { "sender@example.com" },
        subject = subject.ifBlank { "No Subject" },
        body = body,
        triggerNotification = true
      )
      refreshMetrics()

      if (email.isSpam) {
        val spamScore = (email.spamProbability * 100).toInt()
        AlertSoundHelper.playSpamWarningAlert(getApplication())
        _latestSpamAlert.value = SpamAlertInfo(
          subject = email.subject,
          senderName = email.senderName,
          spamScore = spamScore,
          triggerWords = email.topTriggerWords
        )
      } else {
        _statusMessage.value = "✉️ Normal email delivered to Inbox (${((1f - email.spamProbability) * 100).toInt()}% clean)"
      }
    }
  }

  fun markAsSpam(email: EmailEntity) {
    viewModelScope.launch {
      repository.markAsSpam(email)
      refreshMetrics()
      _statusMessage.value = "Trained Naïve Bayes: Email marked as Spam & quarantined"
      if (_selectedEmail.value?.id == email.id) {
        _selectedEmail.value = email.copy(isSpam = true, folder = "SPAM", userFeedback = "MANUAL_SPAM")
      }
    }
  }

  fun markAsNotSpam(email: EmailEntity) {
    viewModelScope.launch {
      repository.markAsNotSpam(email)
      refreshMetrics()
      _statusMessage.value = "Trained Naïve Bayes: Email marked as Ham & restored to Inbox"
      if (_selectedEmail.value?.id == email.id) {
        _selectedEmail.value = email.copy(isSpam = false, folder = "INBOX", userFeedback = "MANUAL_HAM")
      }
    }
  }

  fun toggleStar(email: EmailEntity) {
    viewModelScope.launch {
      repository.toggleStar(email.id)
      if (_selectedEmail.value?.id == email.id) {
        _selectedEmail.value = email.copy(isStarred = !email.isStarred)
      }
    }
  }

  fun deleteEmail(email: EmailEntity) {
    viewModelScope.launch {
      repository.deleteEmail(email.id)
      _statusMessage.value = "Email deleted"
      if (_selectedEmail.value?.id == email.id) {
        _selectedEmail.value = null
      }
    }
  }

  fun toggleAutoSimulator() {
    val newState = !_isAutoSimulatorActive.value
    _isAutoSimulatorActive.value = newState
    if (newState) {
      startAutoSimulator()
      _statusMessage.value = "Real-time incoming mail stream started"
    } else {
      autoSimJob?.cancel()
      _statusMessage.value = "Incoming stream paused"
    }
  }

  private fun startAutoSimulator() {
    autoSimJob?.cancel()
    autoSimJob = viewModelScope.launch {
      val presets = SimulationPreset.entries
      var index = 0
      while (_isAutoSimulatorActive.value) {
        delay(14000)
        val preset = presets[index % presets.size]
        index++
        val email = repository.receiveIncomingEmail(
          senderName = preset.senderName,
          senderEmail = preset.senderEmail,
          subject = preset.subject,
          body = preset.body,
          triggerNotification = true
        )
        refreshMetrics()
        if (email.isSpam) {
          AlertSoundHelper.playSpamWarningAlert(getApplication())
          _latestSpamAlert.value = SpamAlertInfo(
            subject = email.subject,
            senderName = email.senderName,
            spamScore = (email.spamProbability * 100).toInt(),
            triggerWords = email.topTriggerWords
          )
        }
      }
    }
  }

  fun resetModel() {
    viewModelScope.launch {
      repository.resetModelAndSeedData()
      refreshMetrics()
      runPlaygroundPrediction()
      _statusMessage.value = "Smail Naïve Bayes model and sample emails reset to default corpus"
    }
  }

  override fun onCleared() {
    super.onCleared()
    autoSimJob?.cancel()
  }
}
