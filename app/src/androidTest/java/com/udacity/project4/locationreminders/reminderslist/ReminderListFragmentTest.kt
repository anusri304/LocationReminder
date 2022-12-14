package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import  com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.Mockito.verify

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing

class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    // use koin for testing to get singleton instance
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext =  ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get  repository
        repository = get()

        //clear the data
        runBlocking {
            repository.deleteAllReminders()

        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addReminderFABButton_click_navigateToSaveReminderFragment() {

        //Given on the ReminderListFragment
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //When clicking on Add FAB Button
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        // Then Launch the Save  Reminder Fragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

    @Test
    fun reminderListFragment_launch_checkDataDisplayed()  {
        //Given the reminder exist in database
        val reminderDto = createTestReminder()
        runBlocking {
            repository.saveReminder(reminderDto)

        }
        //When the reminder fragment is launched
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //Then the Recyclerview has a textview with fake title as text
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("Fake title"))));

    }

    @Test
     fun reminderListFragment_launch_checkNoDataDisplayed() =  runBlockingTest{
        //Given no data exist in database
        //When the reminder fragment is launched
        val fragmentScenario =launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        //Then No Data is displayed
        onView(withText(R.string.no_data))
            .check(matches(ViewMatchers.isDisplayed()))

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