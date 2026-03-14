package com.chrona.app.di

import android.content.Context
import androidx.room.Room
import com.chrona.app.data.db.ChronaDatabase
import com.chrona.app.data.db.dao.AlarmDao
import com.chrona.app.data.db.dao.TimerPresetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChronaDatabase =
        Room.databaseBuilder(context, ChronaDatabase::class.java, "chrona.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTimerPresetDao(db: ChronaDatabase): TimerPresetDao = db.timerPresetDao()

    @Provides
    fun provideAlarmDao(db: ChronaDatabase): AlarmDao = db.alarmDao()
}
