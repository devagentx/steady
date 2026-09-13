package ai.monkmind.steady.habit

sealed interface HabitStoreFailure {
    data object ReadFailure : HabitStoreFailure

    data object WriteFailure : HabitStoreFailure

    data object InvalidStoredData : HabitStoreFailure
}

sealed interface ObserveTodayResult {
    data class Success(val habits: List<DailyHabit>) : ObserveTodayResult

    data class Failure(val reason: HabitStoreFailure) : ObserveTodayResult
}

sealed interface ObserveManagedHabitsResult {
    data class Success(
        val active: List<Habit>,
        val archived: List<Habit>,
    ) : ObserveManagedHabitsResult

    data class Failure(val reason: HabitStoreFailure) : ObserveManagedHabitsResult
}

sealed interface ObserveHabitResult {
    data class Found(val habit: Habit) : ObserveHabitResult

    data object HabitNotFound : ObserveHabitResult

    data class Failure(val reason: HabitStoreFailure) : ObserveHabitResult
}

sealed interface CreateHabitResult {
    data class Created(val habit: Habit) : CreateHabitResult

    data object BlankName : CreateHabitResult

    data object ActiveLimitReached : CreateHabitResult

    data class Failure(val reason: HabitStoreFailure) : CreateHabitResult
}

sealed interface UpdateHabitResult {
    data object Updated : UpdateHabitResult

    data class InvalidDraft(val errors: Set<HabitDraftError>) : UpdateHabitResult

    data object HabitNotFound : UpdateHabitResult

    data class Failure(val reason: HabitStoreFailure) : UpdateHabitResult
}

sealed interface CompleteHabitResult {
    data class Completed(val completion: HabitCompletion) : CompleteHabitResult

    data object AlreadyCompleted : CompleteHabitResult

    data object HabitNotFound : CompleteHabitResult

    data object Archived : CompleteHabitResult

    data object NotScheduledForDate : CompleteHabitResult

    data class Failure(val reason: HabitStoreFailure) : CompleteHabitResult
}

sealed interface ArchiveHabitResult {
    data object Archived : ArchiveHabitResult

    data object AlreadyArchived : ArchiveHabitResult

    data object HabitNotFound : ArchiveHabitResult

    data class Failure(val reason: HabitStoreFailure) : ArchiveHabitResult
}

sealed interface RestoreHabitResult {
    data object Restored : RestoreHabitResult

    data object AlreadyActive : RestoreHabitResult

    data object ActiveLimitReached : RestoreHabitResult

    data object HabitNotFound : RestoreHabitResult

    data class Failure(val reason: HabitStoreFailure) : RestoreHabitResult
}

sealed interface DeleteHabitResult {
    data object Deleted : DeleteHabitResult

    data object HabitNotFound : DeleteHabitResult

    data class Failure(val reason: HabitStoreFailure) : DeleteHabitResult
}

sealed interface ClearHabitDataResult {
    data object Cleared : ClearHabitDataResult

    data class Failure(val reason: HabitStoreFailure) : ClearHabitDataResult
}
