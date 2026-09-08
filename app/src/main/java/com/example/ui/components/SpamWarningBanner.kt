package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer

data class SpamAlertInfo(
  val subject: String,
  val senderName: String,
  val spamScore: Int,
  val triggerWords: String
)

@Composable
fun SpamWarningBanner(
  alertInfo: SpamAlertInfo?,
  onDismiss: () -> Unit,
  onOpenSpamFolder: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = alertInfo != null,
    enter = expandVertically(),
    exit = shrinkVertically(),
    modifier = modifier
  ) {
    alertInfo?.let { alert ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("spam_warning_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SpamRedContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = SpamRed,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "🚨 SPAM ALERT: High-Risk Threat Detected!",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnSpamRedContainer
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = OnSpamRedContainer,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Text(
            text = "'${alert.subject}' from ${alert.senderName}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSpamRedContainer,
            modifier = Modifier.padding(vertical = 4.dp)
          )

          Text(
            text = "Naïve Bayes AI calculated a ${alert.spamScore}% spam probability. The message has been quarantined to prevent phishing.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSpamRedContainer.copy(alpha = 0.85f)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = onOpenSpamFolder) {
              Text("View in Spam Folder →", fontWeight = FontWeight.Bold, color = SpamRed)
            }
          }
        }
      }
    }
  }
}
