package ai.monkmind.steady.habit

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@JvmInline
value class HabitCompletionId(val value: String)

data class HabitCompletion(
    val id: HabitCompletionId,
    val habitId: HabitId,
    val localDate: LocalDate,
    val completedAt: Instant,
    val completedZoneId: ZoneId,
)
