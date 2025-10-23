package com.convocli.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.convocli.data.model.CommandBlock

/**
 * Room database for ConvoCLI.
 *
 * Contains all entities for local data persistence:
 * - CommandBlock: Command execution history
 *
 * Database version is incremented when schema changes.
 * Export schema is enabled for migration testing.
 */
@Database(
    entities = [CommandBlock::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * DAO for command block operations.
     */
    abstract fun commandBlockDao(): CommandBlockDao
}
