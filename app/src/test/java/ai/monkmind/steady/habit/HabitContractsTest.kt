package ai.monkmind.steady.habit

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitContractsTest {

    @Test
    fun testDefaults_useStableLeafSageAndSevenHabitLimit() {
        assertEquals("leaf", DEFAULT_HABIT_ICON_ID)
        assertEquals("sage", DEFAULT_HABIT_COLOR_ID)
        assertEquals(127, DEFAULT_ACTIVE_DAYS.value)
        assertEquals(HabitSchedule.RoutineCue(Cue.Anytime), DEFAULT_HABIT_SCHEDULE)
        assertEquals(7, MAX_ACTIVE_HABITS)
        assertEquals(listOf(HabitRecurrence.Daily), HabitRecurrence.entries)
    }

    @Test
    fun testCatalogs_matchApprovedPrototypeChoicesAndStableMappings() {
        assertEquals(
            linkedSetOf("drop", "dumbbell", "bottle", "steps", "leaf"),
            HabitCatalog.habitIconIds,
        )
        assertEquals(
            linkedSetOf("blue", "sage", "lilac", "warm", "neutral"),
            HabitCatalog.habitColorIds,
        )
        assertEquals(
            linkedSetOf("leaf", "mountain", "moon", "sun", "lotus"),
            HabitCatalog.profileIconIds,
        )
        assertEquals(
            listOf(
                HabitCatalogStyle("drop", "blue"),
                HabitCatalogStyle("dumbbell", "sage"),
                HabitCatalogStyle("bottle", "lilac"),
                HabitCatalogStyle("steps", "warm"),
                HabitCatalogStyle("leaf", "neutral"),
            ),
            HabitCatalog.styles,
        )
    }

    @Test
    fun testCatalogSuggestion_isDeterministicAndFallsBackToLeafNeutral() {
        assertEquals(
            HabitCatalogStyle("drop", "blue"),
            HabitCatalog.suggestedStyle("Drink water"),
        )
        assertEquals(
            HabitCatalogStyle("dumbbell", "sage"),
            HabitCatalog.suggestedStyle("Strength workout"),
        )
        assertEquals(
            HabitCatalogStyle("bottle", "lilac"),
            HabitCatalog.suggestedStyle("Take vitamins"),
        )
        assertEquals(
            HabitCatalogStyle("steps", "warm"),
            HabitCatalog.suggestedStyle("Evening hike"),
        )
        assertEquals(
            HabitCatalogStyle("leaf", "neutral"),
            HabitCatalog.suggestedStyle("Read"),
        )
    }

    @Test
    fun testIdGenerator_returnsOpaqueStableValuesFromInjectedGenerator() {
        val generatedValues = ArrayDeque(listOf("habit-id", "completion-id"))
        val generator = HabitIdGenerator(generateValue = generatedValues::removeFirst)

        assertEquals(HabitId("habit-id"), generator.generateHabitId())
        assertEquals(
            HabitCompletionId("completion-id"),
            generator.generateCompletionId(),
        )
    }

    @Test
    fun testIdGenerator_defaultGenerator_returnsDistinctNonBlankOpaqueValues() {
        val generator = HabitIdGenerator()

        val first = generator.generateHabitId().value
        val second = generator.generateCompletionId().value

        assertTrue(first.isNotBlank())
        assertTrue(second.isNotBlank())
        assertNotEquals(first, second)
    }

    @Test
    fun testDailyHabit_completionControlsDerivedCompletedState() {
        val habit = habit()
        val date = LocalDate.parse("2026-09-13")
        val incomplete = DailyHabit(
            habit = habit,
            date = date,
            completion = null,
        )
        val complete = DailyHabit(
            habit = habit,
            date = date,
            completion = HabitCompletion(
                id = HabitCompletionId("completion-id"),
                habitId = habit.id,
                localDate = date,
                completedAt = Instant.parse("2026-09-13T08:30:00Z"),
                completedZoneId = ZoneId.of("Asia/Kolkata"),
            ),
        )

        assertFalse(incomplete.isCompleted)
        assertTrue(complete.isCompleted)
    }

    @Test
    fun testRepositoryResults_exposeEveryRequiredProductAndStorageOutcome() {
        val habit = habit()
        val completion = HabitCompletion(
            id = HabitCompletionId("completion-id"),
            habitId = habit.id,
            localDate = LocalDate.parse("2026-09-13"),
            completedAt = Instant.parse("2026-09-13T08:30:00Z"),
            completedZoneId = ZoneId.of("Asia/Kolkata"),
        )

        assertEquals(CreateHabitResult.Created(habit), CreateHabitResult.Created(habit))
        assertEquals(CreateHabitResult.BlankName, CreateHabitResult.BlankName)
        assertEquals(CreateHabitResult.ActiveLimitReached, CreateHabitResult.ActiveLimitReached)
        assertEquals(
            CreateHabitResult.Failure(HabitStoreFailure.WriteFailure),
            CreateHabitResult.Failure(HabitStoreFailure.WriteFailure),
        )
        assertEquals(
            ObserveTodayResult.Failure(HabitStoreFailure.ReadFailure),
            ObserveTodayResult.Failure(HabitStoreFailure.ReadFailure),
        )
        assertEquals(
            ObserveManagedHabitsResult.Success(
                active = listOf(habit),
                archived = emptyList(),
            ),
            ObserveManagedHabitsResult.Success(active = listOf(habit), archived = emptyList()),
        )
        assertEquals(ObserveHabitResult.Found(habit), ObserveHabitResult.Found(habit))
        assertEquals(ObserveHabitResult.HabitNotFound, ObserveHabitResult.HabitNotFound)
        assertEquals(UpdateHabitResult.Updated, UpdateHabitResult.Updated)
        assertEquals(UpdateHabitResult.HabitNotFound, UpdateHabitResult.HabitNotFound)
        assertEquals(
            CompleteHabitResult.Completed(completion),
            CompleteHabitResult.Completed(completion),
        )
        assertEquals(CompleteHabitResult.AlreadyCompleted, CompleteHabitResult.AlreadyCompleted)
        assertEquals(CompleteHabitResult.HabitNotFound, CompleteHabitResult.HabitNotFound)
        assertEquals(CompleteHabitResult.Archived, CompleteHabitResult.Archived)
        assertEquals(
            CompleteHabitResult.NotScheduledForDate,
            CompleteHabitResult.NotScheduledForDate,
        )
        assertEquals(
            CompleteHabitResult.Failure(HabitStoreFailure.InvalidStoredData),
            CompleteHabitResult.Failure(HabitStoreFailure.InvalidStoredData),
        )
        assertEquals(ArchiveHabitResult.Archived, ArchiveHabitResult.Archived)
        assertEquals(ArchiveHabitResult.AlreadyArchived, ArchiveHabitResult.AlreadyArchived)
        assertEquals(RestoreHabitResult.Restored, RestoreHabitResult.Restored)
        assertEquals(RestoreHabitResult.AlreadyActive, RestoreHabitResult.AlreadyActive)
        assertEquals(
            RestoreHabitResult.ActiveLimitReached,
            RestoreHabitResult.ActiveLimitReached,
        )
        assertEquals(DeleteHabitResult.Deleted, DeleteHabitResult.Deleted)
        assertEquals(DeleteHabitResult.HabitNotFound, DeleteHabitResult.HabitNotFound)
        assertEquals(ClearHabitDataResult.Cleared, ClearHabitDataResult.Cleared)
    }

    @Test
    fun testHabit_archiveMetadataIsPairedAndStableFieldsRemainAvailable() {
        val archivedAt = Instant.parse("2026-09-14T08:00:00Z")
        val archived = habit().copy(
            goalNote = "30 minutes",
            activeDays = ActiveDaysMask.of(
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.WEDNESDAY,
            ),
            schedule = HabitSchedule.ExactTime(450),
            archivedAt = archivedAt,
            archivedZoneId = ZoneId.of("Asia/Kolkata"),
        )

        assertTrue(archived.isArchived)
        assertEquals(HabitId("habit-id"), archived.id)
        assertEquals(archivedAt, archived.archivedAt)
        assertEquals(ZoneId.of("Asia/Kolkata"), archived.archivedZoneId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHabit_rejectsUnpairedArchiveMetadata() {
        habit().copy(archivedAt = Instant.parse("2026-09-14T08:00:00Z"))
    }

    private fun habit() = Habit(
        id = HabitId("habit-id"),
        name = "Morning walk",
        recurrence = HabitRecurrence.Daily,
        iconId = DEFAULT_HABIT_ICON_ID,
        colorId = DEFAULT_HABIT_COLOR_ID,
        createdAt = Instant.parse("2026-09-13T08:00:00Z"),
        updatedAt = Instant.parse("2026-09-13T08:00:00Z"),
        createdZoneId = ZoneId.of("Asia/Kolkata"),
    )
}
