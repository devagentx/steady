package ai.monkmind.steady.habit

import ai.monkmind.steady.time.LocalDateSnapshot
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface HabitRepository {
    fun observeToday(date: LocalDate): Flow<ObserveTodayResult>

    fun observeManagedHabits(): Flow<ObserveManagedHabitsResult> =
        flowOf(ObserveManagedHabitsResult.Failure(HabitStoreFailure.ReadFailure))

    fun observeHabit(id: HabitId): Flow<ObserveHabitResult> =
        flowOf(ObserveHabitResult.Failure(HabitStoreFailure.ReadFailure))

    suspend fun createHabit(
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
        if (
            validatedDraft.goalNote != null ||
            validatedDraft.activeDays != DEFAULT_ACTIVE_DAYS ||
            validatedDraft.schedule != DEFAULT_HABIT_SCHEDULE ||
            validatedDraft.iconId != DEFAULT_HABIT_ICON_ID ||
            validatedDraft.colorId != DEFAULT_HABIT_COLOR_ID
        ) {
            return CreateHabitResult.Failure(HabitStoreFailure.WriteFailure)
        }
        return createDailyHabit(validatedDraft.name, time)
    }

    suspend fun createDailyHabit(
        name: String,
        time: LocalDateSnapshot,
    ): CreateHabitResult = CreateHabitResult.Failure(HabitStoreFailure.WriteFailure)

    suspend fun updateHabit(
        id: HabitId,
        draft: HabitDraft,
        time: LocalDateSnapshot,
    ): UpdateHabitResult = UpdateHabitResult.Failure(HabitStoreFailure.WriteFailure)

    suspend fun completeHabit(
        habitId: HabitId,
        time: LocalDateSnapshot,
    ): CompleteHabitResult

    suspend fun archiveHabit(
        id: HabitId,
        time: LocalDateSnapshot,
    ): ArchiveHabitResult = ArchiveHabitResult.Failure(HabitStoreFailure.WriteFailure)

    suspend fun restoreHabit(
        id: HabitId,
        time: LocalDateSnapshot,
    ): RestoreHabitResult = RestoreHabitResult.Failure(HabitStoreFailure.WriteFailure)

    suspend fun deleteHabitAndHistory(id: HabitId): DeleteHabitResult =
        DeleteHabitResult.Failure(HabitStoreFailure.WriteFailure)

    suspend fun clearAllHabitData(): ClearHabitDataResult =
        ClearHabitDataResult.Failure(HabitStoreFailure.WriteFailure)
}
