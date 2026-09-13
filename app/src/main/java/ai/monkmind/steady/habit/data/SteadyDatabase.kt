package ai.monkmind.steady.habit.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class SteadyDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "steady.db"
    }
}
