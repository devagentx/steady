package ai.monkmind.steady.habit

import java.time.LocalDate

data class DailyHabit(
    val habit: Habit,
    val date: LocalDate,
    val completion: HabitCompletion?,
) {
    val isCompleted: Boolean
        get() = completion != null

    val isScheduled: Boolean
        get() = habit.activeDays.contains(date.dayOfWeek)
}
