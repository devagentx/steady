package ai.monkmind.steady.reset

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppMutationMutex {
    private val mutex = Mutex()

    suspend fun <T> withLock(action: suspend () -> T): T =
        mutex.withLock {
            action()
        }
}
