package com.convocli

import com.convocli.data.model.Command
import java.util.UUID

/**
 * Test data builders for common entities.
 *
 * Provides default values with overrides for specific test cases.
 */

fun commandEntity(
    id: Long = 0,
    commandText: String = "ls -la",
    output: String? = null,
    exitCode: Int? = null,
    executedAt: Long = System.currentTimeMillis(),
    workingDirectory: String = "/home",
    sessionId: String? = null
) = Command(
    id = id,
    commandText = commandText,
    output = output,
    exitCode = exitCode,
    executedAt = executedAt,
    workingDirectory = workingDirectory,
    sessionId = sessionId
)
