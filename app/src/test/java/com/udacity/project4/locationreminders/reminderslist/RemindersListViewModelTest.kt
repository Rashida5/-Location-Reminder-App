package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.MainCoroutineRule
import com.udacity.project4.fakedata.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

//use ExperimntalCoroutineApi beacuse function implemented in fake datasource is suspend
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest{
    //get fresh viewmodel
    private lateinit var RemindersListViewModel:RemindersListViewModel
   // val testDispatcher:TestCoroutineDispatcher= TestCoroutineDispatcher()


    //get fresh fake datasource
    private lateinit var DataSource:FakeDataSource

    //make the test in the same thread
    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule=MainCoroutineRule()
    //To test in Dispatchers.Main
    //@get:Rule

    //add data to Fakedata source
   val remider_1= ReminderDTO("First","FirstDesc","FirstLoc",11.000,11.00)
    val remider_2= ReminderDTO("Second","SecondDesc","secondLoc",22.000,22.00)
    val remider_3= ReminderDTO("Third","ThirdDesc","ThirdLoc",33.000,33.00)
    val remider_4= ReminderDTO("Fourth","FourthDesc","FourthLoc",44.000,44.00)
    val remider_5= ReminderDTO("Fifth","FifthDesc","FifthLoc",55.000,55.00)
    @After
    fun tearDown() {
        stopKoin()
    }


    @Test
    fun `load Reminders and assert that list in view model not empty`(){
        //Given view model and datasource
        val ListOfReminders= mutableListOf(remider_1,remider_2,remider_3,remider_4,remider_5)
        DataSource= FakeDataSource(ListOfReminders)
        RemindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(), DataSource)

        //When load the reminders in ListofReminder in viewModel
        RemindersListViewModel.loadReminders()

        //Then the reminders in ListofReminder in viewModel not empty
        assertThat(RemindersListViewModel.remindersList.getOrAwaitValue(),(not(emptyList())))
    }

    @Test
    fun `load null and assert that Error message appear`(){
        //Given the viewmodel and dataSource
        DataSource= FakeDataSource(null)
       DataSource.set_shouldReturnError(true)
       RemindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),DataSource)
   //     mainCoroutineRule.pauseDispatcher()
        //When load the reminders in ListofReminders in viewModel
        RemindersListViewModel.loadReminders()
       //Then Error meassgae appear
        assertThat(RemindersListViewModel.showSnackBar.getOrAwaitValue(),`is`("Test exception"))
    }


    @Test
    fun `load empty list of reminders ans assert show Loading`(){
        //Given DataSource and Viewmodel
        DataSource= FakeDataSource(mutableListOf())  //Add empty list
        RemindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),DataSource)

        mainCoroutineRule.pauseDispatcher()

        //when load the reminders in listOfReminders in viewModel
        RemindersListViewModel.loadReminders()

        //Then assert showLoading equal true

        assertThat(RemindersListViewModel.showLoading.getOrAwaitValue(),`is`(true))

    }


}