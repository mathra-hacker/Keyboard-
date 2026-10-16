package ime.imeui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DebouncedInputHandler(private val scope: CoroutineScope, private val debounceMs: Long = 700L) {

    private var debounceJob: Job? = null

    fun onTyping(token: String, onTypingImmediate: (String) -> Unit, onIdle: (() -> Unit)? = null) {
        onTypingImmediate(token)
        debounceJob?.cancel()
        if (onIdle != null) {
            debounceJob = scope.launch {
                delay(debounceMs)
                onIdle()
            }
        }
    }

    fun cancel() {
        debounceJob?.cancel()
        debounceJob = null
    }
}

