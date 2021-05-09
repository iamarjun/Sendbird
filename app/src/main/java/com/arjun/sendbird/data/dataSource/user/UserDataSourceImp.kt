package com.arjun.sendbird.data.dataSource.user

import com.sendbird.android.ApplicationUserListQuery
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.UserListQuery
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserDataSourceImp @Inject constructor(): UserDataSource {

    private suspend fun userListQuery(listQuery: ApplicationUserListQuery): List<User> {
        return suspendCoroutine { continuation ->
            val userListQueryResultHandler =
                UserListQuery.UserListQueryResultHandler { users, error ->
                    if (error != null) {
                        Timber.e(error)
                        continuation.resumeWithException(error)
                    } else {
                        continuation.resume(users)
                    }
                }
            listQuery.next(userListQueryResultHandler)
        }
    }

    override fun observeUserOnlinePresence(userId: String?): Flow<Boolean> = flow {

        userId ?: return@flow

        try {
            while (true) {
                val listQuery = SendBird.createApplicationUserListQuery()
                listQuery.setUserIdsFilter(listOf(userId))
                val user = userListQuery(listQuery)[0]
                emit(user.connectionStatus == User.ConnectionStatus.ONLINE)
                delay(1000)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
