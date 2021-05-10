package com.arjun.sendbird.ui.login

data class LoginScreenState(
    val isLoading: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val error: Throwable? = null
)
