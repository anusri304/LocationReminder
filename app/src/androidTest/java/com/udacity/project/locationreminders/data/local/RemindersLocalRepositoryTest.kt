package com.udacity.project.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project.locationreminders.data.dto.ReminderDTO
import com.udacity.project.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
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
     fun testInsertReminderandGetReminders() = runBlocking{
        val reminderDto = createFakeReminder()
        repository.saveReminder(reminderDto)

        val result = repository.getReminders() as Result.Success
        MatcherAssert.assertThat(result.data != null, CoreMatchers.`is`(true))

        MatcherAssert.assertThat(result.data[0].id, Matchers.`is`(reminderDto.id))
        MatcherAssert.assertThat(result.data[0].title, Matchers.`is`(reminderDto.title))
        MatcherAssert.assertThat(result.data[0].description, Matchers.`is`(reminderDto.description))
        MatcherAssert.assertThat(result.data[0].latitude, Matchers.`is`(reminderDto.latitude))
        MatcherAssert.assertThat(result.data[0].longitude, Matchers.`is`(reminderDto.longitude))
        MatcherAssert.assertThat(result.data[0].longitude, Matchers.`is`(reminderDto.longitude))
    }

@Test
fun testReminderNotFound_returnError() = runBlocking {
    val result = repository.getReminder("111") is Result.Error
    MatcherAssert.assertThat(result, CoreMatchers.`is`(true))
}


fun createFakeReminder(): ReminderDTO {
    return ReminderDTO(
        "Fake title",
        "Fake description abc",
        "location abc",
        102.00,
        109.00
    )
}

}