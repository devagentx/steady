package ai.monkmind.steady.habit

import java.time.Instant
import java.time.ZoneId

@JvmInline
value class HabitId(val value: String)

enum class HabitRecurrence {
    Daily,
}

data class Habit(
    val id: HabitId,
    val name: String,
    val recurrence: HabitRecurrence,
    val goalNote: String? = null,
    val activeDays: ActiveDaysMask = DEFAULT_ACTIVE_DAYS,
    val schedule: HabitSchedule = DEFAULT_HABIT_SCHEDULE,
    val iconId: String,
    val colorId: String,
    val archivedAt: Instant? = null,
    val archivedZoneId: ZoneId? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdZoneId: ZoneId,
) {
    init {
        require(name.isNotBlank() && name == name.trim()) {
            "Habit name must be trimmed and non-blank."
        }
        require(goalNote == null || goalNote.isNotBlank() && goalNote == goalNote.trim()) {
            "Habit note must be null or trimmed and non-blank."
        }
        require(HabitCatalog.containsHabitIcon(iconId)) { "Unknown habit icon ID." }
        require(HabitCatalog.containsHabitColor(colorId)) { "Unknown habit color ID." }
        require((archivedAt == null) == (archivedZoneId == null)) {
            "Archive instant and zone must both be present or both be absent."
        }
    }

    val isArchived: Boolean
        get() = archivedAt != null
}
