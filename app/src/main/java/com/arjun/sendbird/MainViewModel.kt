package com.arjun.sendbird

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectionManager: ConnectionManager,
) : ViewModel() {

    private val _userExist by lazy { MutableSharedFlow<Boolean>() }
    val userExist = _userExist.asSharedFlow()

    @ExperimentalCoroutinesApi
    val connectionStatus = connectionManager.observeConnection()

    fun login(userId: String) {
        viewModelScope.launch {
            try {
                val exist = connectionManager.connect(userId = userId)
                _userExist.emit(exist)
            } catch (e: Exception) {
                _userExist.emit(false)
            }

        }
    }

}