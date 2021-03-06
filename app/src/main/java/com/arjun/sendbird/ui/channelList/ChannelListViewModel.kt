package com.arjun.sendbird.ui.channelList

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.dataSource.ChannelDataSource
import com.sendbird.android.BaseChannel
import com.sendbird.android.GroupChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val channelDataSource: ChannelDataSource
) : ViewModel() {

    private val _channels by lazy { mutableStateOf<List<GroupChannel>?>(null) }

    val channels: State<List<GroupChannel>?>
        get() = _channels

    init {
        viewModelScope.launch {
            val channels = channelDataSource.loadChannels()
            _channels.value = channels
        }
    }

}