package io.github.hotroso.vietnameselunarcalendar.reminder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LunarEvent::class, ReminderItem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lunarEventDao(): LunarEventDao
    abstract fun reminderItemDao(): ReminderItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lunar_events ADD COLUMN eventType TEXT NOT NULL DEFAULT 'KHAC'")
            }
        }

        /**
         * Migration 2→3: Thêm bảng reminder_items cho multi-reminder.
         * Migrate dữ liệu cũ: mỗi event có 1 remindDaysBefore → tạo 1 ReminderItem tương ứng.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tạo bảng mới
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        eventId INTEGER NOT NULL,
                        daysBefore INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY (eventId) REFERENCES lunar_events(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_items_eventId ON reminder_items(eventId)")

                // Migrate dữ liệu cũ: tạo 1 ReminderItem cho mỗi event hiện có
                db.execSQL("""
                    INSERT INTO reminder_items (eventId, daysBefore, note)
                    SELECT id, remindDaysBefore, '' FROM lunar_events
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lunar_calendar.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
