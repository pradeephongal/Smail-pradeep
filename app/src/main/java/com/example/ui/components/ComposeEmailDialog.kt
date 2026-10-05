package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ComposeEmailDialog(
  onDismiss: () -> Unit,
  onSend: (recipientEmail: String, subject: String, body: String) -> Unit
) {
  var recipient by remember { mutableStateOf("") }
  var subject by remember { mutableStateOf("") }
  var body by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Compose Email",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Text(
          text = "From: pradeephongal17@gmail.com",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = recipient,
          onValueChange = { recipient = it },
          label = { Text("To") },
          placeholder = { Text("colleague@example.com") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("compose_recipient_input")
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = subject,
          onValueChange = { subject = it },
          label = { Text("Subject") },
          placeholder = { Text("Enter subject...") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("compose_subject_input")
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = body,
          onValueChange = { body = it },
          label = { Text("Message Body") },
          placeholder = { Text("Write your email content...") },
          minLines = 4,
          maxLines = 8,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("compose_body_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSend(recipient, subject, body)
          onDismiss()
        },
        enabled = recipient.isNotBlank() || subject.isNotBlank() || body.isNotBlank(),
        modifier = Modifier.testTag("compose_send_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Send")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
