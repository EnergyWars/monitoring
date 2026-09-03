package com.wafflehq.monitoring.di

import com.wafflehq.monitoring.background.NotificationHelper
import com.wafflehq.monitoring.background.Notifier
import com.wafflehq.monitoring.data.monitoring.CheckResultsRepository
import com.wafflehq.monitoring.data.monitoring.MonitorHttpClient
import com.wafflehq.monitoring.data.monitoring.MonitoredPagesRepository
import com.wafflehq.monitoring.data.monitoring.OkHttpMonitorClient
import com.wafflehq.monitoring.data.monitoring.RoomCheckResultsRepository
import com.wafflehq.monitoring.data.monitoring.RoomMonitoredPagesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindMonitoredPagesRepository(impl: RoomMonitoredPagesRepository): MonitoredPagesRepository

    @Binds
    @Singleton
    abstract fun bindCheckResultsRepository(impl: RoomCheckResultsRepository): CheckResultsRepository

    @Binds
    @Singleton
    abstract fun bindMonitorHttpClient(impl: OkHttpMonitorClient): MonitorHttpClient

    @Binds
    @Singleton
    abstract fun bindNotifier(impl: NotificationHelper): Notifier
}
