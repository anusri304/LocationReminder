package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun reminder_insertAndFetch_returnsReminder() = runBlockingTest {
        // GIVEN - a new reminder is saved in the database
        val reminderDto = createTestReminder()
        database.reminderDao().saveReminder(reminderDto)

        // WHEN  - Reminders are retrieved from database
        val retrievedReminders = database.reminderDao().getReminders()

        val retrievedReminder = retrievedReminders[0]

        //Then same reminder is returned
        assertThat(retrievedReminder.id, `is`(reminderDto.id))
        assertThat(retrievedReminder.title, `is`(reminderDto.title))
        assertThat(retrievedReminder.description, `is`(reminderDto.description))
        assertThat(retrievedReminder.latitude, `is`(reminderDto.latitude))
        assertThat(retrievedReminder.longitude, `is`(reminderDto.longitude))
        assertThat(retrievedReminder.longitude, `is`(reminderDto.longitude))
    }


    fun createTestReminder(): ReminderDTO {
        return ReminderDTO(
            "Fake title",
            "Fake description abc",
            "location abc",
            102.00,
            109.00
        )
    }
}