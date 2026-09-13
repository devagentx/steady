package ai.monkmind.steady.habit.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {
    private lateinit var database: SteadyDatabase
    private lateinit var dao: HabitDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SteadyDatabase::class.java,
        ).build()
        dao = database.habitDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeScheduledHabits_filtersEveryWeekdayAndArchivedRows() = runBlocking {
        DayOfWeek.entries.forEachIndexed { index, day ->
            dao.insertHabitWithinLimit(
                habit(
                    id = day.name.lowercase(),
                    activeDaysMask = 1 shl index,
                ),
                limit = 7,
            )
        }
        dao.archiveHabit(
            habitId = DayOfWeek.WEDNESDAY.name.lowercase(),
            archivedAtEpochMs = 2_000,
            archivedZoneId = "UTC",
        )

        DayOfWeek.entries.forEachIndexed { index, day ->
            val rows = dao.observeScheduledHabits(
                localDate = LocalDate.of(2026, 9, 14).plusDays(index.toLong()).toString(),
                weekdayBit = 1 shl index,
            ).first()
            val expected = if (day == DayOfWeek.WEDNESDAY) emptyList() else {
                listOf(day.name.lowercase())
            }
            assertEquals(expected, rows.map { it.habit.id })
        }
    }

    @Test
    fun observeScheduledHabits_joinsRequestedDateAndOrdersCompletionTimeCueAndTies() =
        runBlocking {
            val habits = listOf(
                habit(id = "time-late", scheduleType = "EXACT_TIME", exactTimeMinutes = 600),
                habit(id = "cue-anytime", routineCueId = "ANYTIME"),
                habit(id = "cue-waking", routineCueId = "AFTER_WAKING"),
                habit(id = "cue-breakfast", routineCueId = "AFTER_BREAKFAST"),
                habit(id = "cue-lunch", routineCueId = "AFTER_LUNCH"),
                habit(id = "cue-dinner", routineCueId = "AFTER_DINNER"),
                habit(id = "cue-bed", routineCueId = "BEFORE_BED"),
                habit(id = "time-early-b", scheduleType = "EXACT_TIME", exactTimeMinutes = 480),
                habit(id = "time-early-a", scheduleType = "EXACT_TIME", exactTimeMinutes = 480),
                habit(id = "completed-time", scheduleType = "EXACT_TIME", exactTimeMinutes = 300),
            )
            habits.forEach { dao.insertHabitWithinLimit(it, limit = 10) }
            dao.insertCompletionIfEligible(
                completion(id = "completion-today", habitId = "completed-time"),
                weekdayBit = SUNDAY_BIT,
            )
            dao.insertCompletionIfEligible(
                completion(
                    id = "completion-other-date",
                    habitId = "time-late",
                    localDate = DATE_TWO,
                ),
                weekdayBit = MONDAY_BIT,
            )

            val rows = dao.observeScheduledHabits(DATE_ONE, SUNDAY_BIT).first()

            assertEquals(
                listOf(
                    "time-early-a",
                    "time-early-b",
                    "time-late",
                    "cue-waking",
                    "cue-breakfast",
                    "cue-lunch",
                    "cue-dinner",
                    "cue-bed",
                    "cue-anytime",
                    "completed-time",
                ),
                rows.map { it.habit.id },
            )
            assertNull(rows.single { it.habit.id == "time-late" }.completion)
        }

    @Test
    fun createCountsOnlyActiveRowsAndArchiveFreesCapacity() = runBlocking {
        dao.insertHabitWithinLimit(
            habit(
                id = "archived",
                archivedAtEpochMs = 2_000,
                archivedZoneId = "UTC",
            ),
            limit = 7,
        )
        repeat(7) { index ->
            assertEquals(
                InsertHabitOutcome.Inserted,
                dao.insertHabitWithinLimit(habit(id = "active-$index"), limit = 7),
            )
        }

        assertEquals(
            InsertHabitOutcome.LimitReached,
            dao.insertHabitWithinLimit(habit(id = "blocked"), limit = 7),
        )
        assertEquals(
            ArchiveHabitOutcome.Archived,
            dao.archiveHabit("active-0", 3_000, "UTC"),
        )
        assertEquals(
            InsertHabitOutcome.Inserted,
            dao.insertHabitWithinLimit(habit(id = "replacement"), limit = 7),
        )
        assertEquals(9, dao.observeManagedHabits().first().size)
    }

    @Test
    fun concurrentRestoreFromSixAllowsOnlyOneSeventhActiveHabit() = runBlocking {
        repeat(6) { index ->
            dao.insertHabitWithinLimit(habit(id = "active-$index"), limit = 7)
        }
        listOf("archived-a", "archived-b").forEach { id ->
            dao.insertHabitWithinLimit(
                habit(id = id, archivedAtEpochMs = 2_000, archivedZoneId = "UTC"),
                limit = 7,
            )
        }

        val outcomes = coroutineScope {
            listOf("archived-a", "archived-b")
                .map { id ->
                    async {
                        dao.restoreHabitWithinLimit(id, limit = 7, updatedAtEpochMs = 3_000)
                    }
                }
                .awaitAll()
        }

        assertEquals(1, outcomes.count { it == RestoreHabitOutcome.Restored })
        assertEquals(1, outcomes.count { it == RestoreHabitOutcome.LimitReached })
        assertEquals(7, dao.observeManagedHabits().first().count { it.archivedAtEpochMs == null })
    }

    @Test
    fun restoreChecksIdempotencyBeforeCapacity() = runBlocking {
        repeat(7) { index ->
            dao.insertHabitWithinLimit(habit(id = "active-$index"), limit = 7)
        }

        assertEquals(
            RestoreHabitOutcome.AlreadyActive,
            dao.restoreHabitWithinLimit("active-0", limit = 7, updatedAtEpochMs = 3_000),
        )
    }

    @Test
    fun archiveAndRestorePreserveCompletionHistoryAndStableMetadata() = runBlocking {
        val original = habit(id = "habit-a", createdAtEpochMs = 500)
        val completion = completion(id = "completion-a", habitId = original.id)
        dao.insertHabitWithinLimit(original, limit = 7)
        dao.insertCompletionIfEligible(completion, weekdayBit = SUNDAY_BIT)

        assertEquals(ArchiveHabitOutcome.Archived, dao.archiveHabit("habit-a", 2_000, "UTC"))
        assertTrue(dao.observeScheduledHabits(DATE_ONE, SUNDAY_BIT).first().isEmpty())
        assertEquals(
            RestoreHabitOutcome.Restored,
            dao.restoreHabitWithinLimit("habit-a", limit = 7, updatedAtEpochMs = 3_000),
        )

        val restored = dao.observeScheduledHabits(DATE_ONE, SUNDAY_BIT).first().single()
        assertEquals(original.id, restored.habit.id)
        assertEquals(original.createdAtEpochMs, restored.habit.createdAtEpochMs)
        assertEquals(original.createdZoneId, restored.habit.createdZoneId)
        assertEquals(completion, restored.completion)
        assertNull(restored.habit.archivedAtEpochMs)
        assertNull(restored.habit.archivedZoneId)
    }

    @Test
    fun updateChangesOnlyMutableFieldsAndPreservesHistoryAndLifecycle() = runBlocking {
        val original = habit(
            id = "habit-a",
            createdAtEpochMs = 500,
            archivedAtEpochMs = 2_000,
            archivedZoneId = "Asia/Kolkata",
        )
        dao.insertHabitWithinLimit(original, limit = 7)
        dao.restoreHabitWithinLimit("habit-a", limit = 7, updatedAtEpochMs = 2_500)
        val completion = completion(id = "completion-a", habitId = original.id)
        dao.insertCompletionIfEligible(completion, weekdayBit = SUNDAY_BIT)
        dao.archiveHabit("habit-a", 3_000, "Asia/Kolkata")

        assertEquals(
            UpdateHabitOutcome.Updated,
            dao.updateHabitFields(
                HabitUpdateFields(
                    id = "habit-a",
                    name = "Edited",
                    goalNote = "Read ten pages",
                    activeDaysMask = MONDAY_BIT,
                    scheduleType = "EXACT_TIME",
                    exactTimeMinutes = 480,
                    routineCueId = null,
                    iconId = "drop",
                    colorId = "blue",
                    updatedAtEpochMs = 4_000,
                ),
            ),
        )

        val updated = dao.observeManagedHabits().first().single()
        assertEquals("habit-a", updated.id)
        assertEquals(500, updated.createdAtEpochMs)
        assertEquals("UTC", updated.createdZoneId)
        assertEquals(3_000L, updated.archivedAtEpochMs)
        assertEquals("Asia/Kolkata", updated.archivedZoneId)
        assertEquals("Edited", updated.name)
        assertEquals(4_000L, updated.updatedAtEpochMs)
        assertEquals(
            "completion-a",
            database.openHelper.writableDatabase
                .query("SELECT id FROM habit_completions WHERE habit_id = 'habit-a'")
                .use { cursor ->
                    check(cursor.moveToFirst())
                    cursor.getString(0)
                },
        )
    }

    @Test
    fun completionRejectsArchivedAndUnscheduledHabitsWithoutWriting() = runBlocking {
        dao.insertHabitWithinLimit(
            habit(id = "archived", archivedAtEpochMs = 2_000, archivedZoneId = "UTC"),
            limit = 7,
        )
        dao.insertHabitWithinLimit(
            habit(id = "monday-only", activeDaysMask = MONDAY_BIT),
            limit = 7,
        )

        assertEquals(
            InsertCompletionOutcome.Archived,
            dao.insertCompletionIfEligible(
                completion(id = "archived-completion", habitId = "archived"),
                weekdayBit = SUNDAY_BIT,
            ),
        )
        assertEquals(
            InsertCompletionOutcome.NotScheduledForDate,
            dao.insertCompletionIfEligible(
                completion(id = "unscheduled-completion", habitId = "monday-only"),
                weekdayBit = SUNDAY_BIT,
            ),
        )
        assertTrue(
            database.openHelper.writableDatabase
                .query("SELECT id FROM habit_completions")
                .use { cursor -> !cursor.moveToFirst() },
        )
    }

    @Test
    fun deleteRemovesSelectedChildrenBeforeParentWithoutAffectingOtherRows() = runBlocking {
        listOf("habit-a", "habit-b").forEach { id ->
            dao.insertHabitWithinLimit(habit(id = id), limit = 7)
            dao.insertCompletionIfEligible(
                completion(id = "completion-$id", habitId = id),
                weekdayBit = SUNDAY_BIT,
            )
        }

        assertEquals(DeleteHabitOutcome.Deleted, dao.deleteHabitAndHistory("habit-a"))

        assertEquals(listOf("habit-b"), dao.observeManagedHabits().first().map { it.id })
        assertEquals(
            "completion-habit-b",
            dao.observeScheduledHabits(DATE_ONE, SUNDAY_BIT).first().single().completion?.id,
        )
        assertEquals(DeleteHabitOutcome.HabitNotFound, dao.deleteHabitAndHistory("habit-a"))
    }

    @Test
    fun clearAllHabitDataDeletesChildrenThenParents() = runBlocking {
        dao.insertHabitWithinLimit(habit(id = "habit-a"), limit = 7)
        dao.insertCompletionIfEligible(
            completion(id = "completion-a", habitId = "habit-a"),
            weekdayBit = SUNDAY_BIT,
        )

        dao.clearAllHabitData()

        assertTrue(dao.observeManagedHabits().first().isEmpty())
        assertTrue(
            database.openHelper.writableDatabase
                .query("SELECT id FROM habit_completions")
                .use { cursor -> !cursor.moveToFirst() },
        )
    }

    private fun habit(
        id: String,
        createdAtEpochMs: Long = 1_000,
        activeDaysMask: Int = 127,
        scheduleType: String = "ROUTINE_CUE",
        exactTimeMinutes: Int? = null,
        routineCueId: String? = if (scheduleType == "ROUTINE_CUE") "ANYTIME" else null,
        archivedAtEpochMs: Long? = null,
        archivedZoneId: String? = null,
    ) = HabitEntity(
        id = id,
        name = "Habit $id",
        recurrence = "DAILY",
        iconId = "leaf",
        colorId = "sage",
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = createdAtEpochMs,
        createdZoneId = "UTC",
        activeDaysMask = activeDaysMask,
        scheduleType = scheduleType,
        exactTimeMinutes = exactTimeMinutes,
        routineCueId = routineCueId,
        archivedAtEpochMs = archivedAtEpochMs,
        archivedZoneId = archivedZoneId,
    )

    private fun completion(
        id: String,
        habitId: String,
        localDate: String = DATE_ONE,
    ) = HabitCompletionEntity(
        id = id,
        habitId = habitId,
        localDate = localDate,
        completedAtEpochMs = 2_000,
        completedZoneId = "UTC",
    )

    private companion object {
        const val DATE_ONE = "2026-09-13"
        const val DATE_TWO = "2026-09-14"
        const val MONDAY_BIT = 1 shl 0
        const val SUNDAY_BIT = 1 shl 6
    }
}
