package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BayesAnalysisCard(
  isSpam: Boolean,
  spamProbability: Float,
  triggerTokensString: String,
  modifier: Modifier = Modifier
) {
  val animatedProb by animateFloatAsState(
    targetValue = spamProbability,
    label = "spam_prob_anim"
  )

  val cardBg = if (isSpam) {
    SpamRedContainer.copy(alpha = 0.35f)
  } else {
    HamGreenContainer.copy(alpha = 0.35f)
  }

  val primaryAccent = if (isSpam) SpamRed else HamGreen
  val statusTitle = if (isSpam) "SPAM DETECTED" else "SAFE EMAIL"
  val statusDesc = if (isSpam) {
    "Moved to Spam: Smail automated security filter identified this message as suspicious or spam."
  } else {
    "Delivered to Inbox: Smail automated security filter verified this message is clean and authentic."
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Safe vs Fake Visual Graphic Asset
        Image(
          painter = painterResource(
            if (isSpam) R.drawable.ic_fake_spam_art else R.drawable.ic_safe_verified_art
          ),
          contentDescription = if (isSpam) "Fake Spam Warning Graphic" else "Safe Original Email Graphic",
          modifier = Modifier.size(52.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = statusTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (isSpam) SpamRed else HamGreen
            )

            Surface(
              shape = CircleShape,
              color = primaryAccent.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (isSpam) Icons.Default.Warning else Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = primaryAccent,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isSpam) "Spam Filter" else "Verified Safe",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = primaryAccent
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = statusDesc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Single Definitive Status (Like Gmail - Never show safe and spam together)
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (isSpam) "Classification: Spam Message" else "Classification: Clean Inbox Message",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSpam) SpamRed else HamGreen
          )
          Text(
            text = if (isSpam) "${(animatedProb * 100).toInt()}% Threat Confidence" else "${((1f - animatedProb) * 100).toInt()}% Safety Confidence",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSpam) SpamRed else HamGreen
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Single solid colored bar matching verdict
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSpam) SpamRedContainer else HamGreenContainer)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction = (if (isSpam) animatedProb else (1f - animatedProb)).coerceIn(0.1f, 1f))
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(primaryAccent)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Formula insight
      Text(
        text = "P(Spam | Words) ∝ P(Spam) × ∏ P(wᵢ | Spam) with Laplace α=1.0",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
      )

      if (triggerTokensString.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Key Bayesian Trigger Words:",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        val tokens = triggerTokensString.split(",").map { it.trim() }.filter { it.isNotBlank() }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          for (token in tokens) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSpam) SpamRedContainer else MaterialTheme.colorScheme.surfaceVariant,
              shadowElevation = 0.dp
            ) {
              Text(
                text = token,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSpam) OnSpamRedContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}
