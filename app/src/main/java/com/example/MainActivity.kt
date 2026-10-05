package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.EmailViewModel
import com.example.ui.components.ComposeEmailDialog
import com.example.ui.components.SimulateIncomingEmailDialog
import com.example.ui.screens.ClassifierPlaygroundScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmailDetailScreen
import com.example.ui.screens.EmailListScreen
import com.example.ui.theme.HamGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpamRed

class MainActivity : ComponentActivity() {
  private val viewModel: EmailViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Handle intent extras if notification was tapped
    val targetFolder = intent.getStringExtra("TARGET_FOLDER")
    if (targetFolder == "SPAM") {
      viewModel.setTab(AppTab.SPAM)
    } else if (targetFolder == "INBOX") {
      viewModel.setTab(AppTab.INBOX)
    }

    setContent {
      MyApplicationTheme {
        MainApp(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: EmailViewModel) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedEmail by viewModel.selectedEmail.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
  val isAutoSimulatorActive by viewModel.isAutoSimulatorActive.collectAsStateWithLifecycle()

  val inboxEmails by viewModel.inboxEmails.collectAsStateWithLifecycle()
  val sentEmails by viewModel.sentEmails.collectAsStateWithLifecycle()
  val spamEmails by viewModel.spamEmails.collectAsStateWithLifecycle()
  val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
  val spamCount by viewModel.spamCount.collectAsStateWithLifecycle()
  val hamCount by viewModel.hamCount.collectAsStateWithLifecycle()
  val sentCount by viewModel.sentCount.collectAsStateWithLifecycle()
  val unreadInbox by viewModel.unreadInbox.collectAsStateWithLifecycle()
  val unreadSpam by viewModel.unreadSpam.collectAsStateWithLifecycle()

  val playgroundSubject by viewModel.playgroundSubject.collectAsStateWithLifecycle()
  val playgroundBody by viewModel.playgroundBody.collectAsStateWithLifecycle()
  val playgroundResult by viewModel.playgroundResult.collectAsStateWithLifecycle()
  val modelMetrics by viewModel.modelMetrics.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  var showSimulateDialog by remember { mutableStateOf(false) }
  var showComposeDialog by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf(false) }
  var showSecurityDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }

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
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  LaunchedEffect(statusMessage) {
    statusMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  // If email is selected, display detail reading screen
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
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp)
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
                text = "pradeephongal17@gmail.com",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        },
        actions = {
          // Stream Toggle Button (Simulates real-time incoming messages)
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

          IconButton(onClick = { showInfoDialog = true }) {
            Icon(Icons.Default.Info, contentDescription = "App Information")
          }

          IconButton(onClick = { showSecurityDialog = true }) {
            Icon(Icons.Default.Lock, contentDescription = "Security Status")
          }

          IconButton(onClick = { showSettingsDialog = true }) {
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
                imageVector = if (currentTab == AppTab.SENT) Icons.AutoMirrored.Filled.Send else Icons.AutoMirrored.Outlined.Send,
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

        // Bottom Brand strip
        Surface(
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Smail • On-Device Naïve Bayes",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Created by @PRADEEP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
              )
            }
          }
        }
      }
    },
    floatingActionButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FloatingActionButton(
          onClick = { showComposeDialog = true },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("compose_fab")
        ) {
          Icon(Icons.Default.Edit, contentDescription = "Compose Email")
        }

        ExtendedFloatingActionButton(
          onClick = { showSimulateDialog = true },
          icon = { Icon(Icons.Default.Add, contentDescription = null) },
          text = { Text("Simulate Incoming") },
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.testTag("simulate_email_fab")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
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
              title = "Sent",
              isSentFolder = true,
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

  if (showComposeDialog) {
    ComposeEmailDialog(
      onDismiss = { showComposeDialog = false },
      onSend = { recipient, subject, body ->
        viewModel.sendEmail(recipient, subject, body)
      }
    )
  }

  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      title = { Text("About Smail") },
      text = {
        Text("Smail is an on-device private email application featuring a real-time Naïve Bayes spam classifier. All classification occurs locally on your device with zero data shared externally.")
      },
      confirmButton = {
        TextButton(onClick = { showInfoDialog = false }) { Text("OK") }
      }
    )
  }

  if (showSecurityDialog) {
    AlertDialog(
      onDismissRequest = { showSecurityDialog = false },
      title = { Text("Security & Privacy Shield") },
      text = {
        Text("100% On-Device AI: Naïve Bayes model runs strictly on device using local SQLite database. Incoming emails are scanned in real-time without cloud inference.")
      },
      confirmButton = {
        TextButton(onClick = { showSecurityDialog = false }) { Text("Got It") }
      }
    )
  }

  if (showSettingsDialog) {
    AlertDialog(
      onDismissRequest = { showSettingsDialog = false },
      title = { Text("Settings & Model Management") },
      text = {
        Column {
          Text("Model: Naïve Bayes (Laplace smoothed)")
          Spacer(modifier = Modifier.height(8.dp))
          Text("Total processed: $totalCount")
          Text("Spam caught: $spamCount")
          Text("Ham verified: $hamCount")
          Text("Sent emails: $sentCount")
        }
      },
      confirmButton = {
        TextButton(onClick = {
          viewModel.resetModel()
          showSettingsDialog = false
        }) {
          Text("Reset Model")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSettingsDialog = false }) { Text("Close") }
      }
    )
  }

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
}
