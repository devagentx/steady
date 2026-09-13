package ai.monkmind.steady.habit

import java.util.UUID

class HabitIdGenerator(
    private val generateValue: () -> String = { UUID.randomUUID().toString() },
) {
    fun generateHabitId(): HabitId = HabitId(generateValue())

    fun generateCompletionId(): HabitCompletionId = HabitCompletionId(generateValue())
}
