package com.udacity.project4.locationreminders.reminderlist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import com.util.DataBindingIdlingResource
import com.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//Integreted Test
@MediumTest
class ReminderListFragmentTest :AutoCloseKoinTest() {
    private lateinit var DataSource: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun start_up() {
        stopKoin() //stop app Koin
        appContext = getApplicationContext()
        val MyModule = module {
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //Start koin of test by passing the module we create
        startKoin {
            modules(listOf(MyModule))
        }
        DataSource = get()
        runBlocking {
            DataSource.deleteAllReminders()
        }
    }
    @Before
    fun registerIdlingResource() {
       // IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
 //       IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }



    @Test
    fun Clickbutton_NavigatetoAddReminder()= runBlockingTest{

        //Given Lunched of ReminderListfragment
        val Scenario= launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(Scenario)
        val NavController = mock(NavController::class.java)
        Scenario.onFragment {
            Navigation.setViewNavController(it.view!!,NavController)
        }
        //When click on the button
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Then navigate to Add fragment
        verify(NavController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun reminderList_DisplayedUiWithData() = runBlockingTest {
        //Given Data
        val Reminder_1 = ReminderDTO("First", "Firstdesc", "FirstLoc", 11.0000, 22.00)
        val Reminder_2 = ReminderDTO("Second", "Seconddesc", "SecondLoc", 33.0000, 44.00)
        val Reminder_3 = ReminderDTO("Third", "Thirddesc", "ThirdLoc", 55.0000, 66.00)
        runBlocking {
            DataSource.saveReminder(Reminder_1)
        }
        runBlocking {
            DataSource.saveReminder(Reminder_2)
        }
        runBlocking {
            DataSource.saveReminder(Reminder_3)
        }
        //When Launch Fragment

        val Scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(Scenario)
        val NavController = mock(NavController::class.java)
        Scenario.onFragment {
            Navigation.setViewNavController(it.view!!, NavController)
        }
        //Then Data appear
        onView(withText(Reminder_1.title)).check(matches(isDisplayed()))
        onView(withText(Reminder_1.description)).check(matches(isDisplayed()))
        onView(withText(Reminder_1.location)).check(matches(isDisplayed()))


        onView(withText(Reminder_2.title)).check(matches(isDisplayed()))
        onView(withText(Reminder_2.description)).check(matches(isDisplayed()))
        onView(withText(Reminder_2.location)).check(matches(isDisplayed()))


        onView(withText(Reminder_3.title)).check(matches(isDisplayed()))
        onView(withText(Reminder_3.description)).check(matches(isDisplayed()))
        onView(withText(Reminder_3.location)).check(matches(isDisplayed()))
        Thread.sleep(10000)

    }

    @Test
    fun reminderList_DisplayedUIWithNoData() = runBlockingTest {
    //Given empty Database : no data {already database is empty}
 val Scenario= launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(Scenario)
   val NavController= mock(NavController::class.java)
    //When launch fragment
        Scenario.onFragment {
            Navigation.setViewNavController(it.view!!,NavController)
        }
       //Then Text appear
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        Thread.sleep(10000)
    }

}