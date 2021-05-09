package com.arjun.sendbird.data.dataSource.user

import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    fun observeUserOnlinePresence(userId: String?): Flow<Boolean>
}