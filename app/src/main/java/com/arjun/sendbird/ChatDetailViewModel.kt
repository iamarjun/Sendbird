package com.arjun.sendbird

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ChatDetailViewModel : ViewModel() {
    fun add(value: String) {
        messages.value = messages.value.toMutableList().apply {
            add(value)
        }
    }

    val messages by lazy {
        mutableStateOf(
            listOf(
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
                "Arjun",
            )
        )
    }
}