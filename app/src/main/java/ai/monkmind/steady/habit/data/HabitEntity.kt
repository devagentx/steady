package ai.monkmind.steady.habit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val recurrence: String,
    @ColumnInfo(name = "icon_id")
    val iconId: String,
    @ColumnInfo(name = "color_id")
    val colorId: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long,
    @ColumnInfo(name = "created_zone_id")
    val createdZoneId: String,
    @ColumnInfo(name = "goal_note")
    val goalNote: String? = null,
    @ColumnInfo(name = "active_days_mask", defaultValue = "127")
    val activeDaysMask: Int = 127,
    @ColumnInfo(name = "schedule_type", defaultValue = "'ROUTINE_CUE'")
    val scheduleType: String = "ROUTINE_CUE",
    @ColumnInfo(name = "exact_time_minutes")
    val exactTimeMinutes: Int? = null,
    @ColumnInfo(name = "routine_cue_id", defaultValue = "'ANYTIME'")
    val routineCueId: String? = "ANYTIME",
    @ColumnInfo(name = "archived_at_epoch_ms")
    val archivedAtEpochMs: Long? = null,
    @ColumnInfo(name = "archived_zone_id")
    val archivedZoneId: String? = null,
)
