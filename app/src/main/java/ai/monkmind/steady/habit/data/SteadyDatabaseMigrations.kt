package ai.monkmind.steady.habit.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN goal_note TEXT")
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN active_days_mask INTEGER NOT NULL DEFAULT 127",
        )
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN schedule_type TEXT NOT NULL DEFAULT 'ROUTINE_CUE'",
        )
        db.execSQL("ALTER TABLE habits ADD COLUMN exact_time_minutes INTEGER")
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN routine_cue_id TEXT DEFAULT 'ANYTIME'",
        )
        db.execSQL("ALTER TABLE habits ADD COLUMN archived_at_epoch_ms INTEGER")
        db.execSQL("ALTER TABLE habits ADD COLUMN archived_zone_id TEXT")
    }
}
