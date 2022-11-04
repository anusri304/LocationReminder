package com.udacity.project.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project.locationreminders.data.FakeDataSource
import com.udacity.project.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest: AutoCloseKoinTest() {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var fakeReminderDataSource: FakeDataSource
    private lateinit var remindersViewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        fakeReminderDataSource = FakeDataSource()
        remindersViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminderDataSource)
    }

    @Test
    fun testShouldReturnError () = runBlockingTest  {
        fakeReminderDataSource.setReturnError(true)
        saveReminderData()
        remindersViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersViewModel.showSnackBar.value, CoreMatchers.`is`("No Reminders Found")
        )
    }

    @Test
    fun check_loading() = runBlockingTest {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Save Data
        saveReminderData()

        remindersViewModel.loadReminders()

        // Show Loading will be true as dispatcher is paused
        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(true))


        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Show Loading will be false as dispatcher is resumed
        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    private suspend fun saveReminderData() {
        fakeReminderDataSource.saveReminder(
            ReminderDTO(
                "Fake title",
                "Fake description abc",
                "location abc",
                102.00,
                109.00)
        )
    }
}