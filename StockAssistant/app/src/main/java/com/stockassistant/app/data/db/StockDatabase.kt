package com.stockassistant.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [WatchlistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StockDatabase =
        Room.databaseBuilder(context, StockDatabase::class.java, "stock_assistant.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWatchlistDao(db: StockDatabase): WatchlistDao = db.watchlistDao()
}
