package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmailEntity
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmailItemCard(
  email: EmailEntity,
  onClick: () -> Unit,
  onToggleStar: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
  val timeString = dateFormat.format(Date(email.timestamp))

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("email_item_${email.id}")
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (!email.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (!email.isRead) 2.dp else 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Sender Avatar with Initials
      val initial = email.senderName.take(1).uppercase()
      val avatarBg = if (email.isSpam) {
        SpamRedContainer
      } else {
        MaterialTheme.colorScheme.primaryContainer
      }
      val avatarTextColor = if (email.isSpam) {
        OnSpamRedContainer
      } else {
        MaterialTheme.colorScheme.onPrimaryContainer
      }

      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(avatarBg),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initial,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = avatarTextColor
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = email.senderName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = email.subject,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (!email.isRead) FontWeight.SemiBold else FontWeight.Normal,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = email.body,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ML Status Pill and Trigger Words
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (email.isSpam) {
            val spamScore = (email.spamProbability * 100).toInt()
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SpamRedContainer
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = "Spam Detected",
                  tint = SpamRed,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Bayes: $spamScore% Spam",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = OnSpamRedContainer
                )
              }
            }
          } else {
            val hamScore = ((1f - email.spamProbability) * 100).toInt()
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = HamGreenContainer
            ) {
              Text(
                text = "✓ $hamScore% Clean Ham",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnHamGreenContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          IconButton(
            onClick = onToggleStar,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
              contentDescription = if (email.isStarred) "Starred" else "Unstarred",
              tint = if (email.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
