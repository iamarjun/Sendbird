package com.arjun.sendbird.di

import android.content.ContentResolver
import android.content.Context
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSource
import com.arjun.sendbird.data.dataSource.channel.ChannelDataSourceImp
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSource
import com.arjun.sendbird.data.dataSource.connection.ConnectionDataSourceImp
import com.arjun.sendbird.repository.ChatRepository
import com.arjun.sendbird.repository.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository

    @Binds
    abstract fun bindConnectionDataSource(connectionDataSourceImp: ConnectionDataSourceImp): ConnectionDataSource

    @Binds
    abstract fun bindChannelDataSource(channelDataSourceImp: ChannelDataSourceImp): ChannelDataSource

    companion object {
        @Provides
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
            context.contentResolver
    }
}