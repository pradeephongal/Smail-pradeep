package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmailEntity
import com.example.ui.components.BayesAnalysisCard
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailScreen(
  email: EmailEntity,
  onBack: () -> Unit,
  onMarkAsSpam: () -> Unit,
  onMarkAsNotSpam: () -> Unit,
  onDelete: () -> Unit,
  onToggleStar: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(email.timestamp))

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (email.isSpam) "Spam Message" else "Inbox Message",
            style = MaterialTheme.typography.titleMedium
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = onToggleStar) {
            Icon(
              imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
              contentDescription = "Star",
              tint = if (email.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_email_button")) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Subject Header
      Text(
        text = email.subject,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Sender Info Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val initial = email.senderName.take(1).uppercase()
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (email.isSpam) SpamRedContainer else MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (email.isSpam) OnSpamRedContainer else MaterialTheme.colorScheme.onPrimaryContainer
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = email.senderName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Text(
            text = email.senderEmail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text(
            text = formattedDate,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Bayesian Machine Learning Classifier Analysis Card
      BayesAnalysisCard(
        isSpam = email.isSpam,
        spamProbability = email.spamProbability,
        triggerTokensString = email.topTriggerWords
      )

      // Active Learning User Feedback Chip if already corrected
      if (email.userFeedback != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "ℹ️ Model re-trained: User manually marked as ${if (email.userFeedback == "MANUAL_SPAM") "Spam" else "Legitimate Ham"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Email Body Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = email.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Active Learning Feedback Actions
      Text(
        text = "Machine Learning Active Training Feedback",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Your feedback directly updates the Bayes probability counts and retrains the classifier on this device.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (!email.isSpam) {
          Button(
            onClick = onMarkAsSpam,
            colors = ButtonDefaults.buttonColors(containerColor = SpamRed),
            modifier = Modifier
              .weight(1f)
              .testTag("report_spam_button")
          ) {
            Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Report as Spam")
          }
        } else {
          Button(
            onClick = onMarkAsNotSpam,
            colors = ButtonDefaults.buttonColors(containerColor = HamGreen),
            modifier = Modifier
              .weight(1f)
              .testTag("mark_not_spam_button")
          ) {
            Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Not Spam (Move to Inbox)")
          }
        }

        OutlinedButton(
          onClick = onDelete,
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Delete")
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}
