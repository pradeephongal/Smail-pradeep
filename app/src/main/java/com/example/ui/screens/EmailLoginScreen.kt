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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.UserAccountManager

@Composable
fun EmailLoginScreen(
  onLoginSuccess: (email: String, name: String) -> Unit,
  onBiometricUnlock: () -> Unit,
  isBiometricAvailable: Boolean,
  modifier: Modifier = Modifier
) {
  var emailInput by remember { mutableStateOf(UserAccountManager.DEFAULT_EMAIL) }
  var nameInput by remember { mutableStateOf(UserAccountManager.DEFAULT_NAME) }
  var passwordInput by remember { mutableStateOf("••••••••") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .systemBarsPadding()
      .imePadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // App Logo
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Welcome to Smail",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Secure Email with Naïve Bayes AI Spam Shield",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
      ) {
        Text(
          text = "Created by @PRADEEP",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Login Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Sign in with your Email",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Connect your account to access filtered inbox & spam quarantine",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = emailInput,
            onValueChange = {
              emailInput = it
              errorMessage = null
            },
            label = { Text("Email Address") },
            leadingIcon = {
              Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_email_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_name_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Password / App Key") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = err,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              if (emailInput.isNotBlank() && emailInput.contains("@")) {
                onLoginSuccess(emailInput.trim(), nameInput.trim())
              } else {
                errorMessage = "Please enter a valid email address"
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_submit_button")
          ) {
            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign In to Smail")
          }

          if (isBiometricAvailable) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
              onClick = onBiometricUnlock,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_biometric_button")
            ) {
              Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Authenticate with Biometrics")
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Quick Demo Switcher
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        TextButton(
          onClick = {
            emailInput = "pradeephongal17@gmail.com"
            nameInput = "Pradeep"
          }
        ) {
          Text("Fill @PRADEEP Account", fontSize = 12.sp)
        }
        TextButton(
          onClick = {
            emailInput = "alex.dev@corp-mail.io"
            nameInput = "Alex Dev"
          }
        ) {
          Text("Fill Demo User", fontSize = 12.sp)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bottom Author attribution
      Text(
        text = "Smail • Created by @PRADEEP",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
    }
  }
}
