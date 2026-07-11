package io.github.hotroso.vietnameselunarcalendar.reminder

import androidx.room.*

@Dao
interface ReminderItemDao {

    @Query("SELECT * FROM reminder_items WHERE eventId = :eventId ORDER BY daysBefore DESC")
    suspend fun getRemindersForEvent(eventId: Long): List<ReminderItem>

    @Query("SELECT * FROM reminder_items WHERE eventId IN (SELECT id FROM lunar_events WHERE isEnabled = 1) ORDER BY daysBefore DESC")
    suspend fun getAllEnabledReminders(): List<ReminderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReminderItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ReminderItem>)

    @Delete
    suspend fun delete(item: ReminderItem)

    @Query("DELETE FROM reminder_items WHERE eventId = :eventId")
    suspend fun deleteAllForEvent(eventId: Long)

    @Transaction
    suspend fun replaceRemindersForEvent(eventId: Long, items: List<ReminderItem>) {
        deleteAllForEvent(eventId)
        insertAll(items.map { it.copy(id = 0, eventId = eventId) })
    }
}
