package ai.monkmind.steady.habit.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.DayOfWeek
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SteadyDatabaseMigrationTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SteadyDatabase::class.java,
    )

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        context.deleteDatabase(MIGRATION_DATABASE_NAME)
        context.deleteDatabase(FRESH_DATABASE_NAME)
    }

    @Test
    fun migrateOneToTwo_preservesR0DataAndAppliesExactR1Defaults() {
        migrationHelper.createDatabase(MIGRATION_DATABASE_NAME, 1).apply {
            insertVersionOneFixtures(this)
            close()
        }

        val migrated = migrationHelper.runMigrationsAndValidate(
            MIGRATION_DATABASE_NAME,
            2,
            true,
            MIGRATION_1_2,
        )

        assertEquals(EXPECTED_HABITS, readHabits(migrated))
        assertEquals(EXPECTED_COMPLETIONS, readCompletions(migrated))
        assertEquals(2, readCompletions(migrated).size)

        migrated.query("PRAGMA foreign_key_check").use { cursor ->
            assertFalse(cursor.moveToFirst())
        }
        assertCompletionForeignKey(migrated)
        assertCompletionIndex(migrated)
        assertUniqueCompletionBusinessKey(migrated)
        assertMigratedHabitsAreVisibleEveryDay(migrated)
        migrated.close()
    }

    @Test
    fun migrateOneToTwo_matchesFreshVersionTwoSchema() {
        migrationHelper.createDatabase(MIGRATION_DATABASE_NAME, 1).close()
        val migrated = migrationHelper.runMigrationsAndValidate(
            MIGRATION_DATABASE_NAME,
            2,
            true,
            MIGRATION_1_2,
        )
        val migratedSchema = readSchema(migrated)

        val freshRoomDatabase = Room.databaseBuilder(
            context,
            SteadyDatabase::class.java,
            FRESH_DATABASE_NAME,
        ).build()
        val freshDatabase = freshRoomDatabase.openHelper.writableDatabase
        val freshSchema = readSchema(freshDatabase)

        assertEquals(freshSchema, migratedSchema)

        migrated.close()
        freshRoomDatabase.close()
    }

    private fun insertVersionOneFixtures(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            INSERT INTO habits (
                id, name, recurrence, icon_id, color_id,
                created_at_epoch_ms, updated_at_epoch_ms, created_zone_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "habit-evening",
                "Evening walk",
                "DAILY",
                "steps",
                "warm",
                1_725_814_800_000,
                1_725_814_800_000,
                "America/Los_Angeles",
            ),
        )
        database.execSQL(
            """
            INSERT INTO habits (
                id, name, recurrence, icon_id, color_id,
                created_at_epoch_ms, updated_at_epoch_ms, created_zone_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "habit-morning",
                "Morning water",
                "DAILY",
                "drop",
                "blue",
                1_725_778_800_000,
                1_725_779_100_000,
                "Asia/Kolkata",
            ),
        )
        database.execSQL(
            """
            INSERT INTO habit_completions (
                id, habit_id, local_date, completed_at_epoch_ms, completed_zone_id
            ) VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "completion-later",
                "habit-evening",
                "2024-09-09",
                1_725_929_400_000,
                "America/Los_Angeles",
            ),
        )
        database.execSQL(
            """
            INSERT INTO habit_completions (
                id, habit_id, local_date, completed_at_epoch_ms, completed_zone_id
            ) VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "completion-earlier",
                "habit-morning",
                "2024-09-08",
                1_725_790_200_000,
                "Asia/Kolkata",
            ),
        )
    }

    private fun readHabits(database: SupportSQLiteDatabase): List<HabitRecord> =
        database.query(
            """
            SELECT
                id, name, recurrence, icon_id, color_id,
                created_at_epoch_ms, updated_at_epoch_ms, created_zone_id,
                goal_note, active_days_mask, schedule_type, exact_time_minutes,
                routine_cue_id, archived_at_epoch_ms, archived_zone_id
            FROM habits
            ORDER BY id
            """.trimIndent(),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        HabitRecord(
                            id = cursor.getString(0),
                            name = cursor.getString(1),
                            recurrence = cursor.getString(2),
                            iconId = cursor.getString(3),
                            colorId = cursor.getString(4),
                            createdAtEpochMs = cursor.getLong(5),
                            updatedAtEpochMs = cursor.getLong(6),
                            createdZoneId = cursor.getString(7),
                            goalNote = cursor.getStringOrNull(8),
                            activeDaysMask = cursor.getInt(9),
                            scheduleType = cursor.getString(10),
                            exactTimeMinutes = cursor.getIntOrNull(11),
                            routineCueId = cursor.getStringOrNull(12),
                            archivedAtEpochMs = cursor.getLongOrNull(13),
                            archivedZoneId = cursor.getStringOrNull(14),
                        ),
                    )
                }
            }
        }

    private fun readCompletions(database: SupportSQLiteDatabase): List<CompletionRecord> =
        database.query(
            """
            SELECT id, habit_id, local_date, completed_at_epoch_ms, completed_zone_id
            FROM habit_completions
            ORDER BY id
            """.trimIndent(),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CompletionRecord(
                            id = cursor.getString(0),
                            habitId = cursor.getString(1),
                            localDate = cursor.getString(2),
                            completedAtEpochMs = cursor.getLong(3),
                            completedZoneId = cursor.getString(4),
                        ),
                    )
                }
            }
        }

    private fun assertCompletionForeignKey(database: SupportSQLiteDatabase) {
        database.query("PRAGMA foreign_key_list(`habit_completions`)").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("habits", cursor.getString(cursor.getColumnIndexOrThrow("table")))
            assertEquals("habit_id", cursor.getString(cursor.getColumnIndexOrThrow("from")))
            assertEquals("id", cursor.getString(cursor.getColumnIndexOrThrow("to")))
            assertEquals("RESTRICT", cursor.getString(cursor.getColumnIndexOrThrow("on_delete")))
            assertFalse(cursor.moveToNext())
        }
    }

    private fun assertCompletionIndex(database: SupportSQLiteDatabase) {
        val indices = database.query("PRAGMA index_list(`habit_completions`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("origin")) == "c") {
                        add(
                            cursor.getString(cursor.getColumnIndexOrThrow("name")) to
                                cursor.getInt(cursor.getColumnIndexOrThrow("unique")),
                        )
                    }
                }
            }
        }
        assertEquals(
            listOf("index_habit_completions_habit_id_local_date" to 1),
            indices,
        )
    }

    private fun assertUniqueCompletionBusinessKey(database: SupportSQLiteDatabase) {
        try {
            database.execSQL(
                """
                INSERT INTO habit_completions (
                    id, habit_id, local_date, completed_at_epoch_ms, completed_zone_id
                ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    "completion-duplicate",
                    "habit-morning",
                    "2024-09-08",
                    1_725_790_300_000,
                    "UTC",
                ),
            )
            fail("Expected the migrated unique habit/date index to reject a duplicate")
        } catch (_: SQLiteConstraintException) {
            // Expected.
        }
    }

    private fun assertMigratedHabitsAreVisibleEveryDay(database: SupportSQLiteDatabase) {
        val masks = database.query("SELECT active_days_mask FROM habits").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getInt(0))
                }
            }
        }

        DayOfWeek.entries.forEachIndexed { index, _ ->
            assertTrue(masks.all { mask -> mask and (1 shl index) != 0 })
        }
    }

    private fun readSchema(database: SupportSQLiteDatabase): DatabaseSchema =
        DatabaseSchema(
            habitColumns = readTableColumns(database, "habits"),
            completionColumns = readTableColumns(database, "habit_completions"),
            completionForeignKeys = readForeignKeys(database, "habit_completions"),
            completionIndices = readIndices(database, "habit_completions"),
        )

    private fun readTableColumns(
        database: SupportSQLiteDatabase,
        table: String,
    ): List<ColumnSchema> =
        database.query("PRAGMA table_info(`$table`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ColumnSchema(
                            position = cursor.getInt(cursor.getColumnIndexOrThrow("cid")),
                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                            notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")),
                            defaultValue = cursor.getStringOrNull(
                                cursor.getColumnIndexOrThrow("dflt_value"),
                            ),
                            primaryKeyPosition = cursor.getInt(
                                cursor.getColumnIndexOrThrow("pk"),
                            ),
                        ),
                    )
                }
            }
        }

    private fun readForeignKeys(
        database: SupportSQLiteDatabase,
        table: String,
    ): List<ForeignKeySchema> =
        database.query("PRAGMA foreign_key_list(`$table`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ForeignKeySchema(
                            referencedTable = cursor.getString(
                                cursor.getColumnIndexOrThrow("table"),
                            ),
                            from = cursor.getString(cursor.getColumnIndexOrThrow("from")),
                            to = cursor.getString(cursor.getColumnIndexOrThrow("to")),
                            onUpdate = cursor.getString(
                                cursor.getColumnIndexOrThrow("on_update"),
                            ),
                            onDelete = cursor.getString(
                                cursor.getColumnIndexOrThrow("on_delete"),
                            ),
                        ),
                    )
                }
            }
        }

    private fun readIndices(
        database: SupportSQLiteDatabase,
        table: String,
    ): List<IndexSchema> =
        database.query("PRAGMA index_list(`$table`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("origin")) == "c") {
                        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val columns = database.query("PRAGMA index_info(`$name`)").use {
                            indexCursor ->
                            buildList {
                                while (indexCursor.moveToNext()) {
                                    add(
                                        indexCursor.getString(
                                            indexCursor.getColumnIndexOrThrow("name"),
                                        ),
                                    )
                                }
                            }
                        }
                        add(
                            IndexSchema(
                                name = name,
                                unique = cursor.getInt(cursor.getColumnIndexOrThrow("unique")),
                                columns = columns,
                            ),
                        )
                    }
                }
            }
        }

    private fun android.database.Cursor.getStringOrNull(index: Int): String? =
        if (isNull(index)) null else getString(index)

    private fun android.database.Cursor.getIntOrNull(index: Int): Int? =
        if (isNull(index)) null else getInt(index)

    private fun android.database.Cursor.getLongOrNull(index: Int): Long? =
        if (isNull(index)) null else getLong(index)

    private data class HabitRecord(
        val id: String,
        val name: String,
        val recurrence: String,
        val iconId: String,
        val colorId: String,
        val createdAtEpochMs: Long,
        val updatedAtEpochMs: Long,
        val createdZoneId: String,
        val goalNote: String?,
        val activeDaysMask: Int,
        val scheduleType: String,
        val exactTimeMinutes: Int?,
        val routineCueId: String?,
        val archivedAtEpochMs: Long?,
        val archivedZoneId: String?,
    )

    private data class CompletionRecord(
        val id: String,
        val habitId: String,
        val localDate: String,
        val completedAtEpochMs: Long,
        val completedZoneId: String,
    )

    private data class DatabaseSchema(
        val habitColumns: List<ColumnSchema>,
        val completionColumns: List<ColumnSchema>,
        val completionForeignKeys: List<ForeignKeySchema>,
        val completionIndices: List<IndexSchema>,
    )

    private data class ColumnSchema(
        val position: Int,
        val name: String,
        val type: String,
        val notNull: Int,
        val defaultValue: String?,
        val primaryKeyPosition: Int,
    )

    private data class ForeignKeySchema(
        val referencedTable: String,
        val from: String,
        val to: String,
        val onUpdate: String,
        val onDelete: String,
    )

    private data class IndexSchema(
        val name: String,
        val unique: Int,
        val columns: List<String>,
    )

    private companion object {
        const val MIGRATION_DATABASE_NAME = "steady-migration-test.db"
        const val FRESH_DATABASE_NAME = "steady-fresh-v2-test.db"

        val EXPECTED_HABITS = listOf(
            HabitRecord(
                id = "habit-evening",
                name = "Evening walk",
                recurrence = "DAILY",
                iconId = "steps",
                colorId = "warm",
                createdAtEpochMs = 1_725_814_800_000,
                updatedAtEpochMs = 1_725_814_800_000,
                createdZoneId = "America/Los_Angeles",
                goalNote = null,
                activeDaysMask = 127,
                scheduleType = "ROUTINE_CUE",
                exactTimeMinutes = null,
                routineCueId = "ANYTIME",
                archivedAtEpochMs = null,
                archivedZoneId = null,
            ),
            HabitRecord(
                id = "habit-morning",
                name = "Morning water",
                recurrence = "DAILY",
                iconId = "drop",
                colorId = "blue",
                createdAtEpochMs = 1_725_778_800_000,
                updatedAtEpochMs = 1_725_779_100_000,
                createdZoneId = "Asia/Kolkata",
                goalNote = null,
                activeDaysMask = 127,
                scheduleType = "ROUTINE_CUE",
                exactTimeMinutes = null,
                routineCueId = "ANYTIME",
                archivedAtEpochMs = null,
                archivedZoneId = null,
            ),
        )

        val EXPECTED_COMPLETIONS = listOf(
            CompletionRecord(
                id = "completion-earlier",
                habitId = "habit-morning",
                localDate = "2024-09-08",
                completedAtEpochMs = 1_725_790_200_000,
                completedZoneId = "Asia/Kolkata",
            ),
            CompletionRecord(
                id = "completion-later",
                habitId = "habit-evening",
                localDate = "2024-09-09",
                completedAtEpochMs = 1_725_929_400_000,
                completedZoneId = "America/Los_Angeles",
            ),
        )
    }
}
