package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun createRepository() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        repository = RemindersLocalRepository(database.reminderDao())


    }

    @After
    fun cleanUp() = database.close()

    // If we dont use runBlocking we get job not completed yet error
    @Test
    fun reminder_insertAndFetch_returnsReminder() = runBlocking {
        // GIVEN - a new reminder is saved in the database
        val reminderDto = createFakeReminder()
        repository.saveReminder(reminderDto)

        // WHEN  - Reminders are retrieved from database
        val result = repository.getReminders() as Result.Success

         //Then same reminders is returned
        assertThat(result.data[0].id, Matchers.`is`(reminderDto.id))
        assertThat(result.data[0].title, Matchers.`is`(reminderDto.title))
        assertThat(result.data[0].description, Matchers.`is`(reminderDto.description))
        assertThat(result.data[0].latitude, Matchers.`is`(reminderDto.latitude))
        assertThat(result.data[0].longitude, Matchers.`is`(reminderDto.longitude))
        assertThat(result.data[0].longitude, Matchers.`is`(reminderDto.longitude))
    }

    @Test
    fun fakeReminder_fetch_returnError() = runBlocking {
        // GIVEN - No REminders exist in database
        //When a reminder with id 765 is retrieved
        val result = repository.getReminder("765") is Result.Error
        //Then Error is displayed
        assertThat(result, CoreMatchers.`is`(true))
    }


    fun createFakeReminder(): ReminderDTO {
        return ReminderDTO(
            "Fake title",
            "Fake description abc",
            "Test location abc",
            102.00,
            109.00
        )
    }

}