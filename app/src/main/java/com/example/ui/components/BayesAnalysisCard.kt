package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  val statusTitle = if (isSpam) "⚠️ Quarantined by Naïve Bayes" else "✓ Verified Clean (Ham)"
  val statusIcon = if (isSpam) Icons.Default.Warning else Icons.Default.CheckCircle

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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = statusIcon,
            contentDescription = "Status",
            tint = primaryAccent,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = statusTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSpam) OnSpamRedContainer else OnHamGreenContainer
          )
        }

        Surface(
          shape = CircleShape,
          color = primaryAccent.copy(alpha = 0.15f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "ML Classifier",
              tint = primaryAccent,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Naïve Bayes",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = primaryAccent
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bayes Probability Gauge Bar
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Spam Risk: ${(animatedProb * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSpam) SpamRed else MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Legitimacy: ${((1f - animatedProb) * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (!isSpam) HamGreen else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Multi-segment progress bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(HamGreen.copy(alpha = 0.25f))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction = animatedProb.coerceIn(0.01f, 1f))
              .fillMaxHeight()
              .clip(RoundedCornerShape(5.dp))
              .background(if (animatedProb > 0.5f) SpamRed else primaryAccent)
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
