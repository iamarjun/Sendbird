package com.arjun.sendbird.ui.channelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSource
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val connectionDataSource: ConnectionDataSource,
    private val channelDataSource: ChannelDataSource,
) : ViewModel() {

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
            channelDataSource.loadChannels().map { channels ->
                ChannelListState(
                    loading = false,
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