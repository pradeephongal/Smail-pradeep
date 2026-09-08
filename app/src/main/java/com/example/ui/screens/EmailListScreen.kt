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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.EmailEntity
import com.example.ui.components.EmailItemCard
import com.example.ui.theme.HamGreen
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer

@Composable
fun EmailListScreen(
  title: String,
  isSpamFolder: Boolean,
  emails: List<EmailEntity>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onSelectEmail: (EmailEntity) -> Unit,
  onToggleStar: (EmailEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Search & Filter Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_bar"),
        placeholder = { Text("Search sender, subject or body...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Status info strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (isSpamFolder) SpamRed else HamGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isSpamFolder) {
              "${emails.size} Quarantined Threats"
            } else {
              "${emails.size} Verified Messages"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (isSpamFolder) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SpamRedContainer
          ) {
            Text(
              text = "Auto-Filtered by Naïve Bayes",
              style = MaterialTheme.typography.labelSmall,
              color = OnSpamRedContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
      }
    }

    if (emails.isEmpty()) {
      // Empty State
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(72.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isSpamFolder) Icons.Default.Security else Icons.Default.Inbox,
                contentDescription = null,
                tint = if (isSpamFolder) SpamRed else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = if (isSpamFolder) "No spam emails detected" else "Your inbox is clean",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (isSpamFolder) {
              "Naïve Bayes is constantly monitoring incoming emails. Test a suspicious message using the '+' button below."
            } else {
              "Incoming messages are filtered in real-time by the Bayesian machine learning model."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("email_list")
      ) {
        items(emails, key = { it.id }) { email ->
          EmailItemCard(
            email = email,
            onClick = { onSelectEmail(email) },
            onToggleStar = { onToggleStar(email) }
          )
        }
        item {
          Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
        }
      }
    }
  }
}
