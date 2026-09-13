package ai.monkmind.steady.habit.data

import ai.monkmind.steady.habit.ArchiveHabitResult
import ai.monkmind.steady.habit.ClearHabitDataResult
import ai.monkmind.steady.habit.CompleteHabitResult
import ai.monkmind.steady.habit.CreateHabitResult
import ai.monkmind.steady.habit.DailyHabit
import ai.monkmind.steady.habit.DeleteHabitResult
import ai.monkmind.steady.habit.Habit
import ai.monkmind.steady.habit.HabitCompletion
import ai.monkmind.steady.habit.HabitDraft
import ai.monkmind.steady.habit.HabitDraftError
import ai.monkmind.steady.habit.HabitDraftValidationResult
import ai.monkmind.steady.habit.HabitId
import ai.monkmind.steady.habit.HabitIdGenerator
import ai.monkmind.steady.habit.HabitRecurrence
import ai.monkmind.steady.habit.HabitRepository
import ai.monkmind.steady.habit.HabitSchedule
import ai.monkmind.steady.habit.HabitStoreFailure
import ai.monkmind.steady.habit.MAX_ACTIVE_HABITS
import ai.monkmind.steady.habit.ObserveHabitResult
import ai.monkmind.steady.habit.ObserveManagedHabitsResult
import ai.monkmind.steady.habit.ObserveTodayResult
import ai.monkmind.steady.habit.RestoreHabitResult
import ai.monkmind.steady.habit.UpdateHabitResult
import ai.monkmind.steady.habit.Weekday
import ai.monkmind.steady.reset.AppMutationMutex
import ai.monkmind.steady.time.LocalDateSnapshot
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class RoomHabitRepository(
    private val habitDao: HabitDao,
    private val mutationMutex: AppMutationMutex = AppMutationMutex(),
    private val idGenerator: HabitIdGenerator = HabitIdGenerator(),
    private val mapper: HabitEntityMapper = HabitEntityMapper(),
) : HabitRepository {
    override fun observeToday(date: LocalDate): Flow<ObserveTodayResult> =
        typedRead(
            source = {
                habitDao.observeScheduledHabits(
                    localDate = date.toString(),
                    weekdayBit = Weekday.from(date.dayOfWeek).bit,
                )
                    .map { rows ->
                        val habits = ArrayList<DailyHabit>(rows.size)
                        for (row in rows) {
                            when (val result = mapper.map(row, date)) {
                                StoredHabitMappingResult.InvalidStoredData -> {
                                    return@map ObserveTodayResult.Failure(
                                        HabitStoreFailure.InvalidStoredData,
                                    )
                                }
                                is StoredHabitMappingResult.Success -> habits += result.value
                            }
                        }
                        ObserveTodayResult.Success(habits)
                    }
            },
            failure = { reason -> ObserveTodayResult.Failure(reason) },
        )

    override fun observeManagedHabits(): Flow<ObserveManagedHabitsResult> =
        typedRead(
            source = {
                habitDao.observeManagedHabits()
                    .map { entities ->
                        val active = ArrayList<Habit>()
                        val archived = ArrayList<Habit>()
                        for (entity in entities) {
                            val habit = when (val result = mapper.mapHabit(entity)) {
                                StoredHabitMappingResult.InvalidStoredData -> {
                                    return@map ObserveManagedHabitsResult.Failure(
                                        HabitStoreFailure.InvalidStoredData,
                                    )
                                }
                                is StoredHabitMappingResult.Success -> result.value
                            }
                            if (habit.isArchived) {
                                archived += habit
                            } else {
                                active += habit
                            }
                        }
                        ObserveManagedHabitsResult.Success(
                            active = active,
                            archived = archived,
                        )
                    }
            },
            failure = { reason -> ObserveManagedHabitsResult.Failure(reason) },
        )

    override fun observeHabit(id: HabitId): Flow<ObserveHabitResult> {
        if (!id.value.isValidMutationId()) {
            return flowOf(
                ObserveHabitResult.Failure(HabitStoreFailure.ReadFailure),
            )
        }
        return typedRead(
            source = {
                habitDao.observeHabit(id.value)
                    .map { entity ->
                        if (entity == null) {
                            ObserveHabitResult.HabitNotFound
                        } else {
                            when (val result = mapper.mapHabit(entity)) {
                                StoredHabitMappingResult.InvalidStoredData -> {
                                    ObserveHabitResult.Failure(
                                        HabitStoreFailure.InvalidStoredData,
                                    )
                                }
                                is StoredHabitMappingResult.Success -> {
                                    ObserveHabitResult.Found(result.value)
                                }
                            }
                        }
                    }
            },
            failure = { reason -> ObserveHabitResult.Failure(reason) },
        )
    }

    override suspend fun createHabit(
        draft: HabitDraft,
        time: LocalDateSnapshot,
    ): CreateHabitResult {
        val validatedDraft = when (val validation = draft.validate()) {
            is HabitDraftValidationResult.Invalid -> {
                return if (HabitDraftError.BlankName in validation.errors) {
                    CreateHabitResult.BlankName
                } else {
                    CreateHabitResult.Failure(HabitStoreFailure.WriteFailure)
                }
            }
            is HabitDraftValidationResult.Valid -> validation.draft
        }
        if (!time.isCoherent()) {
            return CreateHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }

        val habitId = idGenerator.generateHabitId()
        if (!habitId.value.isValidMutationId()) {
            return CreateHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }
        val habit = Habit(
            id = habitId,
            name = validatedDraft.name,
            recurrence = HabitRecurrence.Daily,
            goalNote = validatedDraft.goalNote,
            activeDays = validatedDraft.activeDays,
            schedule = validatedDraft.schedule,
            iconId = validatedDraft.iconId,
            colorId = validatedDraft.colorId,
            createdAt = time.instant,
            updatedAt = time.instant,
            createdZoneId = time.zoneId,
        )

        return mutate(
            failure = { CreateHabitResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            when (
                habitDao.insertHabitWithinLimit(
                    entity = mapper.toEntity(habit),
                    limit = MAX_ACTIVE_HABITS,
                )
            ) {
                InsertHabitOutcome.Inserted -> CreateHabitResult.Created(habit)
                InsertHabitOutcome.LimitReached -> CreateHabitResult.ActiveLimitReached
            }
        }
    }

    override suspend fun createDailyHabit(
        name: String,
        time: LocalDateSnapshot,
    ): CreateHabitResult = createHabit(HabitDraft.r0Default(name), time)

    override suspend fun updateHabit(
        id: HabitId,
        draft: HabitDraft,
        time: LocalDateSnapshot,
    ): UpdateHabitResult {
        val validatedDraft = when (val validation = draft.validate()) {
            is HabitDraftValidationResult.Invalid -> {
                return UpdateHabitResult.InvalidDraft(validation.errors)
            }
            is HabitDraftValidationResult.Valid -> validation.draft
        }
        if (!id.value.isValidMutationId() || !time.isCoherent()) {
            return UpdateHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }

        val update = HabitUpdateFields(
            id = id.value,
            name = validatedDraft.name,
            goalNote = validatedDraft.goalNote,
            activeDaysMask = validatedDraft.activeDays.value,
            scheduleType = validatedDraft.schedule.persistedType,
            exactTimeMinutes =
                (validatedDraft.schedule as? HabitSchedule.ExactTime)?.minuteOfDay,
            routineCueId =
                (validatedDraft.schedule as? HabitSchedule.RoutineCue)?.cue?.persistedId,
            iconId = validatedDraft.iconId,
            colorId = validatedDraft.colorId,
            updatedAtEpochMs = time.instant.toEpochMilli(),
        )
        return mutate(
            failure = { UpdateHabitResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            when (habitDao.updateHabitFields(update)) {
                UpdateHabitOutcome.Updated -> UpdateHabitResult.Updated
                UpdateHabitOutcome.HabitNotFound -> UpdateHabitResult.HabitNotFound
            }
        }
    }

    override suspend fun completeHabit(
        habitId: HabitId,
        time: LocalDateSnapshot,
    ): CompleteHabitResult {
        if (!habitId.value.isValidMutationId() || !time.isCoherent()) {
            return CompleteHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }

        val completionId = idGenerator.generateCompletionId()
        if (!completionId.value.isValidMutationId()) {
            return CompleteHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }
        val completion = HabitCompletion(
            id = completionId,
            habitId = habitId,
            localDate = time.localDate,
            completedAt = time.instant,
            completedZoneId = time.zoneId,
        )

        return mutate(
            failure = { CompleteHabitResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            when (
                habitDao.insertCompletionIfEligible(
                    entity = mapper.toEntity(completion),
                    weekdayBit = Weekday.from(time.localDate.dayOfWeek).bit,
                )
            ) {
                InsertCompletionOutcome.Inserted -> CompleteHabitResult.Completed(completion)
                InsertCompletionOutcome.AlreadyCompleted -> CompleteHabitResult.AlreadyCompleted
                InsertCompletionOutcome.HabitNotFound -> CompleteHabitResult.HabitNotFound
                InsertCompletionOutcome.Archived -> CompleteHabitResult.Archived
                InsertCompletionOutcome.NotScheduledForDate -> {
                    CompleteHabitResult.NotScheduledForDate
                }
            }
        }
    }

    override suspend fun archiveHabit(
        id: HabitId,
        time: LocalDateSnapshot,
    ): ArchiveHabitResult {
        if (!id.value.isValidMutationId() || !time.isCoherent()) {
            return ArchiveHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }
        return mutate(
            failure = { ArchiveHabitResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            when (
                habitDao.archiveHabit(
                    habitId = id.value,
                    archivedAtEpochMs = time.instant.toEpochMilli(),
                    archivedZoneId = time.zoneId.id,
                )
            ) {
                ArchiveHabitOutcome.Archived -> ArchiveHabitResult.Archived
                ArchiveHabitOutcome.AlreadyArchived -> ArchiveHabitResult.AlreadyArchived
                ArchiveHabitOutcome.HabitNotFound -> ArchiveHabitResult.HabitNotFound
            }
        }
    }

    override suspend fun restoreHabit(
        id: HabitId,
        time: LocalDateSnapshot,
    ): RestoreHabitResult {
        if (!id.value.isValidMutationId() || !time.isCoherent()) {
            return RestoreHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }
        return mutate(
            failure = { RestoreHabitResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            when (
                habitDao.restoreHabitWithinLimit(
                    habitId = id.value,
                    limit = MAX_ACTIVE_HABITS,
                    updatedAtEpochMs = time.instant.toEpochMilli(),
                )
            ) {
                RestoreHabitOutcome.Restored -> RestoreHabitResult.Restored
                RestoreHabitOutcome.AlreadyActive -> RestoreHabitResult.AlreadyActive
                RestoreHabitOutcome.LimitReached -> RestoreHabitResult.ActiveLimitReached
                RestoreHabitOutcome.HabitNotFound -> RestoreHabitResult.HabitNotFound
            }
        }
    }

    override suspend fun deleteHabitAndHistory(id: HabitId): DeleteHabitResult {
        if (!id.value.isValidMutationId()) {
            return DeleteHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }
        return mutate(
            failure = { DeleteHabitResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            when (habitDao.deleteHabitAndHistory(id.value)) {
                DeleteHabitOutcome.Deleted -> DeleteHabitResult.Deleted
                DeleteHabitOutcome.HabitNotFound -> DeleteHabitResult.HabitNotFound
            }
        }
    }

    override suspend fun clearAllHabitData(): ClearHabitDataResult =
        mutate(
            failure = { ClearHabitDataResult.Failure(HabitStoreFailure.WriteFailure) },
        ) {
            habitDao.clearAllHabitData()
            ClearHabitDataResult.Cleared
        }

    private suspend fun <T> mutate(
        failure: () -> T,
        action: suspend () -> T,
    ): T = try {
        mutationMutex.withLock(action)
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        failure()
    }

    private fun LocalDateSnapshot.isCoherent(): Boolean =
        instant.atZone(zoneId).toLocalDate() == localDate

    private fun String.isValidMutationId(): Boolean =
        isNotBlank() && this == trim()

    private fun <T> typedRead(
        source: () -> Flow<T>,
        failure: (HabitStoreFailure) -> T,
    ): Flow<T> =
        flow {
            emitAll(source())
        }.catch { error ->
            if (error is CancellationException) {
                throw error
            }
            emit(failure(HabitStoreFailure.ReadFailure))
        }

    private val HabitSchedule.persistedType: String
        get() = when (this) {
            is HabitSchedule.ExactTime -> EXACT_TIME_SCHEDULE
            is HabitSchedule.RoutineCue -> ROUTINE_CUE_SCHEDULE
        }

    private companion object {
        const val EXACT_TIME_SCHEDULE = "EXACT_TIME"
        const val ROUTINE_CUE_SCHEDULE = "ROUTINE_CUE"
    }
}
