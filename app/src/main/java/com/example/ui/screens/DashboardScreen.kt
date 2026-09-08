package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ml.ModelMetrics
import com.example.ui.theme.HamGreen
import com.example.ui.theme.HamGreenContainer
import com.example.ui.theme.OnHamGreenContainer
import com.example.ui.theme.OnSpamRedContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer

@Composable
fun DashboardScreen(
  totalCount: Int,
  spamCount: Int,
  hamCount: Int,
  modelMetrics: ModelMetrics,
  onResetModel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val spamRatio = if (totalCount > 0) spamCount.toFloat() / totalCount else 0.45f
  val animatedSpamRatio by animateFloatAsState(targetValue = spamRatio, label = "donut_anim")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Header
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
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "Smail Naïve Bayes Dashboard",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "ML Domain Spam Telemetry • Created by @PRADEEP",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 4 Metrics Grid (2x2)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      MetricCard(
        title = "Total Processed",
        value = "$totalCount",
        subtitle = "Scanned emails",
        icon = Icons.Default.TrendingUp,
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f)
      )
      MetricCard(
        title = "Spam Caught",
        value = "$spamCount",
        subtitle = "${if (totalCount > 0) (spamRatio * 100).toInt() else 0}% of total",
        icon = Icons.Default.Security,
        accentColor = SpamRed,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      MetricCard(
        title = "Legitimate Ham",
        value = "$hamCount",
        subtitle = "${if (totalCount > 0) ((1f - spamRatio) * 100).toInt() else 0}% clean mail",
        icon = Icons.Default.CheckCircle,
        accentColor = HamGreen,
        modifier = Modifier.weight(1f)
      )
      MetricCard(
        title = "Vocabulary Size",
        value = "|V| = ${modelMetrics.totalVocabularySize}",
        subtitle = "Distinct feature tokens",
        icon = Icons.Default.Functions,
        accentColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Donut Chart Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Threat vs Clean Distribution",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          // Custom Canvas Donut Chart
          Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val strokeWidth = 24.dp.toPx()
              val radius = (size.minDimension - strokeWidth) / 2
              val center = Offset(size.width / 2, size.height / 2)
              val rectSize = Size(radius * 2, radius * 2)
              val topLeft = Offset(center.x - radius, center.y - radius)

              // Background Ham Arc
              drawArc(
                color = HamGreen,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = rectSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
              )

              // Foreground Spam Arc
              val spamSweep = animatedSpamRatio * 360f
              if (spamSweep > 0f) {
                drawArc(
                  color = SpamRed,
                  startAngle = -90f,
                  sweepAngle = spamSweep,
                  useCenter = false,
                  topLeft = topLeft,
                  size = rectSize,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
              }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${(animatedSpamRatio * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Spam Ratio",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Legend
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(SpamRed)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Spam Filtered",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "$spamCount emails (${(animatedSpamRatio * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(HamGreen)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Clean Ham",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "$hamCount emails (${((1f - animatedSpamRatio) * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Top Bayesian Spam Indicators Ranking Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Top Spam Keywords (Log-Odds Impact)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = SpamRedContainer
          ) {
            Text(
              text = "ln(P(w|S)/P(w|H))",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = OnSpamRedContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val maxWeight = (modelMetrics.topSpamTokens.firstOrNull()?.second ?: 4.0).coerceAtLeast(1.0)

        modelMetrics.topSpamTokens.take(6).forEach { (word, weight) ->
          Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = word,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "+${String.format(java.util.Locale.US, "%.2f", weight)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SpamRed
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(fraction = (weight / maxWeight).toFloat().coerceIn(0.05f, 1f))
                  .fillMaxHeight()
                  .clip(RoundedCornerShape(3.dp))
                  .background(SpamRed)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Model Architecture & Priors Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Naïve Bayes Model Architecture",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Algorithm:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "Multinomial Naïve Bayes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Smoothing Technique:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "Add-1 Laplace (α = 1.0)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Prior Probabilities:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = "P(S)=${String.format(java.util.Locale.US, "%.2f", modelMetrics.priorSpamProb)} • P(H)=${String.format(java.util.Locale.US, "%.2f", modelMetrics.priorHamProb)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Training Samples:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = "${modelMetrics.totalSpamDocs} Spam / ${modelMetrics.totalHamDocs} Ham",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Total Corpus Tokens:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = "${modelMetrics.totalSpamTokens + modelMetrics.totalHamTokens} tokens",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Reset Model Action
    OutlinedButton(
      onClick = onResetModel,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("reset_model_button"),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
      Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Reset Model & Seed Corpus to Baseline")
    }

    Spacer(modifier = Modifier.height(80.dp))
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp
      )
    }
  }
}
