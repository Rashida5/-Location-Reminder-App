package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.fakedata.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
//import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest{


    @get:Rule
    var mainCoroutineRule=MainCoroutineRule()

    @get:Rule
    var instantExecutorRule= InstantTaskExecutorRule()

    private lateinit var SaveReminderViewModel:SaveReminderViewModel
    private lateinit var DataSource:FakeDataSource
    private lateinit var appContext: Application
    var EmptyTitle=ReminderDataItem(null,"Desc","loc1",11.00,22.00)
    var EmptyLoc=ReminderDataItem("title11","Desc1",null,33.00,44.00)
    var vaild=ReminderDataItem("title12","Desc12","loc3",55.00,66.00)
    @After
    fun Shut_down(){
        stopKoin()
    }

    @Test
    fun Add_Reminder_without_title_should_return_Error(){
        //Given ViewModel and FakeDataSource
        DataSource= FakeDataSource(mutableListOf())
        SaveReminderViewModel=SaveReminderViewModel(ApplicationProvider.getApplicationContext(),DataSource)
       //When enter empty title
        SaveReminderViewModel.validateEnteredData(EmptyTitle)
 //Then SnackBar Error should appear
        assertThat(SaveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_enter_title))

    }

   @Test
    fun Add_Reminder_without_Loc_should_return_Error(){
        //Given ViewModel and FakeDataSource
        DataSource= FakeDataSource(mutableListOf())
        SaveReminderViewModel=SaveReminderViewModel(ApplicationProvider.getApplicationContext(),DataSource)
        //When enter empty title
        SaveReminderViewModel.validateEnteredData(EmptyLoc)
        //Then SnackBar Error should appear
        assertThat(SaveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_select_location))

    }

    @Test
    fun Check_Loading_when_Enter_valid_DataItem(){
        //Given ViewModel and Fake DataSoure
        DataSource= FakeDataSource(mutableListOf())
        SaveReminderViewModel=SaveReminderViewModel(ApplicationProvider.getApplicationContext(),DataSource)
        //When Enter vaild DataItem
        mainCoroutineRule.pauseDispatcher()
        SaveReminderViewModel.saveReminder(vaild)
        assertThat(SaveReminderViewModel.showLoading.getOrAwaitValue(),`is`(true))
        mainCoroutineRule.resumeDispatcher()
        //Then Load become true until DataItem save successfully in ListOfDataItem and Snackbar appear

        assertThat(SaveReminderViewModel.showLoading.getOrAwaitValue(),`is`(false))


    }
    //Tests  for only test getReminderFunction
    @Test
    fun Check_if_data_not_validate_will_not_add_to_dataSource()= runBlockingTest{
        DataSource= FakeDataSource(mutableListOf())
        SaveReminderViewModel=SaveReminderViewModel(ApplicationProvider.getApplicationContext(),DataSource)
        //When enter empty title
        SaveReminderViewModel.validateEnteredData(EmptyLoc)
        val result=DataSource.getReminder("title11")
       assertThat(result,`is`(Result.Error("Reminder not found!")))

    }
    @Test
    fun Check_if_should_Return_true_is_alreadyTrue()= runBlockingTest{
        DataSource= FakeDataSource(null)
        DataSource.set_shouldReturnError(true)
        val result=DataSource.getReminder("title11")
        assertThat(result,`is`(Result.Error("Test exception")))
    }

    //it just for test file working successfullay or not
    @Test
    fun Addition() {
        assertEquals(4, 2 + 2)
    }

}