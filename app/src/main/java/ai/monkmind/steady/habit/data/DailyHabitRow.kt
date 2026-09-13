package ai.monkmind.steady.habit.data

import androidx.room.Embedded

data class DailyHabitRow(
    @Embedded
    val habit: HabitEntity,
    @Embedded(prefix = "completion_")
    val completion: HabitCompletionEntity?,
)
