package com.convocli.di

import android.content.Context
import androidx.room.Room
import com.convocli.data.db.AppDatabase
import com.convocli.data.db.CommandBlockDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 *
 * Provides Room database and DAOs as singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     *
     * Database is built with:
     * - Name: "convocli-database"
     * - Location: Internal app storage
     * - Schema export enabled for migrations
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "convocli-database"
        ).build()
    }

    /**
     * Provides CommandBlockDao from the database.
     *
     * @param database The app database instance
     * @return DAO for command block operations
     */
    @Provides
    fun provideCommandBlockDao(database: AppDatabase): CommandBlockDao {
        return database.commandBlockDao()
    }
}
