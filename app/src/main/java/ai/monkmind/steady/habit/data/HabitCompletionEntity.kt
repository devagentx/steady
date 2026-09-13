package ai.monkmind.steady.habit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(
            value = ["habit_id", "local_date"],
            unique = true,
        ),
    ],
)
data class HabitCompletionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "habit_id")
    val habitId: String,
    @ColumnInfo(name = "local_date")
    val localDate: String,
    @ColumnInfo(name = "completed_at_epoch_ms")
    val completedAtEpochMs: Long,
    @ColumnInfo(name = "completed_zone_id")
    val completedZoneId: String,
)
