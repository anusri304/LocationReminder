package com.udacity.project.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project.locationreminders.data.ReminderDataSource
import com.udacity.project.locationreminders.data.local.LocalDB
import com.udacity.project.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project.util.DataBindingIdlingResource
import com.udacity.project.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import org.mockito.Mockito.mock
import  com.udacity.project.R
import com.udacity.project.locationreminders.data.dto.ReminderDTO
import com.udacity.project.util.RecyclerUtil
import kotlinx.coroutines.test.runBlockingTest

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing

class ReminderListFragmentTest : AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

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
    fun clickAddReminderFABButton_navigateToSaveReminderFragment() {

        //Given on the ReminderListFragment
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //When clicking on Add FAB Button
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        // Then Launch the Save  Reminder Fragment
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

//    @Test
//    suspend fun launchReminderListFragment_checkDataDisplayed()  {
//        val reminderDto = createFakeReminder()
//        repository.saveReminder(reminderDto)
//        val fragmentScenario =
//            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        dataBindingIdlingResource.monitorFragment(fragmentScenario)
//
//
//
//       // Espresso.onView(withId(R.id.reminderssRecyclerView)).check(matches(atPosition(0, withText("Test Text"))));
//
//        onView(withId(R.id.reminderssRecyclerView))
//            .check(matches(RecyclerUtil.atPosition(0, hasDescendant(withText("Fake title")))));
//    }

    @Test
     fun launchReminderListFragment_checkNoDataDisplayed() =  runBlockingTest{
        val fragmentScenario =launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText(R.string.no_data))
            .check(matches(ViewMatchers.isDisplayed()))

    }

//    fun createFakeReminder(): ReminderDTO {
//        return ReminderDTO(
//            "Fake title",
//            "Fake description abc",
//            "location abc",
//            102.00,
//            109.00
//        )
//    }
}