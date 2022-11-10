package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeReminderDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        fakeReminderDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminderDataSource
        )
    }

    @Test
    fun getReminder_validate_displayError() = runBlockingTest {
        // Initialise the viewmodel with incomplete reminder
        val result = saveReminderViewModel.validateEnteredData(createIncompleteReminderDataItem())
        //Assert that the validation fails
        MatcherAssert.assertThat(result, CoreMatchers.`is`(false))
    }


    @Test
    fun testLoading_showHide() = runBlockingTest {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()
        // Save Data
        saveReminderViewModel.saveReminder(createIncompleteReminderDataItem())

        // Show Loading will be true as dispatcher is paused
        assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(true))
        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Show Loading will be false as dispatcher is resumed
        assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    private fun createIncompleteReminderDataItem(): ReminderDataItem {
        return ReminderDataItem(
            "",
            "Fake Reminder Description",
            "Fake LOcation",
            102.00,
            109.00
        )
    }
}