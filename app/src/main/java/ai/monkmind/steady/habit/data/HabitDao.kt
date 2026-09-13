package ai.monkmind.steady.habit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

sealed interface InsertHabitOutcome {
    data object Inserted : InsertHabitOutcome

    data object LimitReached : InsertHabitOutcome
}

sealed interface InsertCompletionOutcome {
    data object Inserted : InsertCompletionOutcome

    data object AlreadyCompleted : InsertCompletionOutcome

    data object HabitNotFound : InsertCompletionOutcome

    data object Archived : InsertCompletionOutcome

    data object NotScheduledForDate : InsertCompletionOutcome
}

sealed interface UpdateHabitOutcome {
    data object Updated : UpdateHabitOutcome

    data object HabitNotFound : UpdateHabitOutcome
}

sealed interface ArchiveHabitOutcome {
    data object Archived : ArchiveHabitOutcome

    data object AlreadyArchived : ArchiveHabitOutcome

    data object HabitNotFound : ArchiveHabitOutcome
}

sealed interface RestoreHabitOutcome {
    data object Restored : RestoreHabitOutcome

    data object AlreadyActive : RestoreHabitOutcome

    data object LimitReached : RestoreHabitOutcome

    data object HabitNotFound : RestoreHabitOutcome
}

sealed interface DeleteHabitOutcome {
    data object Deleted : DeleteHabitOutcome

    data object HabitNotFound : DeleteHabitOutcome
}

data class HabitUpdateFields(
    val id: String,
    val name: String,
    val goalNote: String?,
    val activeDaysMask: Int,
    val scheduleType: String,
    val exactTimeMinutes: Int?,
    val routineCueId: String?,
    val iconId: String,
    val colorId: String,
    val updatedAtEpochMs: Long,
)

@Dao
abstract class HabitDao {
    @Query(
        """
        SELECT
            h.id,
            h.name,
            h.recurrence,
            h.icon_id,
            h.color_id,
            h.created_at_epoch_ms,
            h.updated_at_epoch_ms,
            h.created_zone_id,
            h.goal_note,
            h.active_days_mask,
            h.schedule_type,
            h.exact_time_minutes,
            h.routine_cue_id,
            h.archived_at_epoch_ms,
            h.archived_zone_id,
            c.id AS completion_id,
            c.habit_id AS completion_habit_id,
            c.local_date AS completion_local_date,
            c.completed_at_epoch_ms AS completion_completed_at_epoch_ms,
            c.completed_zone_id AS completion_completed_zone_id
        FROM habits AS h
        LEFT JOIN habit_completions AS c
            ON c.habit_id = h.id
            AND c.local_date = :localDate
        WHERE h.recurrence = 'DAILY'
            AND h.archived_at_epoch_ms IS NULL
            AND (h.active_days_mask & :weekdayBit) != 0
        ORDER BY
            CASE WHEN c.id IS NULL THEN 0 ELSE 1 END,
            CASE h.schedule_type
                WHEN 'EXACT_TIME' THEN 0
                WHEN 'ROUTINE_CUE' THEN 1
                ELSE 2
            END,
            CASE
                WHEN h.schedule_type = 'EXACT_TIME' THEN h.exact_time_minutes
                ELSE NULL
            END,
            CASE h.routine_cue_id
                WHEN 'AFTER_WAKING' THEN 0
                WHEN 'AFTER_BREAKFAST' THEN 1
                WHEN 'AFTER_LUNCH' THEN 2
                WHEN 'AFTER_DINNER' THEN 3
                WHEN 'BEFORE_BED' THEN 4
                WHEN 'ANYTIME' THEN 5
                ELSE 6
            END,
            h.created_at_epoch_ms ASC,
            h.id ASC
        """,
    )
    abstract fun observeScheduledHabits(
        localDate: String,
        weekdayBit: Int,
    ): Flow<List<DailyHabitRow>>

    @Query(
        """
        SELECT * FROM habits
        ORDER BY
            CASE WHEN archived_at_epoch_ms IS NULL THEN 0 ELSE 1 END,
            created_at_epoch_ms ASC,
            id ASC
        """,
    )
    abstract fun observeManagedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    abstract fun observeHabit(habitId: String): Flow<HabitEntity?>

