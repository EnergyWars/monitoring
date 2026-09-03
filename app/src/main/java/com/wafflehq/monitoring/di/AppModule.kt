package com.wafflehq.monitoring.di

import android.content.Context
import androidx.room.Room
import com.wafflehq.monitoring.data.db.AppDatabase
import com.wafflehq.monitoring.data.db.CheckResultDao
import com.wafflehq.monitoring.data.db.MonitoredPageDao
import com.wafflehq.monitoring.data.features.FeatureFilesRepository
import com.wafflehq.monitoring.data.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideMonitoredPageDao(db: AppDatabase): MonitoredPageDao = db.monitoredPageDao()

    @Provides
    fun provideCheckResultDao(db: AppDatabase): CheckResultDao = db.checkResultDao()

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext ctx: Context): SettingsRepository =
        SettingsRepository(ctx)

    @Provides
    @Singleton
    fun provideFeatureFilesRepository(@ApplicationContext ctx: Context): FeatureFilesRepository =
        FeatureFilesRepository(ctx)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()
}
