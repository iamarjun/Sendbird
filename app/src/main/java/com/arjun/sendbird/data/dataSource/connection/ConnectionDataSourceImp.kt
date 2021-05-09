package com.arjun.sendbird.data.dataSource.connection

import com.arjun.sendbird.cache.UserManager
import com.arjun.sendbird.util.getAccessToken
import com.sendbird.android.SendBird
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ConnectionDataSourceImp @Inject constructor(
    private val userManager: UserManager
) : ConnectionDataSource {

    @ExperimentalCoroutinesApi
    override suspend fun connect(userId: String?): Boolean {
        val id = userId ?: userManager.getUserId().first()
        return suspendCancellableCoroutine { continuation ->
            SendBird.connect(userId, getAccessToken(userId = id)) { user, error ->
                if (error != null) {
                    Timber.e(error)
                    continuation.resumeWithException(error)
                } else {
                    continuation.resume(user != null)
                }
            }
        }
    }

    override suspend fun disconnect(onDisconnect: () -> Unit) {
        SendBird.disconnect {
            onDisconnect()
        }
        userManager.clearUser()
    }

    @ExperimentalCoroutinesApi
    override fun observeConnection(): Flow<Boolean> {

        return callbackFlow {
            val channelHandler = object : SendBird.ConnectionHandler {

                override fun onReconnectStarted() {}

                override fun onReconnectSucceeded() {
                    offer(true)
                }

                override fun onReconnectFailed() {}
            }

            SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, channelHandler)

            when (SendBird.getConnectionState()) {
                SendBird.ConnectionState.OPEN -> offer(false)

                SendBird.ConnectionState.CLOSED -> offer(
                    connect(
                        userManager.getUserId().first()
                    )
                )

                else -> {
                }
            }

            awaitClose { SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID) }
        }
    }

    companion object {
        private const val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_ID"
    }
}