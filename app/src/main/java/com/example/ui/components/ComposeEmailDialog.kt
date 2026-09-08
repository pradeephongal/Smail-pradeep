package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ml.ClassificationResult
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ComposeEmailDialog(
  senderEmail: String,
  onDismiss: () -> Unit,
  onSend: (to: String, subject: String, body: String) -> Unit,
  classifyDraft: (subject: String, body: String) -> ClassificationResult
) {
  var toAddress by remember { mutableStateOf("colleague@domain.com") }
  var subject by remember { mutableStateOf("") }
  var body by remember { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val draftResult by remember(subject, body) {
    derivedStateOf { classifyDraft(subject, body) }
  }

  AlertDialog(
    onDismissRequest = { if (!isSending) onDismiss() },
    properties = DialogProperties(decorFitsSystemWindows = false),
    modifier = Modifier
      .fillMaxWidth()
      .imePadding()
      .padding(vertical = 12.dp)
      .testTag("compose_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Compose Real-Time Email",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
        if (!isSending) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Sender pill
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "From: ",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = senderEmail,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        OutlinedTextField(
          value = toAddress,
          onValueChange = { toAddress = it },
          label = { Text("To (Recipient Email)") },
          singleLine = true,
          enabled = !isSending,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("compose_to_input")
        )

        // Quick self-email shortcut
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          TextButton(
            onClick = { toAddress = senderEmail },
            enabled = !isSending
          ) {
            Text("Send to My Inbox", fontSize = 11.sp)
          }
          TextButton(
            onClick = {
              subject = "Quarterly Sprint Review & Presentation Deck"
              body = "Hi team,\n\nPlease review the attached slide deck for our quarterly sprint sync. Let me know if you have any feedback before tomorrow."
            },
            enabled = !isSending
          ) {
            Text("Sample Clean Note", fontSize = 11.sp)
          }
        }

        OutlinedTextField(
          value = subject,
          onValueChange = { subject = it },
          label = { Text("Subject") },
          singleLine = true,
          enabled = !isSending,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("compose_subject_input")
        )

        OutlinedTextField(
          value = body,
          onValueChange = { body = it },
          label = { Text("Message Body") },
          minLines = 4,
          maxLines = 7,
          enabled = !isSending,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("compose_body_input")
        )

        // Real-time Naïve Bayes draft evaluation badge
        if (subject.isNotBlank() || body.isNotBlank()) {
          val isSpamDraft = draftResult.isSpam
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSpamDraft) SpamRedContainer else HamGreenContainer
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isSpamDraft) "⚠️ Naïve Bayes Flag: High Spam Probability" else "✓ Smail AI: Normal Clean Email",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSpamDraft) OnSpamRedContainer else OnHamGreenContainer
              )
              Text(
                text = "${(draftResult.spamProbability * 100).toInt()}% Risk",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSpamDraft) SpamRed else HamGreen
              )
            }
          }
        }

        if (isSending) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Transmitting email in real-time via Smail engine...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if ((subject.isNotBlank() || body.isNotBlank()) && !isSending) {
            isSending = true
            scope.launch {
              delay(400) // Realistic real-time network handshake
              onSend(toAddress, subject, body)
              onDismiss()
            }
          }
        },
        enabled = !isSending && (subject.isNotBlank() || body.isNotBlank()),
        modifier = Modifier.testTag("send_email_button")
      ) {
        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Send in Real-Time")
      }
    },
    dismissButton = {
      if (!isSending) {
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}
