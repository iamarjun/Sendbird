package com.arjun.sendbird.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val connectionDataSource: ConnectionDataSource,
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginScreenState(isLoading = false))
    val loginState = _loginState.asStateFlow()

    fun login(userId: String) {
        viewModelScope.launch {
            connectionDataSource.connect(userId)
                .map { isUserLoggedIn ->
                    LoginScreenState(
                        isLoading = true,
                        isUserLoggedIn = isUserLoggedIn
                    )
                }
                .onStart { LoginScreenState(isLoading = true) }
                .catch {
                    LoginScreenState(
                        isLoading = false,
                        isUserLoggedIn = false,
                        error = it
                    )
                }.collect {
                    _loginState.value = it
                }
        }
    }
}