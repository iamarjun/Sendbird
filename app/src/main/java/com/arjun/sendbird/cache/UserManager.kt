package com.arjun.sendbird.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(
    name = "user_pref"
)

class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit {
            it[USER_ID] = userId
        }
    }

    fun getUserId(): Flow<String> = context.dataStore.data.map {
        it[USER_ID] ?: ""
    }

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
    }
}