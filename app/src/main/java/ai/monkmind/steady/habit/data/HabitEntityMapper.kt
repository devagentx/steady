package ai.monkmind.steady.habit.data

import ai.monkmind.steady.habit.ActiveDaysMask
import ai.monkmind.steady.habit.Cue
import ai.monkmind.steady.habit.DailyHabit
import ai.monkmind.steady.habit.Habit
import ai.monkmind.steady.habit.HabitCatalog
import ai.monkmind.steady.habit.HabitCompletion
import ai.monkmind.steady.habit.HabitCompletionId
import ai.monkmind.steady.habit.HabitId
import ai.monkmind.steady.habit.HabitRecurrence
import ai.monkmind.steady.habit.HabitSchedule
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal class HabitEntityMapper {
    fun toEntity(habit: Habit): HabitEntity = HabitEntity(
        id = habit.id.value,
        name = habit.name,
        recurrence = DAILY_RECURRENCE,
        goalNote = habit.goalNote,
        activeDaysMask = habit.activeDays.value,
        scheduleType = when (habit.schedule) {
            is HabitSchedule.ExactTime -> EXACT_TIME_SCHEDULE
            is HabitSchedule.RoutineCue -> ROUTINE_CUE_SCHEDULE
        },
        exactTimeMinutes = (habit.schedule as? HabitSchedule.ExactTime)?.minuteOfDay,
        routineCueId =
            (habit.schedule as? HabitSchedule.RoutineCue)?.cue?.persistedId,
        iconId = habit.iconId,
        colorId = habit.colorId,
        archivedAtEpochMs = habit.archivedAt?.toEpochMilli(),
        archivedZoneId = habit.archivedZoneId?.id,
        createdAtEpochMs = habit.createdAt.toEpochMilli(),
        updatedAtEpochMs = habit.updatedAt.toEpochMilli(),
        createdZoneId = habit.createdZoneId.id,
    )

    fun toEntity(completion: HabitCompletion): HabitCompletionEntity =
        HabitCompletionEntity(
            id = completion.id.value,
            habitId = completion.habitId.value,
            localDate = completion.localDate.toString(),
            completedAtEpochMs = completion.completedAt.toEpochMilli(),
            completedZoneId = completion.completedZoneId.id,
        )

    fun map(
        row: DailyHabitRow,
        requestedDate: LocalDate,
    ): StoredHabitMappingResult<DailyHabit> {
        val habit = when (val result = mapHabit(row.habit)) {
            StoredHabitMappingResult.InvalidStoredData -> {
                return StoredHabitMappingResult.InvalidStoredData
            }
            is StoredHabitMappingResult.Success -> result.value
        }
        val completion = row.completion?.let { entity ->
            when (val result = mapCompletion(entity)) {
                StoredHabitMappingResult.InvalidStoredData -> {
                    return StoredHabitMappingResult.InvalidStoredData
                }
                is StoredHabitMappingResult.Success -> result.value
            }
        }

        if (
            completion != null &&
            (completion.habitId != habit.id || completion.localDate != requestedDate)
        ) {
            return StoredHabitMappingResult.InvalidStoredData
        }

        return StoredHabitMappingResult.Success(
            DailyHabit(
                habit = habit,
                date = requestedDate,
                completion = completion,
            ),
        )
    }

    internal fun mapHabit(entity: HabitEntity): StoredHabitMappingResult<Habit> {
        if (
            !isStoredTextValid(entity.id) ||
            !isStoredTextValid(entity.name) ||
            entity.recurrence != DAILY_RECURRENCE ||
            (entity.goalNote != null && !isStoredTextValid(entity.goalNote)) ||
            !HabitCatalog.containsHabitIcon(entity.iconId) ||
            !HabitCatalog.containsHabitColor(entity.colorId) ||
            entity.createdAtEpochMs < 0 ||
            entity.updatedAtEpochMs < entity.createdAtEpochMs
        ) {
            return StoredHabitMappingResult.InvalidStoredData
        }

        val activeDays = try {
            ActiveDaysMask(entity.activeDaysMask)
        } catch (_: IllegalArgumentException) {
            return StoredHabitMappingResult.InvalidStoredData
        }
        val schedule = mapSchedule(entity)
            ?: return StoredHabitMappingResult.InvalidStoredData
        val createdZoneId = parseZoneId(entity.createdZoneId)
            ?: return StoredHabitMappingResult.InvalidStoredData
        val archiveFieldsArePaired =
            (entity.archivedAtEpochMs == null) == (entity.archivedZoneId == null)
        if (!archiveFieldsArePaired || (entity.archivedAtEpochMs ?: 0) < 0) {
            return StoredHabitMappingResult.InvalidStoredData
        }
        val archivedZoneId = entity.archivedZoneId?.let(::parseZoneId)
        if (entity.archivedZoneId != null && archivedZoneId == null) {
            return StoredHabitMappingResult.InvalidStoredData
        }

        return StoredHabitMappingResult.Success(
            Habit(
                id = HabitId(entity.id),
                name = entity.name,
                recurrence = HabitRecurrence.Daily,
                goalNote = entity.goalNote,
                activeDays = activeDays,
                schedule = schedule,
                iconId = entity.iconId,
                colorId = entity.colorId,
                archivedAt = entity.archivedAtEpochMs?.let(Instant::ofEpochMilli),
                archivedZoneId = archivedZoneId,
                createdAt = Instant.ofEpochMilli(entity.createdAtEpochMs),
                updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMs),
                createdZoneId = createdZoneId,
            ),
        )
    }

    private fun mapSchedule(entity: HabitEntity): HabitSchedule? =
        when (entity.scheduleType) {
            EXACT_TIME_SCHEDULE -> {
                val minuteOfDay = entity.exactTimeMinutes
                if (
                    minuteOfDay == null ||
                    minuteOfDay !in HabitSchedule.MINUTE_OF_DAY_RANGE ||
                    entity.routineCueId != null
                ) {
                    null
                } else {
                    HabitSchedule.ExactTime(minuteOfDay)
                }
            }
            ROUTINE_CUE_SCHEDULE -> {
                val cueId = entity.routineCueId
                val cue = cueId?.let(Cue::fromPersistedId)
                if (entity.exactTimeMinutes != null || cue == null) {
                    null
                } else {
                    HabitSchedule.RoutineCue(cue)
                }
            }
            else -> null
        }

    private fun mapCompletion(
        entity: HabitCompletionEntity,
    ): StoredHabitMappingResult<HabitCompletion> {
        if (
            !isStoredTextValid(entity.id) ||
            !isStoredTextValid(entity.habitId) ||
            entity.completedAtEpochMs < 0
        ) {
            return StoredHabitMappingResult.InvalidStoredData
        }

        val localDate = try {
            LocalDate.parse(entity.localDate)
        } catch (_: DateTimeException) {
            return StoredHabitMappingResult.InvalidStoredData
        }
        if (localDate.toString() != entity.localDate) {
            return StoredHabitMappingResult.InvalidStoredData
        }

        val completedZoneId = parseZoneId(entity.completedZoneId)
            ?: return StoredHabitMappingResult.InvalidStoredData
        val completedAt = Instant.ofEpochMilli(entity.completedAtEpochMs)
        if (completedAt.atZone(completedZoneId).toLocalDate() != localDate) {
            return StoredHabitMappingResult.InvalidStoredData
        }

        return StoredHabitMappingResult.Success(
            HabitCompletion(
                id = HabitCompletionId(entity.id),
                habitId = HabitId(entity.habitId),
                localDate = localDate,
                completedAt = completedAt,
                completedZoneId = completedZoneId,
            ),
        )
    }

    private fun parseZoneId(value: String): ZoneId? {
        if (!isStoredTextValid(value)) {
            return null
        }
        return try {
            ZoneId.of(value)
        } catch (_: DateTimeException) {
            null
        }
    }

    private fun isStoredTextValid(value: String): Boolean =
        value.isNotBlank() && value == value.trim()

    private companion object {
        const val DAILY_RECURRENCE = "DAILY"
        const val EXACT_TIME_SCHEDULE = "EXACT_TIME"
        const val ROUTINE_CUE_SCHEDULE = "ROUTINE_CUE"
    }
}
