package ai.monkmind.steady.habit

import ai.monkmind.steady.time.LocalDateSnapshot
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FakeHabitRepository : HabitRepository {
    data class CreateHabitCall(
        val draft: HabitDraft,
        val time: LocalDateSnapshot,
    )

    data class CreateCall(
        val name: String,
        val time: LocalDateSnapshot,
    )

    data class CompleteCall(
        val habitId: HabitId,
        val time: LocalDateSnapshot,
    )

    data class UpdateCall(
        val id: HabitId,
        val draft: HabitDraft,
        val time: LocalDateSnapshot,
    )

    data class LifecycleCall(
        val id: HabitId,
        val time: LocalDateSnapshot,
    )

    val observedDates = mutableListOf<LocalDate>()
    val observedHabitIds = mutableListOf<HabitId>()
    val cancelledObservationDates = mutableListOf<LocalDate>()
    var managedObservationCount = 0
        private set
    val createHabitCalls = mutableListOf<CreateHabitCall>()
    val createCalls = mutableListOf<CreateCall>()
    val completeCalls = mutableListOf<CompleteCall>()
    val updateCalls = mutableListOf<UpdateCall>()
    val archiveCalls = mutableListOf<LifecycleCall>()
    val restoreCalls = mutableListOf<LifecycleCall>()
    val deleteCalls = mutableListOf<HabitId>()
    var clearCallCount = 0
        private set
    var cancelledCreateCallCount = 0
        private set
    var cancelledCompleteCallCount = 0
        private set

    private val observations = mutableMapOf<LocalDate, MutableSharedFlow<ObserveTodayResult>>()
    private val managedObservations = MutableSharedFlow<ObserveManagedHabitsResult>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    private val habitObservations = mutableMapOf<HabitId, MutableSharedFlow<ObserveHabitResult>>()
    private val createResults = Channel<CreateHabitResult>(Channel.UNLIMITED)
    private val updateResults = Channel<UpdateHabitResult>(Channel.UNLIMITED)
    private val completionResults = Channel<CompleteHabitResult>(Channel.UNLIMITED)
    private val archiveResults = Channel<ArchiveHabitResult>(Channel.UNLIMITED)
    private val restoreResults = Channel<RestoreHabitResult>(Channel.UNLIMITED)
    private val deleteResults = Channel<DeleteHabitResult>(Channel.UNLIMITED)
    private val clearResults = Channel<ClearHabitDataResult>(Channel.UNLIMITED)

    override fun observeToday(date: LocalDate): Flow<ObserveTodayResult> = flow {
        observedDates += date
        try {
            emitAll(observation(date))
        } finally {
            cancelledObservationDates += date
        }
    }

    override fun observeManagedHabits(): Flow<ObserveManagedHabitsResult> = flow {
        managedObservationCount += 1
        emitAll(managedObservations)
    }

    override fun observeHabit(id: HabitId): Flow<ObserveHabitResult> = flow {
        observedHabitIds += id
        emitAll(habitObservation(id))
    }

    override suspend fun createHabit(
        draft: HabitDraft,
        time: LocalDateSnapshot,
    ): CreateHabitResult {
        createHabitCalls += CreateHabitCall(draft, time)
        return receiveCreateResult()
    }

    override suspend fun createDailyHabit(
        name: String,
        time: LocalDateSnapshot,
    ): CreateHabitResult {
        createCalls += CreateCall(name, time)
        return receiveCreateResult()
    }

    private suspend fun receiveCreateResult(): CreateHabitResult =
        try {
            createResults.receive()
        } catch (error: CancellationException) {
            cancelledCreateCallCount += 1
            throw error
        }

    override suspend fun completeHabit(
        habitId: HabitId,
        time: LocalDateSnapshot,
    ): CompleteHabitResult {
        completeCalls += CompleteCall(habitId, time)
        return try {
            completionResults.receive()
        } catch (error: CancellationException) {
            cancelledCompleteCallCount += 1
            throw error
        }
    }

    override suspend fun updateHabit(
        id: HabitId,
        draft: HabitDraft,
        time: LocalDateSnapshot,
    ): UpdateHabitResult {
        updateCalls += UpdateCall(id, draft, time)
        return updateResults.receive()
    }

    override suspend fun archiveHabit(
        id: HabitId,
        time: LocalDateSnapshot,
    ): ArchiveHabitResult {
        archiveCalls += LifecycleCall(id, time)
        return archiveResults.receive()
    }

    override suspend fun restoreHabit(
        id: HabitId,
        time: LocalDateSnapshot,
    ): RestoreHabitResult {
        restoreCalls += LifecycleCall(id, time)
        return restoreResults.receive()
    }

    override suspend fun deleteHabitAndHistory(id: HabitId): DeleteHabitResult {
        deleteCalls += id
        return deleteResults.receive()
    }

    override suspend fun clearAllHabitData(): ClearHabitDataResult {
        clearCallCount += 1
        return clearResults.receive()
    }

    fun emitToday(
        date: LocalDate,
        result: ObserveTodayResult,
    ) {
        check(observation(date).tryEmit(result))
    }

    fun enqueueCreateResult(result: CreateHabitResult) {
        check(createResults.trySend(result).isSuccess)
    }

    fun emitManagedHabits(result: ObserveManagedHabitsResult) {
        check(managedObservations.tryEmit(result))
    }

    fun emitHabit(
        id: HabitId,
        result: ObserveHabitResult,
    ) {
        check(habitObservation(id).tryEmit(result))
    }

    fun enqueueUpdateResult(result: UpdateHabitResult) {
        check(updateResults.trySend(result).isSuccess)
    }

    fun enqueueCompletionResult(result: CompleteHabitResult) {
        check(completionResults.trySend(result).isSuccess)
    }

    fun enqueueArchiveResult(result: ArchiveHabitResult) {
        check(archiveResults.trySend(result).isSuccess)
    }

    fun enqueueRestoreResult(result: RestoreHabitResult) {
        check(restoreResults.trySend(result).isSuccess)
    }

    fun enqueueDeleteResult(result: DeleteHabitResult) {
        check(deleteResults.trySend(result).isSuccess)
    }

    fun enqueueClearResult(result: ClearHabitDataResult) {
        check(clearResults.trySend(result).isSuccess)
    }

    private fun observation(date: LocalDate): MutableSharedFlow<ObserveTodayResult> =
        observations.getOrPut(date) {
            MutableSharedFlow(
                replay = 1,
                extraBufferCapacity = 1,
            )
        }

    private fun habitObservation(id: HabitId): MutableSharedFlow<ObserveHabitResult> =
        habitObservations.getOrPut(id) {
            MutableSharedFlow(
                replay = 1,
                extraBufferCapacity = 1,
            )
        }
}
