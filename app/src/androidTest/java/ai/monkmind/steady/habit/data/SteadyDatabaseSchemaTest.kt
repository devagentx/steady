package ai.monkmind.steady.habit.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SteadyDatabaseSchemaTest {
    private lateinit var context: Context
    private lateinit var database: SteadyDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DATABASE_NAME)
        database = Room.databaseBuilder(
            context,
            SteadyDatabase::class.java,
            TEST_DATABASE_NAME,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(TEST_DATABASE_NAME)
    }

    @Test
    fun versionTwo_createsOpensAndRunsPrimaryQuery() = runBlocking {
        assertEquals(2, database.openHelper.readableDatabase.version)
        assertTrue(
            database.habitDao()
                .observeScheduledHabits("2026-09-13", weekdayBit = 1 shl 6)
                .first()
                .isEmpty(),
        )
    }

    @Test
    fun versionTwo_hasExpectedHabitColumnsAndDefaults() {
        val sqliteDatabase = database.openHelper.readableDatabase

        val columns = sqliteDatabase.query("PRAGMA table_info(`habits`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ColumnDefinition(
                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")),
                            defaultValue = cursor.getString(
                                cursor.getColumnIndexOrThrow("dflt_value"),
                            ),
                        ),
                    )
                }
            }
        }

        assertEquals(
            listOf(
                ColumnDefinition("id", 1, null),
                ColumnDefinition("name", 1, null),
                ColumnDefinition("recurrence", 1, null),
                ColumnDefinition("icon_id", 1, null),
                ColumnDefinition("color_id", 1, null),
                ColumnDefinition("created_at_epoch_ms", 1, null),
                ColumnDefinition("updated_at_epoch_ms", 1, null),
                ColumnDefinition("created_zone_id", 1, null),
                ColumnDefinition("goal_note", 0, null),
                ColumnDefinition("active_days_mask", 1, "127"),
                ColumnDefinition("schedule_type", 1, "'ROUTINE_CUE'"),
                ColumnDefinition("exact_time_minutes", 0, null),
                ColumnDefinition("routine_cue_id", 0, "'ANYTIME'"),
                ColumnDefinition("archived_at_epoch_ms", 0, null),
                ColumnDefinition("archived_zone_id", 0, null),
            ),
            columns,
        )
    }

    @Test
    fun versionTwo_preservesExpectedForeignKeyAndOnlyCompositeCompletionIndex() {
        val sqliteDatabase = database.openHelper.readableDatabase

        sqliteDatabase.query("PRAGMA foreign_key_list(`habit_completions`)").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("habits", cursor.getString(cursor.getColumnIndexOrThrow("table")))
            assertEquals("habit_id", cursor.getString(cursor.getColumnIndexOrThrow("from")))
            assertEquals("id", cursor.getString(cursor.getColumnIndexOrThrow("to")))
            assertEquals("RESTRICT", cursor.getString(cursor.getColumnIndexOrThrow("on_delete")))
            assertFalse(cursor.moveToNext())
        }

        sqliteDatabase.query("PRAGMA index_list(`habit_completions`)").use { cursor ->
            val declaredIndices = buildList {
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("origin")) == "c") {
                        add(
                            cursor.getString(cursor.getColumnIndexOrThrow("name")) to
                                cursor.getInt(cursor.getColumnIndexOrThrow("unique")),
                        )
                    }
                }
            }
            assertEquals(
                listOf("index_habit_completions_habit_id_local_date" to 1),
                declaredIndices,
            )
        }
    }

    private companion object {
        const val TEST_DATABASE_NAME = "steady-schema-test.db"
    }

    private data class ColumnDefinition(
        val name: String,
        val notNull: Int,
        val defaultValue: String?,
    )
}
