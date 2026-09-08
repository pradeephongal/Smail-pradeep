package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpamRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockScreen(
  onUnlockSuccess: () -> Unit,
  verifyPin: (String) -> Boolean,
  onBiometricClick: () -> Unit,
  isBiometricAvailable: Boolean,
  modifier: Modifier = Modifier
) {
  var enteredPin by remember { mutableStateOf("") }
  var hasError by remember { mutableStateOf(false) }
  val shakeOffset = remember { Animatable(0f) }
  val coroutineScope = rememberCoroutineScope()

  fun onDigitPress(digit: String) {
    if (enteredPin.length < 4) {
      val newPin = enteredPin + digit
      enteredPin = newPin
      hasError = false

      if (newPin.length == 4) {
        if (verifyPin(newPin)) {
          onUnlockSuccess()
        } else {
          hasError = true
          coroutineScope.launch {
            // Shake animation
            repeat(3) {
              shakeOffset.animateTo(20f, tween(50))
              shakeOffset.animateTo(-20f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
            enteredPin = ""
          }
        }
      }
    }
  }

  fun onBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      hasError = false
    }
  }

  fun onClear() {
    enteredPin = ""
    hasError = false
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
    ) {
      // Security Shield Icon
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(76.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(38.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Smail Security Authentication",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Protected by BiometricPrompt API & Naïve Bayes",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = if (hasError) "Incorrect PIN! Try default: 1234" else "Authenticate with Fingerprint, Face or 4-Digit PIN",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (hasError) FontWeight.Bold else FontWeight.Normal,
        color = if (hasError) SpamRed else MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 4 PIN Dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until 4) {
          val isFilled = i < enteredPin.length
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(
                when {
                  hasError -> SpamRed
                  isFilled -> MaterialTheme.colorScheme.primary
                  else -> MaterialTheme.colorScheme.surfaceVariant
                }
              )
              .border(
                width = 2.dp,
                color = if (hasError) SpamRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Biometric Unlock CTA
      OutlinedButton(
        onClick = onBiometricClick,
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .testTag("biometric_unlock_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Unlock with Biometrics (BiometricPrompt)")
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Keypad
      val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
      )

      for (row in keypadRows) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(24.dp),
          modifier = Modifier.padding(vertical = 4.dp)
        ) {
          for (key in row) {
            KeypadButton(
              key = key,
              onClick = {
                when (key) {
                  "C" -> onClear()
                  "⌫" -> onBackspace()
                  else -> onDigitPress(key)
                }
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "💡 Default PIN is: 1234 (BiometricPrompt available)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 11.sp
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Required footer: Creator name below the app
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      ) {
        Text(
          text = "Created by @PRADEEP",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun KeypadButton(
  key: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(64.dp)
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      .clickable(onClick = onClick)
      .testTag("keypad_$key"),
    contentAlignment = Alignment.Center
  ) {
    if (key == "⌫") {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.Backspace,
        contentDescription = "Backspace",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(22.dp)
      )
    } else {
      Text(
        text = key,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
