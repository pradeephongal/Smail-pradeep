package com.example.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
  onSendCustom: (senderName: String, senderEmail: String, subject: String, body: String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  var customSenderName by remember { mutableStateOf("Lottery Award Notification") }
  var customSenderEmail by remember { mutableStateOf("claim@urgent-prize.net") }
  var customSubject by remember { mutableStateOf("You have won $500,000 cash prize! Claim immediately!") }
  var customBody by remember {
    mutableStateOf("Congratulations! Click here to verify your account password and wire transfer details urgently.")
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(decorFitsSystemWindows = false),
    modifier = Modifier
      .fillMaxWidth()
      .imePadding()
      .padding(vertical = 16.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AddAlert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Simulate Incoming Email",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
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
        Text(
          text = "Test real-time Naïve Bayes classification and incoming notification delivery.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Presets") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Custom Input") }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
          // Presets
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                          text = if (isSpamPreset) "Spam Test" else "Ham Test",
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
        } else {
          // Custom Email form
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = customSenderName,
              onValueChange = { customSenderName = it },
              label = { Text("Sender Name") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = customSenderEmail,
              onValueChange = { customSenderEmail = it },
              label = { Text("Sender Email Address") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = customSubject,
              onValueChange = { customSubject = it },
              label = { Text("Subject Line") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = customBody,
              onValueChange = { customBody = it },
              label = { Text("Email Message Body") },
              minLines = 3,
              maxLines = 6,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    },
    confirmButton = {
      if (selectedTab == 1) {
        Button(
          onClick = {
            onSendCustom(customSenderName, customSenderEmail, customSubject, customBody)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("send_custom_button")
        ) {
          Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Simulate & Notify")
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
