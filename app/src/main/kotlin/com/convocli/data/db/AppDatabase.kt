package com.convocli.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.convocli.data.model.Command

/**
 * ConvoCLI Room database.
 *
 * Version 1: Initial schema with Command entity for terminal history.
 */
@Database(
    entities = [Command::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
