package com.convocli.data.db

import androidx.room.TypeConverter
import com.convocli.data.model.CommandStatus

/**
 * Room type converters for custom types in the database.
 *
 * Converts non-primitive types (enums) to/from types that Room can persist.
 */
class Converters {
    /**
     * Converts CommandStatus enum to String for database storage.
     */
    @TypeConverter
    fun fromCommandStatus(status: CommandStatus): String = status.name

    /**
     * Converts String from database to CommandStatus enum.
     */
    @TypeConverter
    fun toCommandStatus(value: String): CommandStatus = CommandStatus.valueOf(value)
}
