package com.udacity.project

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project.locationreminders.RemindersActivity
import com.udacity.project.locationreminders.data.ReminderDataSource
import com.udacity.project.locationreminders.data.local.LocalDB
import com.udacity.project.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project.util.DataBindingIdlingResource
import com.udacity.project.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
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

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

   private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
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
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

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
    fun saveReminderButton_click_displaySelectLocationSnackbarMessage() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Fake Title1"), ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Fake Desc 1"), ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        val snackBarMessage = appContext.getString(R.string.err_select_location)


        Espresso.onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun saveReminderButton_click_displayReminderSavedToastMessage() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("Fake Title"))
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("Fake Description"))
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.map)).perform(ViewActions.longClick())
        Espresso.onView(withId(R.id.saveLocationButton)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(
                    CoreMatchers.`is`(
                        getActivity(activityScenario).window.decorView
                    )
                )
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

}
