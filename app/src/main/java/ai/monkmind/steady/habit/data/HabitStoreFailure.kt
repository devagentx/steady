package ai.monkmind.steady.habit.data

internal sealed interface StoredHabitMappingResult<out T> {
    data class Success<T>(val value: T) : StoredHabitMappingResult<T>

    data object InvalidStoredData : StoredHabitMappingResult<Nothing>
}
