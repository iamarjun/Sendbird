package com.arjun.sendbird

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.sendbird.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectionManager: ConnectionManager,
) : ViewModel() {

    private val _userId by lazy { MutableLiveData<String>() }
    val userId: LiveData<String>
        get() = _userId


    fun onUserIdChange(newUserId: String) {
        _userId.value = newUserId
    }

    private val _userExist by lazy { MutableLiveData<Resource<Boolean>>() }
    val userExist: LiveData<Resource<Boolean>>
        get() = _userExist


    @ExperimentalCoroutinesApi
    val connectionStatus = connectionManager.observeConnection()

    fun login(userId: String) {
        viewModelScope.launch {
            _userExist.value = (Resource.Loading())
            try {
                val exist = connectionManager.connect(userId = userId)
                _userExist.value = (Resource.Success(exist))
            } catch (e: Exception) {
                _userExist.value = (Resource.Error(e))
            }

        }
    }

}