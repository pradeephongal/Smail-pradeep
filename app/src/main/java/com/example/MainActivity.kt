package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.security.BiometricAuthHelper
import com.example.ui.AppTab
import com.example.ui.EmailViewModel
import com.example.ui.components.AppFooter
import com.example.ui.components.ComposeEmailDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SimulateIncomingEmailDialog
import com.example.ui.components.SpamWarningBanner
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.ClassifierPlaygroundScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmailDetailScreen
import com.example.ui.screens.EmailListScreen
import com.example.ui.screens.EmailLoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpamRed

class MainActivity : FragmentActivity() {
  private val viewModel: EmailViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Handle intent extras if notification was tapped
    val targetFolder = intent.getStringExtra("TARGET_FOLDER")
    when (targetFolder) {
      "SPAM" -> viewModel.setTab(AppTab.SPAM)
      "SENT" -> viewModel.setTab(AppTab.SENT)
      "INBOX" -> viewModel.setTab(AppTab.INBOX)
    }

    setContent {
      MyApplicationTheme {
        MainApp(
          activity = this,
          viewModel = viewModel,
          onTriggerBiometrics = { triggerBiometricPrompt() }
        )
      }
    }
  }

  fun triggerBiometricPrompt() {
    BiometricAuthHelper.showBiometricPrompt(
      activity = this,
      onSuccess = {
        viewModel.unlockSuccess()
      },
      onError = { _ -> }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
  activity: FragmentActivity,
  viewModel: EmailViewModel,
  onTriggerBiometrics: () -> Unit
) {
  val context = LocalContext.current
  val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
  val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
  val userName by viewModel.userName.collectAsStateWithLifecycle()

  val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
  val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
  val currentPin by viewModel.currentPin.collectAsStateWithLifecycle()

  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedEmail by viewModel.selectedEmail.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
  val latestSpamAlert by viewModel.latestSpamAlert.collectAsStateWithLifecycle()
  val isAutoSimulatorActive by viewModel.isAutoSimulatorActive.collectAsStateWithLifecycle()

  val inboxEmails by viewModel.inboxEmails.collectAsStateWithLifecycle()
  val sentEmails by viewModel.sentEmails.collectAsStateWithLifecycle()
  val spamEmails by viewModel.spamEmails.collectAsStateWithLifecycle()
  val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
  val spamCount by viewModel.spamCount.collectAsStateWithLifecycle()
  val hamCount by viewModel.hamCount.collectAsStateWithLifecycle()
  val unreadInbox by viewModel.unreadInbox.collectAsStateWithLifecycle()
  val unreadSpam by viewModel.unreadSpam.collectAsStateWithLifecycle()

  val playgroundSubject by viewModel.playgroundSubject.collectAsStateWithLifecycle()
  val playgroundBody by viewModel.playgroundBody.collectAsStateWithLifecycle()
  val playgroundResult by viewModel.playgroundResult.collectAsStateWithLifecycle()
  val modelMetrics by viewModel.modelMetrics.collectAsStateWithLifecycle()

  val isBiometricAvailable = remember { BiometricAuthHelper.isBiometricAvailable(context) }

  val snackbarHostState = remember { SnackbarHostState() }
  var showSimulateDialog by remember { mutableStateOf(false) }
  var showComposeDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showPrivacyDialog by remember { mutableStateOf(false) }

  // Notification permission requester for Android 13+
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { _ -> }
  )

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        try {
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } catch (e: Exception) {
          android.util.Log.e("MainActivity", "Notification permission launch failed", e)
        }
      }
    }
    // Launch BiometricPrompt on app start if biometrics are available and app is locked
    if (isLoggedIn && isAppLocked && isBiometricAvailable) {
      onTriggerBiometrics()
    }
  }

  LaunchedEffect(statusMessage) {
    statusMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  // 1. If not logged in, show Email Login screen
  if (!isLoggedIn) {
    EmailLoginScreen(
      onLoginSuccess = { email, name ->
        viewModel.login(email, name)
      },
      onBiometricUnlock = {
        onTriggerBiometrics()
      },
      isBiometricAvailable = isBiometricAvailable
    )
    return
  }

  // 2. If app is locked, enforce BiometricPrompt / PIN authentication
  if (isAppLocked) {
    AppLockScreen(
      onUnlockSuccess = { viewModel.unlockSuccess() },
      verifyPin = { pin -> viewModel.verifyPin(pin) },
      onBiometricClick = { onTriggerBiometrics() },
      isBiometricAvailable = isBiometricAvailable
    )
    return
  }

  // 3. If email is selected, display detail reading screen
  if (selectedEmail != null) {
    EmailDetailScreen(
      email = selectedEmail!!,
      onBack = { viewModel.selectEmail(null) },
      onMarkAsSpam = { viewModel.markAsSpam(selectedEmail!!) },
      onMarkAsNotSpam = { viewModel.markAsNotSpam(selectedEmail!!) },
      onDelete = { viewModel.deleteEmail(selectedEmail!!) },
      onToggleStar = { viewModel.toggleStar(selectedEmail!!) }
    )
    return
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.systemBars,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(34.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Smail",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = userEmail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
              )
            }
          }
        },
        actions = {
          // Stream Toggle Button
          FilledTonalIconButton(
            onClick = { viewModel.toggleAutoSimulator() },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = if (isAutoSimulatorActive) SpamRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.testTag("auto_sim_toggle")
          ) {
            Icon(
              imageVector = if (isAutoSimulatorActive) Icons.Default.Stop else Icons.Default.PlayArrow,
              contentDescription = if (isAutoSimulatorActive) "Stop Live Stream" else "Start Live Stream",
              tint = if (isAutoSimulatorActive) SpamRed else MaterialTheme.colorScheme.primary
            )
          }

          // Privacy Policy
          IconButton(
            onClick = { showPrivacyDialog = true },
            modifier = Modifier.testTag("top_privacy_button")
          ) {
            Icon(Icons.Default.PrivacyTip, contentDescription = "Privacy Policy")
          }

          // Lock Now
          IconButton(
            onClick = { viewModel.lockApp() },
            modifier = Modifier.testTag("top_lock_button")
          ) {
            Icon(Icons.Default.Lock, contentDescription = "Lock App")
          }

          // Settings
          IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier.testTag("top_settings_button")
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface
        ) {
          NavigationBarItem(
            selected = currentTab == AppTab.INBOX,
            onClick = { viewModel.setTab(AppTab.INBOX) },
            icon = {
              BadgedBox(
                badge = {
                  if (unreadInbox > 0) {
                    Badge { Text("$unreadInbox") }
                  }
                }
              ) {
                Icon(
                  imageVector = if (currentTab == AppTab.INBOX) Icons.Filled.Inbox else Icons.Outlined.Inbox,
                  contentDescription = "Inbox"
                )
              }
            },
            label = { Text("Inbox") },
            modifier = Modifier.testTag("tab_inbox")
          )

          NavigationBarItem(
            selected = currentTab == AppTab.SENT,
            onClick = { viewModel.setTab(AppTab.SENT) },
            icon = {
              Icon(
                imageVector = if (currentTab == AppTab.SENT) Icons.Filled.Send else Icons.Outlined.Send,
                contentDescription = "Sent"
              )
            },
            label = { Text("Sent") },
            modifier = Modifier.testTag("tab_sent")
          )

          NavigationBarItem(
            selected = currentTab == AppTab.SPAM,
            onClick = { viewModel.setTab(AppTab.SPAM) },
            icon = {
              BadgedBox(
                badge = {
                  if (unreadSpam > 0) {
                    Badge(containerColor = SpamRed) { Text("$unreadSpam") }
                  }
                }
              ) {
                Icon(
                  imageVector = if (currentTab == AppTab.SPAM) Icons.Filled.Security else Icons.Outlined.Security,
                  contentDescription = "Spam"
                )
              }
            },
            label = { Text("Spam") },
            modifier = Modifier.testTag("tab_spam")
          )

          NavigationBarItem(
            selected = currentTab == AppTab.DASHBOARD,
            onClick = { viewModel.setTab(AppTab.DASHBOARD) },
            icon = {
              Icon(
                imageVector = if (currentTab == AppTab.DASHBOARD) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                contentDescription = "Dashboard"
              )
            },
            label = { Text("Dashboard") },
            modifier = Modifier.testTag("tab_dashboard")
          )

          NavigationBarItem(
            selected = currentTab == AppTab.INSPECTOR,
            onClick = { viewModel.setTab(AppTab.INSPECTOR) },
            icon = {
              Icon(
                imageVector = if (currentTab == AppTab.INSPECTOR) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                contentDescription = "Playground"
              )
            },
            label = { Text("Playground") },
            modifier = Modifier.testTag("tab_playground")
          )
        }

        // Creator name permanently below the app
        AppFooter()
      }
    },
    floatingActionButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FloatingActionButton(
          onClick = { showComposeDialog = true },
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.testTag("compose_fab")
        ) {
          Icon(Icons.Default.Edit, contentDescription = "Compose Real-Time Email")
        }

        ExtendedFloatingActionButton(
          onClick = { showSimulateDialog = true },
          icon = { Icon(Icons.Default.Add, contentDescription = null) },
          text = { Text("Simulate Incoming") },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("simulate_email_fab")
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Prominent In-App Red Warning Banner (shown when spam is intercepted)
      SpamWarningBanner(
        alertInfo = latestSpamAlert,
        onDismiss = { viewModel.dismissSpamAlert() },
        onOpenSpamFolder = {
          viewModel.dismissSpamAlert()
          viewModel.setTab(AppTab.SPAM)
        }
      )

      Box(modifier = Modifier.weight(1f)) {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "tab_transition"
        ) { tab ->
          when (tab) {
            AppTab.INBOX -> {
              EmailListScreen(
                title = "Inbox",
                isSpamFolder = false,
                emails = inboxEmails,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSelectEmail = { viewModel.selectEmail(it) },
                onToggleStar = { viewModel.toggleStar(it) }
              )
            }

            AppTab.SENT -> {
              EmailListScreen(
                title = "Sent Mail",
                isSpamFolder = false,
                emails = sentEmails,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSelectEmail = { viewModel.selectEmail(it) },
                onToggleStar = { viewModel.toggleStar(it) }
              )
            }

            AppTab.SPAM -> {
              EmailListScreen(
                title = "Spam Quarantine",
                isSpamFolder = true,
                emails = spamEmails,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSelectEmail = { viewModel.selectEmail(it) },
                onToggleStar = { viewModel.toggleStar(it) }
              )
            }

            AppTab.DASHBOARD -> {
              DashboardScreen(
                totalCount = totalCount,
                spamCount = spamCount,
                hamCount = hamCount,
                modelMetrics = modelMetrics,
                onResetModel = { viewModel.resetModel() }
              )
            }

            AppTab.INSPECTOR -> {
              ClassifierPlaygroundScreen(
                subject = playgroundSubject,
                body = playgroundBody,
                result = playgroundResult,
                onSubjectChange = { viewModel.setPlaygroundSubject(it) },
                onBodyChange = { viewModel.setPlaygroundBody(it) },
                onTestAsIncoming = { subj, bdy ->
                  viewModel.simulateCustomEmail("Playground Tester", "tester@ai-sandbox.org", subj, bdy)
                }
              )
            }
          }
        }
      }
    }
  }

  // Dialogs
  if (showSimulateDialog) {
    SimulateIncomingEmailDialog(
      onDismiss = { showSimulateDialog = false },
      onSelectPreset = { preset ->
        viewModel.simulateIncomingEmail(preset)
      },
      onSendCustom = { sender, email, subj, body ->
        viewModel.simulateCustomEmail(sender, email, subj, body)
      }
    )
  }

  if (showComposeDialog) {
    ComposeEmailDialog(
      senderEmail = userEmail,
      onDismiss = { showComposeDialog = false },
      onSend = { to, subj, body ->
        viewModel.sendEmail(to, subj, body)
      },
      classifyDraft = { subj, body ->
        viewModel.classifyDraft(subj, body)
      }
    )
  }

  if (showSettingsDialog) {
    SettingsDialog(
      userEmail = userEmail,
      userName = userName,
      isAppLockEnabled = isAppLockEnabled,
      currentPin = currentPin,
      onToggleAppLock = { viewModel.toggleAppLock(it) },
      onChangePin = { viewModel.changePin(it) },
      onTestWarningSound = { viewModel.playWarningAlertSound() },
      onOpenPrivacyPolicy = {
        showSettingsDialog = false
        showPrivacyDialog = true
      },
      onLogout = {
        showSettingsDialog = false
        viewModel.logout()
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  if (showPrivacyDialog) {
    PrivacyPolicyDialog(
      onDismiss = { showPrivacyDialog = false }
    )
  }
}
