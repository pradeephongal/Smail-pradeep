package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ml.ClassificationResult
import com.example.ui.SimulationPreset
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer

@Composable
fun SimulateIncomingEmailDialog(
  onDismiss: () -> Unit,
  onSelectPreset: (SimulationPreset) -> Unit,
  onSendCustom: (senderName: String, senderEmail: String, subject: String, body: String) -> Unit,
  onClassifyLive: ((senderName: String, senderEmail: String, subject: String, body: String) -> ClassificationResult)? = null
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  var customSenderName by remember { mutableStateOf("") }
  var customSenderEmail by remember { mutableStateOf("") }
  var customSubject by remember { mutableStateOf("") }
  var customBody by remember { mutableStateOf("") }

  // Live classification preview
  val hasInput = customSubject.isNotBlank() || customBody.isNotBlank() || customSenderEmail.isNotBlank()
  val liveResult = remember(customSenderName, customSenderEmail, customSubject, customBody, onClassifyLive) {
    if (onClassifyLive != null && hasInput) {
      onClassifyLive(customSenderName, customSenderEmail, customSubject, customBody)
    } else {
      null
    }
  }

  val isLiveSpam = liveResult?.isSpam ?: false

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(decorFitsSystemWindows = false),
    modifier = Modifier
      .fillMaxWidth()
      .imePadding()
      .padding(vertical = 12.dp)
      .testTag("scan_email_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isLiveSpam) SpamRedContainer else HamGreenContainer,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isLiveSpam) Icons.Default.Security else Icons.Default.Inbox,
                contentDescription = null,
                tint = if (isLiveSpam) SpamRed else HamGreen,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Scan & Filter Real Email",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Detects Spam vs Safe like Gmail",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Scan Any Email") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Test Scenarios") }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
          // Tab 0: Real Email Scanning & Input
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Paste or type any real email to analyze it. If spam, it directly moves to the Spam section; if safe, it delivers to your Inbox.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Quick Samples helper row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  customSenderName = "Google Cloud Team"
                  customSenderEmail = "billing@cloud.google.com"
                  customSubject = "Monthly Service Invoice & Billing Statement"
                  customBody = "Hi, your monthly cloud usage statement for the current billing cycle is now available to review in your console."
                },
                modifier = Modifier.weight(1f)
              ) {
                Text("Paste Safe Mail", fontSize = 11.sp, maxLines = 1)
              }

              OutlinedButton(
                onClick = {
                  customSenderName = "Lottery Prize Department"
                  customSenderEmail = "winner@prize-pool.org"
                  customSubject = "CONGRATULATIONS: You won $2,500,000 cash bonus!"
                  customBody = "Urgent claim notice: Your email won millions. Click here to verify bank account password and receive immediate wire transfer today!"
                },
                modifier = Modifier.weight(1f)
              ) {
                Text("Paste Spam Mail", fontSize = 11.sp, maxLines = 1)
              }
            }

            OutlinedTextField(
              value = customSenderName,
              onValueChange = { customSenderName = it },
              label = { Text("Sender Name") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_sender_name_input")
            )

            OutlinedTextField(
              value = customSenderEmail,
              onValueChange = { customSenderEmail = it },
              label = { Text("Sender Email Address") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_sender_email_input")
            )

            OutlinedTextField(
              value = customSubject,
              onValueChange = { customSubject = it },
              label = { Text("Email Subject") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_subject_input")
            )

            OutlinedTextField(
              value = customBody,
              onValueChange = { customBody = it },
              label = { Text("Email Message Body") },
              minLines = 3,
              maxLines = 6,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_body_input")
            )

            // Live Spam Detection Verdict Card with Safe vs Fake Visual Graphic
            if (liveResult != null) {
              val isSpam = liveResult.isSpam

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .border(
                    width = 1.5.dp,
                    color = if (isSpam) SpamRed else HamGreen,
                    shape = RoundedCornerShape(16.dp)
                  ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSpam) SpamRedContainer.copy(alpha = 0.45f) else HamGreenContainer.copy(alpha = 0.45f)
                )
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    // Safe vs Fake Visual Image Asset
                    Image(
                      painter = painterResource(
                        if (isSpam) R.drawable.ic_fake_spam_art else R.drawable.ic_safe_verified_art
                      ),
                      contentDescription = if (isSpam) "Spam Warning Graphic" else "Safe Original Email Graphic",
                      modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = if (isSpam) "SPAM DETECTED" else "SAFE EMAIL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpam) SpamRed else HamGreen
                      )

                      Spacer(modifier = Modifier.height(3.dp))

                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = (if (isSpam) SpamRed else HamGreen).copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = if (isSpam) "⚠️ Auto-Routes to Spam" else "✓ Auto-Delivers to Inbox",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = if (isSpam) SpamRed else HamGreen,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  Text(
                    text = liveResult.detectionDetails,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )

                  Spacer(modifier = Modifier.height(6.dp))

                  Text(
                    text = if (isSpam) {
                      "Gmail-style Protection: Smail will quarantine this email in the Spam folder."
                    } else {
                      "Gmail-style Protection: Smail verified this email is clean. Delivered to Inbox."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )

                  if (isSpam && liveResult.topTriggerWords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Spam triggers: " + liveResult.topTriggerWords.take(4).joinToString(", ") { it.word },
                      style = MaterialTheme.typography.labelSmall,
                      color = SpamRed,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            } else {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "Type or paste an email above to see instant automatic detection like Gmail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        } else {
          // Tab 1: Presets for quick demonstration
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Choose a test scenario to see how the classifier automatically sorts incoming mail:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SimulationPreset.entries.forEach { preset ->
              val isSpamPreset = preset == SimulationPreset.LOTTERY_SCAM ||
                  preset == SimulationPreset.PHISHING_BANK ||
                  preset == SimulationPreset.CRYPTO_GIVEAWAY

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("preset_${preset.name}")
                  .clickable {
                    onSelectPreset(preset)
                    onDismiss()
                  },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSpamPreset) SpamRedContainer.copy(alpha = 0.25f)
                  else HamGreenContainer.copy(alpha = 0.25f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (isSpamPreset) Icons.Default.Security else Icons.Default.Email,
                    contentDescription = null,
                    tint = if (isSpamPreset) SpamRed else HamGreen,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        text = preset.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSpamPreset) SpamRedContainer else HamGreenContainer
                      ) {
                        Text(
                          text = if (isSpamPreset) "Routes to Spam" else "Delivers to Inbox",
                          style = MaterialTheme.typography.labelSmall,
                          color = if (isSpamPreset) OnSpamRedContainer else OnHamGreenContainer,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = preset.subject,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (selectedTab == 0) {
        val buttonColor = if (isLiveSpam) SpamRed else MaterialTheme.colorScheme.primary

        Button(
          onClick = {
            onSendCustom(customSenderName, customSenderEmail, customSubject, customBody)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
          modifier = Modifier.testTag("scan_and_route_button")
        ) {
          Icon(
            imageVector = if (isLiveSpam) Icons.Default.Security else Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            if (isLiveSpam) "Filter Directly to Spam" else "Filter & Deliver to Inbox"
          )
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
