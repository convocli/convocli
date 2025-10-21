package com.convocli.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.convocli.data.model.Command
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test for CommandDao Room operations.
 *
 * Uses in-memory database for isolated testing.
 */
@RunWith(AndroidJUnit4::class)
class CommandDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var commandDao: CommandDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        commandDao = database.commandDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertCommand_andRetrieve() = runTest {
        // Given
        val command = Command(
            commandText = "ls -la",
            workingDirectory = "/home",
            executedAt = System.currentTimeMillis()
        )

        // When
        val id = commandDao.insert(command)
        val retrieved = commandDao.getRecent(1).first()

        // Then
        assertEquals(command.commandText, retrieved.commandText)
        assertEquals(command.workingDirectory, retrieved.workingDirectory)
    }

    @Test
    fun observeAll_emitsFlow() = runTest {
        // Given
        val command = Command(
            commandText = "pwd",
            workingDirectory = "/home",
            executedAt = System.currentTimeMillis()
        )

        // When
        commandDao.insert(command)
        val commands = commandDao.observeAll().first()

        // Then
        assertEquals(1, commands.size)
        assertEquals("pwd", commands.first().commandText)
    }
}
