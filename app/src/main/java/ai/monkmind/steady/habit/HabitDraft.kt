package ai.monkmind.steady.habit

data class HabitDraft(
    val name: String,
    val goalNote: String?,
    val activeDays: ActiveDaysMask?,
    val schedule: HabitSchedule?,
    val iconId: String,
    val colorId: String,
) {
    fun validate(): HabitDraftValidationResult {
        val normalizedName = name.trim()
        val normalizedNote = goalNote?.trim()?.takeIf(String::isNotBlank)
        val errors = linkedSetOf<HabitDraftError>()

        if (normalizedName.isBlank()) {
            errors += HabitDraftError.BlankName
        }
        if (activeDays == null) {
            errors += HabitDraftError.MissingActiveDays
        }
        if (schedule == null) {
            errors += HabitDraftError.MissingSchedule
        }
        if (!HabitCatalog.containsHabitIcon(iconId)) {
            errors += HabitDraftError.UnknownIcon
        }
        if (!HabitCatalog.containsHabitColor(colorId)) {
            errors += HabitDraftError.UnknownColor
        }

        if (errors.isNotEmpty()) {
            return HabitDraftValidationResult.Invalid(errors)
        }

        return HabitDraftValidationResult.Valid(
            ValidatedHabitDraft(
                name = normalizedName,
                goalNote = normalizedNote,
                activeDays = checkNotNull(activeDays),
                schedule = checkNotNull(schedule),
                iconId = iconId,
                colorId = colorId,
            ),
        )
    }

    companion object {
        fun r0Default(name: String): HabitDraft = HabitDraft(
            name = name,
            goalNote = null,
            activeDays = DEFAULT_ACTIVE_DAYS,
            schedule = DEFAULT_HABIT_SCHEDULE,
            iconId = DEFAULT_HABIT_ICON_ID,
            colorId = DEFAULT_HABIT_COLOR_ID,
        )
    }
}

data class ValidatedHabitDraft(
    val name: String,
    val goalNote: String?,
    val activeDays: ActiveDaysMask,
    val schedule: HabitSchedule,
    val iconId: String,
    val colorId: String,
)

enum class HabitDraftError {
    BlankName,
    MissingActiveDays,
    MissingSchedule,
    UnknownIcon,
    UnknownColor,
}

sealed interface HabitDraftValidationResult {
    data class Valid(val draft: ValidatedHabitDraft) : HabitDraftValidationResult

    data class Invalid(val errors: Set<HabitDraftError>) : HabitDraftValidationResult
}