    @Query("SELECT COUNT(*) FROM habits WHERE archived_at_epoch_ms IS NULL")
    protected abstract suspend fun countActiveHabits(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertHabit(entity: HabitEntity)

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    protected abstract suspend fun habitById(habitId: String): HabitEntity?

    @Query(
        """
        UPDATE habits SET
            name = :name,
            goal_note = :goalNote,
            active_days_mask = :activeDaysMask,
            schedule_type = :scheduleType,
            exact_time_minutes = :exactTimeMinutes,
            routine_cue_id = :routineCueId,
            icon_id = :iconId,
            color_id = :colorId,
            updated_at_epoch_ms = :updatedAtEpochMs
        WHERE id = :habitId
        """,
    )
    protected abstract suspend fun updateMutableFields(
        habitId: String,
        name: String,
        goalNote: String?,
        activeDaysMask: Int,
        scheduleType: String,
        exactTimeMinutes: Int?,
        routineCueId: String?,
        iconId: String,
        colorId: String,
        updatedAtEpochMs: Long,
    ): Int

    @Query(
        """
        UPDATE habits SET
            archived_at_epoch_ms = :archivedAtEpochMs,
            archived_zone_id = :archivedZoneId,
            updated_at_epoch_ms = :updatedAtEpochMs
        WHERE id = :habitId
        """,
    )
    protected abstract suspend fun setArchived(
        habitId: String,
        archivedAtEpochMs: Long?,
        archivedZoneId: String?,
        updatedAtEpochMs: Long,
    ): Int

    @Query(
        """
        SELECT * FROM habit_completions
        WHERE habit_id = :habitId
            AND local_date = :localDate
        LIMIT 1
        """,
    )
    protected abstract suspend fun completionForDate(
        habitId: String,
        localDate: String,
    ): HabitCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertCompletion(entity: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habit_id = :habitId")
    protected abstract suspend fun deleteCompletionsForHabit(habitId: String)

    @Query("DELETE FROM habits WHERE id = :habitId")
    protected abstract suspend fun deleteHabit(habitId: String): Int

    @Query("DELETE FROM habit_completions")
    protected abstract suspend fun deleteAllCompletions()

    @Query("DELETE FROM habits")
    protected abstract suspend fun deleteAllHabits()

    @Transaction
    open suspend fun insertHabitWithinLimit(
        entity: HabitEntity,
        limit: Int,
    ): InsertHabitOutcome {
        if (countActiveHabits() >= limit) {
            return InsertHabitOutcome.LimitReached
        }

        insertHabit(entity)
        return InsertHabitOutcome.Inserted
    }

    @Transaction
    open suspend fun updateHabitFields(fields: HabitUpdateFields): UpdateHabitOutcome {
        if (habitById(fields.id) == null) {
            return UpdateHabitOutcome.HabitNotFound
        }

        updateMutableFields(
            habitId = fields.id,
            name = fields.name,
            goalNote = fields.goalNote,
            activeDaysMask = fields.activeDaysMask,
            scheduleType = fields.scheduleType,
            exactTimeMinutes = fields.exactTimeMinutes,
            routineCueId = fields.routineCueId,
            iconId = fields.iconId,
            colorId = fields.colorId,
            updatedAtEpochMs = fields.updatedAtEpochMs,
        )
        return UpdateHabitOutcome.Updated
    }

    @Transaction
    open suspend fun archiveHabit(
        habitId: String,
        archivedAtEpochMs: Long,
        archivedZoneId: String,
    ): ArchiveHabitOutcome {
        val habit = habitById(habitId)
            ?: return ArchiveHabitOutcome.HabitNotFound
        if (habit.archivedAtEpochMs != null) {
            return ArchiveHabitOutcome.AlreadyArchived
        }

        setArchived(
            habitId = habitId,
            archivedAtEpochMs = archivedAtEpochMs,
            archivedZoneId = archivedZoneId,
            updatedAtEpochMs = archivedAtEpochMs,
        )
        return ArchiveHabitOutcome.Archived
    }

    @Transaction
    open suspend fun restoreHabitWithinLimit(
        habitId: String,
        limit: Int,
        updatedAtEpochMs: Long,
    ): RestoreHabitOutcome {
        val habit = habitById(habitId)
            ?: return RestoreHabitOutcome.HabitNotFound
        if (habit.archivedAtEpochMs == null) {
            return RestoreHabitOutcome.AlreadyActive
        }
        if (countActiveHabits() >= limit) {
            return RestoreHabitOutcome.LimitReached
        }

        setArchived(
            habitId = habitId,
            archivedAtEpochMs = null,
            archivedZoneId = null,
            updatedAtEpochMs = updatedAtEpochMs,
        )
        return RestoreHabitOutcome.Restored
    }

    @Transaction
    open suspend fun deleteHabitAndHistory(habitId: String): DeleteHabitOutcome {
        if (habitById(habitId) == null) {
            return DeleteHabitOutcome.HabitNotFound
        }

        deleteCompletionsForHabit(habitId)
        deleteHabit(habitId)
        return DeleteHabitOutcome.Deleted
    }

    @Transaction
    open suspend fun clearAllHabitData() {
        deleteAllCompletions()
        deleteAllHabits()
    }

    @Transaction
    open suspend fun insertCompletionIfEligible(
        entity: HabitCompletionEntity,
        weekdayBit: Int,
    ): InsertCompletionOutcome {
        val habit = habitById(entity.habitId)
            ?: return InsertCompletionOutcome.HabitNotFound
        if (habit.archivedAtEpochMs != null) {
            return InsertCompletionOutcome.Archived
        }
        if (habit.activeDaysMask and weekdayBit == 0) {
            return InsertCompletionOutcome.NotScheduledForDate
        }

        if (completionForDate(entity.habitId, entity.localDate) != null) {
            return InsertCompletionOutcome.AlreadyCompleted
        }

        insertCompletion(entity)
        return InsertCompletionOutcome.Inserted
    }
}
