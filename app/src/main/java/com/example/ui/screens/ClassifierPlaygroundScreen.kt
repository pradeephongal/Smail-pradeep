package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ml.ClassificationResult
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassifierPlaygroundScreen(
  subject: String,
  body: String,
  result: ClassificationResult?,
  onSubjectChange: (String) -> Unit,
  onBodyChange: (String) -> Unit,
  onTestAsIncoming: (subject: String, body: String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Title
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 16.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "ML Inspector & Playground",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Test arbitrary text & inspect Naïve Bayes calculations",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Quick preset templates
    Text(
      text = "Load Sample Test Case:",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SampleChip(
        label = "🎰 Cash Prize Scam",
        onClick = {
          onSubjectChange("URGENT: You won $1,000,000 lottery cash prize! Claim bonus!")
          onBodyChange("Congratulations! Click here to verify your bank account and receive immediate wire transfer.")
        }
      )
      SampleChip(
        label = "🚨 Phishing Password",
        onClick = {
          onSubjectChange("Urgent: Your account is suspended due to security violation")
          onBodyChange("Action required: Verify password and billing details immediately to restore your account access.")
        }
      )
      SampleChip(
        label = "💼 Normal Meeting",
        onClick = {
          onSubjectChange("Architecture review meeting and sprint planning notes")
          onBodyChange("Hi team, please review the attached database schema document before our sync at 2 PM.")
        }
      )
      SampleChip(
        label = "🍕 Social Catchup",
        onClick = {
          onSubjectChange("Coffee and lunch tomorrow?")
          onBodyChange("Hey! Are you free for a quick chat and coffee near the office cafeteria around noon?")
        }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Input Fields
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
          value = subject,
          onValueChange = onSubjectChange,
          label = { Text("Subject Line") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("playground_subject_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = body,
          onValueChange = onBodyChange,
          label = { Text("Email Body") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("playground_body_input"),
          minLines = 3,
          maxLines = 6
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = { onTestAsIncoming(subject, body) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("test_as_incoming_button"),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Send as Real-Time Incoming Notification")
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Live Classification Math Output Card
    AnimatedVisibility(visible = result != null) {
      result?.let { res ->
        val isSpam = res.isSpam
        val accentColor = if (isSpam) SpamRed else HamGreen
        val containerColor = if (isSpam) SpamRedContainer else HamGreenContainer
        val textColor = if (isSpam) OnSpamRedContainer else OnHamGreenContainer

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            // Header Verdict
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isSpam) Icons.Default.Warning else Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isSpam) "CLASSIFIED AS SPAM" else "CLASSIFIED AS CLEAN HAM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                  )
                  Text(
                    text = "Threshold = 0.50",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = containerColor
              ) {
                Text(
                  text = "${(res.spamProbability * 100).toInt()}% Risk",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = textColor,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mathematical Probability breakdown
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(14.dp)
            ) {
              Text(
                text = "Bayesian Posterior Calculation:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "P(Spam | Message) = ${String.format(java.util.Locale.US, "%.4f", res.spamProbability)} (${(res.spamProbability * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isSpam) SpamRed else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "P(Ham | Message)  = ${String.format(java.util.Locale.US, "%.4f", res.hamProbability)} (${(res.hamProbability * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (!isSpam) HamGreen else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Log-Likelihood Difference Δ = ${String.format(java.util.Locale.US, "%+.2f", res.logOddsRatio)}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Extracted Tokens: ${res.extractedTokensCount} active words",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contributing Tokens
            Text(
              text = "Top Contributing Feature Words (Log-Odds Impact):",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (res.topTriggerWords.isEmpty()) {
              Text(
                text = "No prominent trigger words detected. Using class priors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                res.topTriggerWords.forEach { wordImpact ->
                  val isSpamBias = wordImpact.impactScore > 0
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSpamBias) SpamRedContainer else HamGreenContainer
                  ) {
                    Text(
                      text = "${wordImpact.word} (${String.format(java.util.Locale.US, "%+.1f", wordImpact.impactScore)})",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = if (isSpamBias) OnSpamRedContainer else OnHamGreenContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(80.dp))
  }
}

@Composable
private fun SampleChip(
  label: String,
  onClick: () -> Unit
) {
  OutlinedButton(
    onClick = onClick,
    shape = RoundedCornerShape(20.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(text = label, style = MaterialTheme.typography.labelSmall)
  }
}
