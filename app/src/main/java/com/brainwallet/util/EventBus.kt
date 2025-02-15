package com.brainwallet.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {

    private val _events = MutableSharedFlow<Event>(
        replay = 0, // No replays, emit only to active collectors
        extraBufferCapacity = 1, // Buffer size of 1 to prevent backpressure issues
        onBufferOverflow = BufferOverflow.DROP_OLDEST // Drop oldest when buffer overflows
    )

    val events: SharedFlow<Event> = _events.asSharedFlow()

    suspend fun emit(event: Event) {
        _events.emit(event)
    }


    sealed class Event {
        data class Message(val message: String) : Event()
    }
}

