package com.arjun.sendbird.data.dataSource.connection

import kotlinx.coroutines.flow.Flow

interface ConnectionDataSource {

    fun connect(userId: String? = null): Flow<Boolean>

    suspend fun disconnect(onDisconnect: () -> Unit)

    fun observeConnection(): Flow<Boolean>
}