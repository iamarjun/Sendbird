package com.arjun.sendbird.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSource
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import com.arjun.sendbird.ui.channelList.ChannelListState
import com.arjun.sendbird.ui.login.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendbirdViewModel @Inject constructor(
    private val connectionDataSource: ConnectionDataSource,
    private val channelDataSource: ChannelDataSource
) : ViewModel() {

    private val loading = MutableStateFlow(false)

    private val _loginState = MutableStateFlow(LoginScreenState(isLoading = false))
    val loginState = _loginState.asStateFlow()

    fun login(userId: String) {
        viewModelScope.launch {
            try {
                combine(
                    //TODO: expose flow instead of this hack
                    flowOf(connectionDataSource.connect(userId)),
                    loading,
                ) { isUserLoggedIn, loading ->
                    LoginScreenState(
                        isLoading = loading,
                        isUserLoggedIn = isUserLoggedIn
                    )
                }.catch {
                    LoginScreenState(
                        isLoading = false,
                        isUserLoggedIn = false,
                        error = it
                    )
                }.collect {
                    _loginState.value = it
                }
            } catch (e: Exception) {
                //TODO: Workaround for not exposing a flow in the first place, to catch when the ConnectionDataSource::connect throws an error
                _loginState.value = LoginScreenState(
                    isLoading = false,
                    isUserLoggedIn = false,
                    error = e
                )
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            connectionDataSource.disconnect {
                onLogout()
            }
        }
    }

    private val _channelListState = MutableStateFlow(ChannelListState(loading = true))
    val channelListState = _channelListState.asStateFlow()


    fun getChannels() {
        viewModelScope.launch {
            combine(
                loading,
                flowOf(channelDataSource.loadChannels())
            ) { loading, channels ->
                ChannelListState(
                    loading = loading,
                    channelList = channels
                )
            }.catch {
                throw it
            }.collect {
                _channelListState.value = it
            }
        }
    }

}
