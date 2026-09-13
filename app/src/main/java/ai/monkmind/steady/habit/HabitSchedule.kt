package ai.monkmind.steady.habit

sealed interface HabitSchedule {
    data class ExactTime(val minuteOfDay: Int) : HabitSchedule {
        init {
            require(minuteOfDay in MINUTE_OF_DAY_RANGE) {
                "Exact time must be between 00:00 and 23:59."
            }
        }
    }

    data class RoutineCue(val cue: Cue) : HabitSchedule

    companion object {
        val Anytime = RoutineCue(Cue.Anytime)

        val MINUTE_OF_DAY_RANGE = 0..1439
    }
}

enum class Cue(val persistedId: String) {
    Anytime("ANYTIME"),
    AfterWaking("AFTER_WAKING"),
    AfterBreakfast("AFTER_BREAKFAST"),
    AfterLunch("AFTER_LUNCH"),
    AfterDinner("AFTER_DINNER"),
    BeforeBed("BEFORE_BED"),
    ;

    companion object {
        fun fromPersistedId(id: String): Cue? =
            entries.firstOrNull { cue -> cue.persistedId == id }
    }
}
