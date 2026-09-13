package ai.monkmind.steady.habit

import ai.monkmind.steady.habit.data.DailyHabitRow
import ai.monkmind.steady.habit.data.HabitCompletionEntity
import ai.monkmind.steady.habit.data.HabitEntity
import ai.monkmind.steady.habit.data.HabitEntityMapper
import ai.monkmind.steady.habit.data.StoredHabitMappingResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitEntityMapperTest {
    private val mapper = HabitEntityMapper()
    private val requestedDate = LocalDate.parse("2026-09-13")

    @Test
    fun testMap_validIncompleteRow_mapsEveryHabitField() {
        val result = mapper.map(
            row = DailyHabitRow(
                habit = habitEntity(),
                completion = null,
            ),
            requestedDate = requestedDate,
        )

        assertEquals(
            StoredHabitMappingResult.Success(
                DailyHabit(
                    habit = habit(),
                    date = requestedDate,
                    completion = null,
                ),
            ),
            result,
        )
    }

    @Test
    fun testMap_validExactTimeAndArchivedHabit_mapsEveryR1Field() {
        val entity = habitEntity().copy(
            goalNote = "Read 20 pages",
            activeDaysMask = ActiveDaysMask.of(
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.FRIDAY,
            ).value,
            scheduleType = "EXACT_TIME",
            exactTimeMinutes = 7 * 60 + 30,
            routineCueId = null,
            iconId = HabitCatalog.BOTTLE_ICON_ID,
            colorId = HabitCatalog.LILAC_COLOR_ID,
            updatedAtEpochMs = Instant.parse("2026-09-14T08:00:00Z").toEpochMilli(),
            archivedAtEpochMs = Instant.parse("2026-09-14T08:00:00Z").toEpochMilli(),
            archivedZoneId = "Europe/Paris",
        )

        assertEquals(
            StoredHabitMappingResult.Success(
                habit().copy(
                    goalNote = "Read 20 pages",
                    activeDays = ActiveDaysMask.of(
                        java.time.DayOfWeek.MONDAY,
                        java.time.DayOfWeek.WEDNESDAY,
                        java.time.DayOfWeek.FRIDAY,
                    ),
                    schedule = HabitSchedule.ExactTime(7 * 60 + 30),
                    iconId = HabitCatalog.BOTTLE_ICON_ID,
                    colorId = HabitCatalog.LILAC_COLOR_ID,
                    updatedAt = Instant.parse("2026-09-14T08:00:00Z"),
                    archivedAt = Instant.parse("2026-09-14T08:00:00Z"),
                    archivedZoneId = ZoneId.of("Europe/Paris"),
                ),
            ),
            mapper.mapHabit(entity),
        )
    }

    @Test
    fun testMap_validCompletedRow_mapsEveryCompletionField() {
        val completion = HabitCompletion(
            id = HabitCompletionId("completion-id"),
            habitId = HabitId("habit-id"),
            localDate = requestedDate,
            completedAt = Instant.parse("2026-09-13T08:30:00Z"),
            completedZoneId = ZoneId.of("Asia/Kolkata"),
        )

        assertEquals(
            StoredHabitMappingResult.Success(
                DailyHabit(
                    habit = habit(),
                    date = requestedDate,
                    completion = completion,
                ),
            ),
            mapper.map(
                row = DailyHabitRow(
                    habit = habitEntity(),
                    completion = completionEntity(),
                ),
                requestedDate = requestedDate,
            ),
        )
    }

    @Test
    fun testMap_rejectsInvalidHabitSemanticFields() {
        val invalidEntities = listOf(
            habitEntity().copy(id = ""),
            habitEntity().copy(id = " habit-id"),
            habitEntity().copy(name = " \n "),
            habitEntity().copy(name = " Morning walk "),
            habitEntity().copy(recurrence = "WEEKLY"),
            habitEntity().copy(iconId = "seedling"),
            habitEntity().copy(colorId = "teal"),
            habitEntity().copy(goalNote = ""),
            habitEntity().copy(goalNote = " Untrimmed"),
            habitEntity().copy(activeDaysMask = 0),
            habitEntity().copy(activeDaysMask = 128),
            habitEntity().copy(scheduleType = "AT_TIME"),
            habitEntity().copy(
                scheduleType = "EXACT_TIME",
                exactTimeMinutes = null,
                routineCueId = null,
            ),
            habitEntity().copy(
                scheduleType = "EXACT_TIME",
                exactTimeMinutes = -1,
                routineCueId = null,
            ),
            habitEntity().copy(
                scheduleType = "EXACT_TIME",
                exactTimeMinutes = 1_440,
                routineCueId = null,
            ),
            habitEntity().copy(
                scheduleType = "EXACT_TIME",
                exactTimeMinutes = 480,
                routineCueId = "ANYTIME",
            ),
            habitEntity().copy(
                scheduleType = "ROUTINE_CUE",
                exactTimeMinutes = 480,
                routineCueId = "ANYTIME",
            ),
            habitEntity().copy(routineCueId = null),
            habitEntity().copy(routineCueId = "MORNING"),
            habitEntity().copy(createdAtEpochMs = -1),
            habitEntity().copy(updatedAtEpochMs = 0),
            habitEntity().copy(createdZoneId = "Not/AZone"),
            habitEntity().copy(archivedAtEpochMs = 1_000),
            habitEntity().copy(archivedZoneId = "UTC"),
            habitEntity().copy(archivedAtEpochMs = -1, archivedZoneId = "UTC"),
            habitEntity().copy(archivedAtEpochMs = 1_000, archivedZoneId = "Not/AZone"),
        )

        invalidEntities.forEach { entity ->
            assertInvalid(DailyHabitRow(habit = entity, completion = null))
        }
    }

    @Test
    fun testMap_rejectsInvalidCompletionSemanticFields() {
        val invalidEntities = listOf(
            completionEntity().copy(id = ""),
            completionEntity().copy(id = " completion-id"),
            completionEntity().copy(habitId = ""),
            completionEntity().copy(habitId = " habit-id"),
            completionEntity().copy(localDate = "13-09-2026"),
            completionEntity().copy(localDate = "2026-9-13"),
            completionEntity().copy(completedAtEpochMs = -1),
            completionEntity().copy(completedZoneId = "Not/AZone"),
            completionEntity().copy(
                completedAtEpochMs = Instant.parse("2026-09-14T00:00:00Z").toEpochMilli(),
            ),
        )

        invalidEntities.forEach { entity ->
            assertInvalid(DailyHabitRow(habit = habitEntity(), completion = entity))
        }
    }

    @Test
    fun testMap_rejectsCompletionForDifferentHabitOrRequestedDate() {
        assertInvalid(
            DailyHabitRow(
                habit = habitEntity(),
                completion = completionEntity().copy(habitId = "another-habit"),
            ),
        )
        assertInvalid(
            DailyHabitRow(
                habit = habitEntity(),
                completion = completionEntity().copy(localDate = "2026-09-14"),
            ),
        )
    }

    @Test
    fun testToEntity_preservesValidatedDomainValuesAndStablePersistenceValues() {
        val habit = habit()
        val completion = HabitCompletion(
            id = HabitCompletionId("completion-id"),
            habitId = habit.id,
            localDate = requestedDate,
            completedAt = Instant.parse("2026-09-13T08:30:00Z"),
            completedZoneId = ZoneId.of("Asia/Kolkata"),
        )

        assertEquals(habitEntity(), mapper.toEntity(habit))
        assertEquals(completionEntity(), mapper.toEntity(completion))
    }

    @Test
    fun testToEntity_preservesEveryScheduleCatalogAndArchiveValue() {
        Cue.entries.forEach { cue ->
            val habit = habit().copy(
                goalNote = "Stay consistent",
                activeDays = ActiveDaysMask.of(java.time.DayOfWeek.TUESDAY),
                schedule = HabitSchedule.RoutineCue(cue),
                iconId = HabitCatalog.habitIconIds.last(),
                colorId = HabitCatalog.habitColorIds.last(),
                updatedAt = Instant.parse("2026-09-14T08:00:00Z"),
                archivedAt = Instant.parse("2026-09-14T08:00:00Z"),
                archivedZoneId = ZoneId.of("UTC"),
            )

            val entity = mapper.toEntity(habit)

            assertEquals("ROUTINE_CUE", entity.scheduleType)
            assertEquals(cue.persistedId, entity.routineCueId)
            assertNull(entity.exactTimeMinutes)
            assertEquals(habit, successValue(mapper.mapHabit(entity)))
        }

        val exactTimeHabit = habit().copy(schedule = HabitSchedule.ExactTime(1_439))
        val exactTimeEntity = mapper.toEntity(exactTimeHabit)

        assertEquals("EXACT_TIME", exactTimeEntity.scheduleType)
        assertEquals(1_439, exactTimeEntity.exactTimeMinutes)
        assertNull(exactTimeEntity.routineCueId)
        assertEquals(exactTimeHabit, successValue(mapper.mapHabit(exactTimeEntity)))
    }

    @Test
    fun testMap_acceptsEveryRegisteredHabitCatalogId() {
        HabitCatalog.habitIconIds.forEach { iconId ->
            val mapped = successValue(mapper.mapHabit(habitEntity().copy(iconId = iconId)))
            assertEquals(iconId, mapped.iconId)
        }
        HabitCatalog.habitColorIds.forEach { colorId ->
            val mapped = successValue(mapper.mapHabit(habitEntity().copy(colorId = colorId)))
            assertEquals(colorId, mapped.colorId)
        }
    }

    private fun assertInvalid(row: DailyHabitRow) {
        assertTrue(
            mapper.map(row, requestedDate) ===
                StoredHabitMappingResult.InvalidStoredData,
        )
    }

    private fun successValue(result: StoredHabitMappingResult<Habit>): Habit {
        assertTrue(result is StoredHabitMappingResult.Success)
        return (result as StoredHabitMappingResult.Success).value
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

    private fun habitEntity() = HabitEntity(
        id = "habit-id",
        name = "Morning walk",
        recurrence = "DAILY",
        iconId = DEFAULT_HABIT_ICON_ID,
        colorId = DEFAULT_HABIT_COLOR_ID,
        createdAtEpochMs = Instant.parse("2026-09-13T08:00:00Z").toEpochMilli(),
        updatedAtEpochMs = Instant.parse("2026-09-13T08:00:00Z").toEpochMilli(),
        createdZoneId = "Asia/Kolkata",
    )

    private fun completionEntity() = HabitCompletionEntity(
        id = "completion-id",
        habitId = "habit-id",
        localDate = "2026-09-13",
        completedAtEpochMs = Instant.parse("2026-09-13T08:30:00Z").toEpochMilli(),
        completedZoneId = "Asia/Kolkata",
    )
}
