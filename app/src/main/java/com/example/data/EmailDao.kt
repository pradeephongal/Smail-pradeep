package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {

  @Query("SELECT * FROM emails WHERE folder = :folder ORDER BY timestamp DESC")
  fun getEmailsByFolder(folder: String): Flow<List<EmailEntity>>

  @Query("SELECT * FROM emails ORDER BY timestamp DESC")
  fun getAllEmails(): Flow<List<EmailEntity>>

  @Query("SELECT * FROM emails WHERE id = :id LIMIT 1")
  fun getEmailById(id: Long): Flow<EmailEntity?>

  @Query("SELECT COUNT(*) FROM emails WHERE isSpam = 1")
  fun getSpamCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM emails WHERE isSpam = 0")
  fun getHamCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM emails")
  fun getTotalCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM emails WHERE userFeedback IS NOT NULL")
  fun getFeedbackCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM emails WHERE folder = 'INBOX' AND isRead = 0")
  fun getUnreadInboxCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM emails WHERE folder = 'SPAM' AND isRead = 0")
  fun getUnreadSpamCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEmail(email: EmailEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEmails(emails: List<EmailEntity>)

  @Update
  suspend fun updateEmail(email: EmailEntity)

  @Query("DELETE FROM emails WHERE id = :id")
  suspend fun deleteEmail(id: Long)

  @Query("UPDATE emails SET folder = 'TRASH' WHERE id = :id")
  suspend fun moveToTrash(id: Long)

  @Query("UPDATE emails SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: Long)

  @Query("UPDATE emails SET isStarred = NOT isStarred WHERE id = :id")
  suspend fun toggleStar(id: Long)

  @Query("DELETE FROM emails WHERE folder = 'TRASH'")
  suspend fun emptyTrash()

  @Query("SELECT COUNT(*) FROM emails")
  suspend fun getDirectCount(): Int
}
