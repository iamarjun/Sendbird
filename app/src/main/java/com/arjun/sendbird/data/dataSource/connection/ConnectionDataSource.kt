package com.arjun.sendbird.data.dataSource.connection

import kotlinx.coroutines.flow.Flow

interface ConnectionDataSource {

    suspend fun connect(userId: String? = null): Boolean

    suspend fun disconnect(onDisconnect: () -> Unit)

    fun observeConnection(): Flow<Boolean>
}