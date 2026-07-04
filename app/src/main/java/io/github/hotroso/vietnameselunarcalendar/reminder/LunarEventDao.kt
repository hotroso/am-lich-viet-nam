package io.github.hotroso.vietnameselunarcalendar.reminder

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LunarEventDao {

    @Query("SELECT * FROM lunar_events ORDER BY lunarMonth ASC, lunarDay ASC")
    fun getAllEvents(): LiveData<List<LunarEvent>>

    @Query("SELECT * FROM lunar_events WHERE isEnabled = 1")
    suspend fun getEnabledEvents(): List<LunarEvent>

    @Query("SELECT * FROM lunar_events WHERE id = :id")
    suspend fun getEventById(id: Long): LunarEvent?

    @Query("SELECT * FROM lunar_events WHERE lunarDay = :day AND lunarMonth = :month")
    suspend fun getEventsByLunarDate(day: Int, month: Int): List<LunarEvent>

    @Query("SELECT * FROM lunar_events WHERE isEnabled = 1")
    fun getAllEnabledEventsSync(): List<LunarEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: LunarEvent): Long

    @Update
    suspend fun update(event: LunarEvent)

    @Delete
    suspend fun delete(event: LunarEvent)

    @Query("DELETE FROM lunar_events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
