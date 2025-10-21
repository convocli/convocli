package com.convocli.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a terminal command execution.
 *
 * Stores command history for replay, search, and UI display.
 */
@Entity(
    tableName = "commands",
    indices = [
        Index(value = ["executed_at"], name = "idx_executed_at"),
        Index(value = ["session_id"], name = "idx_session_id")
    ]
)
data class Command(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "command_text")
    val commandText: String,

    @ColumnInfo(name = "output")
    val output: String? = null,

    @ColumnInfo(name = "exit_code")
    val exitCode: Int? = null,

    @ColumnInfo(name = "executed_at")
    val executedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "working_directory")
    val workingDirectory: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String? = null
)
