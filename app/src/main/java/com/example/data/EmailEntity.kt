package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val senderName: String,
  val senderEmail: String,
  val subject: String,
  val body: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isSpam: Boolean,
  val spamProbability: Float, // 0.0 to 1.0
  val topTriggerWords: String, // Comma-separated or JSON tokens
  val folder: String, // "INBOX", "SPAM", "TRASH"
  val isRead: Boolean = false,
  val isStarred: Boolean = false,
  val userFeedback: String? = null // "MANUAL_SPAM", "MANUAL_HAM", null
)
