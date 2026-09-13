package ai.monkmind.steady.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

interface LocalDateSource {
    val snapshots: Flow<LocalDateSnapshot>

    fun snapshot(): LocalDateSnapshot
}

data class LocalDateSnapshot(
    val instant: Instant,
    val zoneId: ZoneId,
    val localDate: LocalDate,
)
