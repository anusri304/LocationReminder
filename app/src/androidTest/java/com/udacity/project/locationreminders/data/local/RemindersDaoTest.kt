package com.udacity.project.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
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
     fun testInsertReminderandGetReminders() = runBlockingTest{
        val reminderDto = createFakeReminder()
       database.reminderDao().saveReminder(reminderDto)

        val loadedReminderList = database.reminderDao().getReminders()

        val loadedReminder = loadedReminderList[0]

        MatcherAssert.assertThat<ReminderDTO>(loadedReminder as ReminderDTO, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(loadedReminder.id, `is`(reminderDto.id))
        MatcherAssert.assertThat(loadedReminder.title, `is`(reminderDto.title))
        MatcherAssert.assertThat(loadedReminder.description, `is`(reminderDto.description))
        MatcherAssert.assertThat(loadedReminder.latitude, `is`(reminderDto.latitude))
        MatcherAssert.assertThat(loadedReminder.longitude, `is`(reminderDto.longitude))
        MatcherAssert.assertThat(loadedReminder.longitude, `is`(reminderDto.longitude))
    }


    fun createFakeReminder():ReminderDTO {
        return  ReminderDTO(
            "Fake title",
            "Fake description abc",
            "location abc",
            102.00,
            109.00)
    }
}