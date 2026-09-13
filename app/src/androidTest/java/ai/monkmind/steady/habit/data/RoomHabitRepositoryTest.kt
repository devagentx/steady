package ai.monkmind.steady.habit.data

import ai.monkmind.steady.habit.ActiveDaysMask
import ai.monkmind.steady.habit.ArchiveHabitResult
import ai.monkmind.steady.habit.ClearHabitDataResult
import ai.monkmind.steady.habit.CompleteHabitResult
import ai.monkmind.steady.habit.CreateHabitResult
import ai.monkmind.steady.habit.Cue
import ai.monkmind.steady.habit.DEFAULT_HABIT_COLOR_ID
import ai.monkmind.steady.habit.DEFAULT_HABIT_ICON_ID
import ai.monkmind.steady.habit.DeleteHabitResult
import ai.monkmind.steady.habit.HabitDraft
import ai.monkmind.steady.habit.HabitCompletionId
import ai.monkmind.steady.habit.HabitId
import ai.monkmind.steady.habit.HabitIdGenerator
import ai.monkmind.steady.habit.HabitRecurrence
import ai.monkmind.steady.habit.HabitSchedule
import ai.monkmind.steady.habit.HabitStoreFailure
import ai.monkmind.steady.habit.ObserveHabitResult
import ai.monkmind.steady.habit.ObserveManagedHabitsResult
import ai.monkmind.steady.habit.ObserveTodayResult
import ai.monkmind.steady.habit.RestoreHabitResult
import ai.monkmind.steady.habit.UpdateHabitResult
import ai.monkmind.steady.reset.AppMutationMutex
import ai.monkmind.steady.time.LocalDateSnapshot
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomHabitRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: SteadyDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DATABASE_NAME)
        database = openDatabase()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
        if (::context.isInitialized) {
            context.deleteDatabase(TEST_DATABASE_NAME)
        }
    }

    @Test
    fun createDailyHabit_trimsOnceAndPersistsStableDefaultsAndSnapshot() = runBlocking {
        val repository = repositoryWithIds("habit-stable-id")

        val result = repository.createDailyHabit("  Morning walk  ", SNAPSHOT)
        val stored = database.habitDao()
            .observeScheduledHabits(DATE.toString(), SUNDAY_BIT)
            .first()
            .single()
            .habit

        val created = result as CreateHabitResult.Created
        assertEquals(HabitId("habit-stable-id"), created.habit.id)
        assertEquals("Morning walk", created.habit.name)
        assertEquals(HabitRecurrence.Daily, created.habit.recurrence)
        assertEquals(DEFAULT_HABIT_ICON_ID, created.habit.iconId)
        assertEquals(DEFAULT_HABIT_COLOR_ID, created.habit.colorId)
        assertEquals(SNAPSHOT.instant, created.habit.createdAt)
        assertEquals(SNAPSHOT.instant, created.habit.updatedAt)
        assertEquals(SNAPSHOT.zoneId, created.habit.createdZoneId)
        assertEquals("habit-stable-id", stored.id)
        assertEquals("Morning walk", stored.name)
        assertEquals("DAILY", stored.recurrence)
        assertEquals(DEFAULT_HABIT_ICON_ID, stored.iconId)
        assertEquals(DEFAULT_HABIT_COLOR_ID, stored.colorId)
        assertEquals(SNAPSHOT.instant.toEpochMilli(), stored.createdAtEpochMs)
        assertEquals(SNAPSHOT.instant.toEpochMilli(), stored.updatedAtEpochMs)
        assertEquals(SNAPSHOT.zoneId.id, stored.createdZoneId)
    }

    @Test
    fun createDailyHabit_blankOrIncoherentInputDoesNotPersistOrGenerateId() = runBlocking {
        var generatedIds = 0
        val repository = RoomHabitRepository(
            habitDao = database.habitDao(),
            idGenerator = HabitIdGenerator {
                generatedIds += 1
                "generated-$generatedIds"
            },
        )
        val incoherent = SNAPSHOT.copy(localDate = DATE.plusDays(1))

        assertEquals(CreateHabitResult.BlankName, repository.createDailyHabit(" \n ", SNAPSHOT))
        assertEquals(
            CreateHabitResult.Failure(HabitStoreFailure.WriteFailure),
            repository.createDailyHabit("Morning walk", incoherent),
        )
        assertEquals(0, generatedIds)
        assertEquals(
            ObserveTodayResult.Success(emptyList()),
            repository.observeToday(DATE).first(),
        )
    }

    @Test
    fun createDailyHabit_mapsLimitAndUnexpectedConstraintFailureExhaustively() = runBlocking {
        val generatedIds = ArrayDeque((1..7).map { "habit-$it" } + "habit-duplicate")
        val repository = RoomHabitRepository(
            habitDao = database.habitDao(),
            idGenerator = HabitIdGenerator(generatedIds::removeFirst),
        )

        repeat(7) {
            assertTrue(
                repository.createDailyHabit("Habit $it", SNAPSHOT) is
                    CreateHabitResult.Created,
            )
        }
        assertEquals(
            CreateHabitResult.ActiveLimitReached,
            repository.createDailyHabit("Eighth", SNAPSHOT),
        )

        database.close()
        context.deleteDatabase(TEST_DATABASE_NAME)
        database = openDatabase()
        val firstRepository = repositoryWithIds("shared-habit-id")
        assertTrue(firstRepository.createDailyHabit("First", SNAPSHOT) is CreateHabitResult.Created)
        val collisionRepository = repositoryWithIds("shared-habit-id")
        assertEquals(
            CreateHabitResult.Failure(HabitStoreFailure.WriteFailure),
            collisionRepository.createDailyHabit("Collision", SNAPSHOT),
        )
    }

    @Test
    fun observeToday_mapsRowsAndConvertsMalformedStorageToTypedFailure() = runBlocking {
        val repository = repositoryWithIds("habit-id")
        repository.createDailyHabit("Morning walk", SNAPSHOT)

        val success = repository.observeToday(DATE).first() as ObserveTodayResult.Success
        assertEquals(listOf("Morning walk"), success.habits.map { it.habit.name })
        assertEquals(listOf(false), success.habits.map { it.isCompleted })

        database.openHelper.writableDatabase.execSQL(
            "UPDATE habits SET icon_id = 'unsupported' WHERE id = 'habit-id'",
        )

        assertEquals(
            ObserveTodayResult.Failure(HabitStoreFailure.InvalidStoredData),
            repository.observeToday(DATE).first(),
        )
    }

    @Test
    fun observeToday_databaseFailureIsTypedAndDoesNotLeakRoomException() = runBlocking {
        val repository = repositoryWithIds("habit-id")
        database.close()

        assertEquals(
            ObserveTodayResult.Failure(HabitStoreFailure.ReadFailure),
            repository.observeToday(DATE).first(),
        )
    }

    @Test
    fun completeHabit_mapsMissingDuplicateAndInsertedWhilePreservingFirstHistory() = runBlocking {
        assertEquals(
            CompleteHabitResult.HabitNotFound,
            repositoryWithIds("missing-completion-id").completeHabit(
                HabitId("missing"),
                SNAPSHOT,
            ),
        )
        val repository = repositoryWithIds(
            "habit-id",
            "completion-first",
            "completion-duplicate",
        )
        repository.createDailyHabit("Morning walk", SNAPSHOT)

        val firstResult = repository.completeHabit(HabitId("habit-id"), SNAPSHOT)
        val duplicateSnapshot = LocalDateSnapshot(
            instant = Instant.parse("2026-09-13T10:30:00Z"),
            zoneId = ZoneId.of("UTC"),
            localDate = DATE,
        )
        val duplicateResult = repository.completeHabit(HabitId("habit-id"), duplicateSnapshot)

        assertEquals(
            CompleteHabitResult.Completed(
                ai.monkmind.steady.habit.HabitCompletion(
                    id = HabitCompletionId("completion-first"),
                    habitId = HabitId("habit-id"),
                    localDate = DATE,
                    completedAt = SNAPSHOT.instant,
                    completedZoneId = SNAPSHOT.zoneId,
                ),
            ),
            firstResult,
        )
        assertEquals(CompleteHabitResult.AlreadyCompleted, duplicateResult)
        val stored = database.habitDao()
            .observeScheduledHabits(DATE.toString(), SUNDAY_BIT)
            .first()
            .single()
            .completion
        assertEquals("completion-first", stored?.id)
        assertEquals(SNAPSHOT.instant.toEpochMilli(), stored?.completedAtEpochMs)
        assertEquals(SNAPSHOT.zoneId.id, stored?.completedZoneId)
    }

    @Test
    fun completeHabit_rejectsInvalidIdAndIncoherentSnapshotWithoutGeneratingId() = runBlocking {
        var generatedIds = 0
        val repository = RoomHabitRepository(
            habitDao = database.habitDao(),
            idGenerator = HabitIdGenerator {
                generatedIds += 1
                "completion-$generatedIds"
            },
        )

        assertEquals(
            CompleteHabitResult.Failure(HabitStoreFailure.WriteFailure),
            repository.completeHabit(HabitId(" \t "), SNAPSHOT),
        )
        assertEquals(
            CompleteHabitResult.Failure(HabitStoreFailure.WriteFailure),
            repository.completeHabit(
                HabitId("habit-id"),
                SNAPSHOT.copy(localDate = DATE.plusDays(1)),
            ),
        )
        assertEquals(0, generatedIds)
    }

    @Test
    fun generatedBlankIdsBecomeTypedWriteFailuresWithoutPersistence() = runBlocking {
        val repository = repositoryWithIds(" ", "habit-id", "\t")

        assertEquals(
            CreateHabitResult.Failure(HabitStoreFailure.WriteFailure),
            repository.createDailyHabit("Morning walk", SNAPSHOT),
        )
        assertEquals(
            CreateHabitResult.Created::class,
            repository.createDailyHabit("Morning walk", SNAPSHOT)::class,
        )
        assertEquals(
            CompleteHabitResult.Failure(HabitStoreFailure.WriteFailure),
            repository.completeHabit(HabitId("habit-id"), SNAPSHOT),
        )
        val row = database.habitDao()
            .observeScheduledHabits(DATE.toString(), SUNDAY_BIT)
            .first()
            .single()
        assertEquals("habit-id", row.habit.id)
        assertNull(row.completion)
    }

    @Test
    fun createdHabitAndCompletionSurviveDatabaseCloseAndReopen() = runBlocking {
        val repository = repositoryWithIds("habit-id", "completion-id")
        repository.createDailyHabit("Morning walk", SNAPSHOT)
        repository.completeHabit(HabitId("habit-id"), SNAPSHOT)

        database.close()
        database = openDatabase()
        val reopenedRepository = repositoryWithIds("unused")
        val result = reopenedRepository.observeToday(DATE).first() as ObserveTodayResult.Success

        assertEquals(1, result.habits.size)
        assertEquals("habit-id", result.habits.single().habit.id.value)
        assertEquals("completion-id", result.habits.single().completion?.id?.value)
    }

    @Test
    fun scheduledCrudLifecyclePreservesStableIdentityAndHistory() = runBlocking {
        val repository = repositoryWithIds(
            "habit-id",
            "completion-id",
            "archived-attempt-id",
        )
        val draft = draft(
            name = "  Read  ",
            note = "  Ten pages  ",
            activeDays = ActiveDaysMask.of(DATE.dayOfWeek),
            schedule = HabitSchedule.ExactTime(480),
        )

        val created = repository.createHabit(draft, SNAPSHOT) as CreateHabitResult.Created
        assertEquals("Read", created.habit.name)
        assertEquals("Ten pages", created.habit.goalNote)
        assertEquals(HabitSchedule.ExactTime(480), created.habit.schedule)
        assertEquals(
            listOf(HabitId("habit-id")),
            (repository.observeToday(DATE).first() as ObserveTodayResult.Success)
                .habits
                .map { it.habit.id },
        )
        assertTrue(
            repository.completeHabit(HabitId("habit-id"), SNAPSHOT) is
                CompleteHabitResult.Completed,
        )

        val updatedAt = SNAPSHOT.copy(instant = Instant.parse("2026-09-13T09:00:00Z"))
        assertEquals(
            UpdateHabitResult.Updated,
            repository.updateHabit(
                id = HabitId("habit-id"),
                draft = draft(
                    name = "Read calmly",
                    note = null,
                    activeDays = ActiveDaysMask.EveryDay,
                    schedule = HabitSchedule.RoutineCue(Cue.BeforeBed),
                ),
                time = updatedAt,
            ),
        )
        val updated = (repository.observeHabit(HabitId("habit-id")).first() as
            ObserveHabitResult.Found).habit
        assertEquals(HabitId("habit-id"), updated.id)
        assertEquals(SNAPSHOT.instant, updated.createdAt)
        assertEquals(SNAPSHOT.zoneId, updated.createdZoneId)
        assertEquals(updatedAt.instant, updated.updatedAt)

        val archivedAt = SNAPSHOT.copy(instant = Instant.parse("2026-09-13T10:00:00Z"))
        assertEquals(
            ArchiveHabitResult.Archived,
            repository.archiveHabit(HabitId("habit-id"), archivedAt),
        )
        assertEquals(
            ArchiveHabitResult.AlreadyArchived,
            repository.archiveHabit(HabitId("habit-id"), archivedAt),
        )
        assertEquals(
            CompleteHabitResult.Archived,
            repository.completeHabit(HabitId("habit-id"), archivedAt),
        )
        val managed = repository.observeManagedHabits().first() as
            ObserveManagedHabitsResult.Success
        assertTrue(managed.active.isEmpty())
        assertEquals(listOf(HabitId("habit-id")), managed.archived.map { it.id })

        val restoredAt = SNAPSHOT.copy(instant = Instant.parse("2026-09-13T11:00:00Z"))
        assertEquals(
            RestoreHabitResult.Restored,
            repository.restoreHabit(HabitId("habit-id"), restoredAt),
        )
        assertEquals(
            RestoreHabitResult.AlreadyActive,
            repository.restoreHabit(HabitId("habit-id"), restoredAt),
        )
        val restoredToday = repository.observeToday(DATE).first() as ObserveTodayResult.Success
        assertEquals("completion-id", restoredToday.habits.single().completion?.id?.value)

        assertEquals(
            DeleteHabitResult.Deleted,
            repository.deleteHabitAndHistory(HabitId("habit-id")),
        )
        assertEquals(
            DeleteHabitResult.HabitNotFound,
            repository.deleteHabitAndHistory(HabitId("habit-id")),
        )
        assertEquals(
            ObserveHabitResult.HabitNotFound,
            repository.observeHabit(HabitId("habit-id")).first(),
        )
    }

    @Test
    fun completionRequiresCurrentWeekdayEligibility() = runBlocking {
        val repository = repositoryWithIds("habit-id", "completion-id")
        repository.createHabit(
            draft(
                name = "Monday habit",
                activeDays = ActiveDaysMask.of(java.time.DayOfWeek.MONDAY),
            ),
            SNAPSHOT,
        )

        assertEquals(
            CompleteHabitResult.NotScheduledForDate,
            repository.completeHabit(HabitId("habit-id"), SNAPSHOT),
        )
        assertTrue(
            database.openHelper.writableDatabase
                .query("SELECT id FROM habit_completions")
                .use { cursor -> !cursor.moveToFirst() },
        )
    }

    @Test
    fun managedAndSingleReadsConvertMalformedStorageToTypedFailures() = runBlocking {
        val repository = repositoryWithIds("habit-id")
        repository.createDailyHabit("Morning walk", SNAPSHOT)
        database.openHelper.writableDatabase.execSQL(
            "UPDATE habits SET active_days_mask = 0 WHERE id = 'habit-id'",
        )

        assertEquals(
            ObserveManagedHabitsResult.Failure(HabitStoreFailure.InvalidStoredData),
            repository.observeManagedHabits().first(),
        )
        assertEquals(
            ObserveHabitResult.Failure(HabitStoreFailure.InvalidStoredData),
            repository.observeHabit(HabitId("habit-id")).first(),
        )
    }

    @Test
    fun sharedMutationMutexAndRoomTransactionKeepConcurrentRestoreAtCapacity() = runBlocking {
        val dao = database.habitDao()
        repeat(6) { index ->
            dao.insertHabitWithinLimit(entity("active-$index"), limit = 7)
        }
        listOf("archived-a", "archived-b").forEach { id ->
            dao.insertHabitWithinLimit(
                entity(
                    id = id,
                    archivedAtEpochMs = SNAPSHOT.instant.toEpochMilli(),
                    archivedZoneId = SNAPSHOT.zoneId.id,
                ),
                limit = 7,
            )
        }
        val mutex = AppMutationMutex()
        val firstRepository = RoomHabitRepository(dao, mutationMutex = mutex)
        val secondRepository = RoomHabitRepository(dao, mutationMutex = mutex)

        val outcomes = coroutineScope {
            listOf(
                async { firstRepository.restoreHabit(HabitId("archived-a"), SNAPSHOT) },
                async { secondRepository.restoreHabit(HabitId("archived-b"), SNAPSHOT) },
            ).awaitAll()
        }

        assertEquals(1, outcomes.count { it == RestoreHabitResult.Restored })
        assertEquals(1, outcomes.count { it == RestoreHabitResult.ActiveLimitReached })
        val managed = firstRepository.observeManagedHabits().first() as
            ObserveManagedHabitsResult.Success
        assertEquals(7, managed.active.size)
        assertEquals(1, managed.archived.size)
    }

    @Test
    fun clearAllHabitDataRemovesActiveArchivedAndCompletionRows() = runBlocking {
        val repository = repositoryWithIds("active", "completion", "archived")
        repository.createDailyHabit("Active", SNAPSHOT)
        repository.completeHabit(HabitId("active"), SNAPSHOT)
        repository.createDailyHabit("Archived", SNAPSHOT)
        repository.archiveHabit(HabitId("archived"), SNAPSHOT)

        assertEquals(ClearHabitDataResult.Cleared, repository.clearAllHabitData())

        val managed = repository.observeManagedHabits().first() as
            ObserveManagedHabitsResult.Success
        assertTrue(managed.active.isEmpty())
        assertTrue(managed.archived.isEmpty())
        assertTrue(
            database.openHelper.writableDatabase
                .query("SELECT id FROM habit_completions")
                .use { cursor -> !cursor.moveToFirst() },
        )
    }

    @Test
    fun repositoryRethrowsCancellationFromObservationAndMutations() {
        val cancellation = CancellationException("cancelled")
        val repository = RoomHabitRepository(
            habitDao = CancellingHabitDao(cancellation),
            idGenerator = HabitIdGenerator { "stable-id" },
        )

        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.observeToday(DATE).first()
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.createDailyHabit("Morning walk", SNAPSHOT)
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.completeHabit(HabitId("habit-id"), SNAPSHOT)
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.observeManagedHabits().first()
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.observeHabit(HabitId("habit-id")).first()
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.updateHabit(HabitId("habit-id"), draft("Updated"), SNAPSHOT)
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.archiveHabit(HabitId("habit-id"), SNAPSHOT)
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.restoreHabit(HabitId("habit-id"), SNAPSHOT)
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.deleteHabitAndHistory(HabitId("habit-id"))
            }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.clearAllHabitData()
            }
        }
    }

    private fun repositoryWithIds(vararg ids: String): RoomHabitRepository {
        val generatedIds = ArrayDeque(ids.toList())
        return RoomHabitRepository(
            habitDao = database.habitDao(),
            idGenerator = HabitIdGenerator(generatedIds::removeFirst),
        )
    }

    private fun openDatabase(): SteadyDatabase =
        Room.databaseBuilder(
            context,
            SteadyDatabase::class.java,
            TEST_DATABASE_NAME,
        ).build()

    private fun draft(
        name: String,
        note: String? = null,
        activeDays: ActiveDaysMask = ActiveDaysMask.EveryDay,
        schedule: HabitSchedule = HabitSchedule.Anytime,
    ): HabitDraft = HabitDraft(
        name = name,
        goalNote = note,
        activeDays = activeDays,
        schedule = schedule,
        iconId = DEFAULT_HABIT_ICON_ID,
        colorId = DEFAULT_HABIT_COLOR_ID,
    )

    private fun entity(
        id: String,
        archivedAtEpochMs: Long? = null,
        archivedZoneId: String? = null,
    ): HabitEntity = HabitEntity(
        id = id,
        name = id,
        recurrence = "DAILY",
        iconId = DEFAULT_HABIT_ICON_ID,
        colorId = DEFAULT_HABIT_COLOR_ID,
        createdAtEpochMs = SNAPSHOT.instant.toEpochMilli(),
        updatedAtEpochMs = SNAPSHOT.instant.toEpochMilli(),
        createdZoneId = SNAPSHOT.zoneId.id,
        archivedAtEpochMs = archivedAtEpochMs,
        archivedZoneId = archivedZoneId,
    )

    private class CancellingHabitDao(
        private val cancellation: CancellationException,
    ) : HabitDao() {
        override fun observeScheduledHabits(
            localDate: String,
            weekdayBit: Int,
        ) = flow<List<DailyHabitRow>> {
            throw cancellation
        }

        override fun observeManagedHabits() = flow<List<HabitEntity>> {
            throw cancellation
        }

        override fun observeHabit(habitId: String) = flow<HabitEntity?> {
            throw cancellation
        }

        override suspend fun countActiveHabits(): Int = throw cancellation

        override suspend fun insertHabit(entity: HabitEntity) = throw cancellation

        override suspend fun habitById(habitId: String): HabitEntity? = throw cancellation

        override suspend fun updateMutableFields(
            habitId: String,
            name: String,
            goalNote: String?,
            activeDaysMask: Int,
            scheduleType: String,
            exactTimeMinutes: Int?,
            routineCueId: String?,
            iconId: String,
            colorId: String,
            updatedAtEpochMs: Long,
        ): Int = throw cancellation

        override suspend fun setArchived(
            habitId: String,
            archivedAtEpochMs: Long?,
            archivedZoneId: String?,
            updatedAtEpochMs: Long,
        ): Int = throw cancellation

        override suspend fun completionForDate(
            habitId: String,
            localDate: String,
        ): HabitCompletionEntity? = throw cancellation

        override suspend fun insertCompletion(entity: HabitCompletionEntity) = throw cancellation

        override suspend fun deleteCompletionsForHabit(habitId: String) = throw cancellation

        override suspend fun deleteHabit(habitId: String): Int = throw cancellation

        override suspend fun deleteAllCompletions() = throw cancellation

        override suspend fun deleteAllHabits() = throw cancellation
    }

    private companion object {
        const val TEST_DATABASE_NAME = "steady-room-repository-test.db"
        const val SUNDAY_BIT = 1 shl 6
        val DATE: LocalDate = LocalDate.parse("2026-09-13")
        val SNAPSHOT = LocalDateSnapshot(
            instant = Instant.parse("2026-09-13T08:30:00Z"),
            zoneId = ZoneId.of("Asia/Kolkata"),
            localDate = DATE,
        )
    }
}
