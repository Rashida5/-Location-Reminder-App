package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var DataSourceRepo:RemindersLocalRepository
    private lateinit var DataBase:RemindersDatabase
    private  var Reminder_1= ReminderDTO("Dao","FirstDesc","FirstLoc",11.00,22.00)
    @get:Rule
    var instantTaskExecutorRule=InstantTaskExecutorRule()

    @Before
    fun set_up(){
        DataBase= Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
      DataSourceRepo= RemindersLocalRepository(DataBase.reminderDao(),Dispatchers.Main)
    }
    @After
    fun teardown(){
        DataBase.close()
    }
    @Test
    fun check_if_the_reminder_save_is_retrived()= runBlocking {
        // GIVEN - A new Reminder saved in the database.
        DataSourceRepo.saveReminder(Reminder_1)

        // WHEN  - Reminder retrieved by ID.
         val Result=DataSourceRepo.getReminder(Reminder_1.id)

        Result as Result.Success

    // THEN - Same Reminder is returned.
       Assert.assertThat(Result.data.id,`is`(Reminder_1.id))
        Assert.assertThat(Result.data.title,`is`(Reminder_1.title))
        Assert.assertThat(Result.data.description,`is`(Reminder_1.description))
        Assert.assertThat(Result.data.latitude,`is`(Reminder_1.latitude))
        Assert.assertThat(Result.data.longitude,`is`(Reminder_1.longitude))
    }
    //Reminder not found!
    @Test
    fun return_error_if_id_not_exist_in_data_add()= runBlocking{
        // GIVEN - A new Reminder saved in the database.
        DataSourceRepo.saveReminder(Reminder_1)
        // WHEN  - Reminder retrieved by ID.

        val Result=DataSourceRepo.getReminder("100000000")
         Result as Result.Error


        //THEN - Error message is returned.

        Assert.assertThat(Result.message,`is`("Reminder not found!"))
    }
    //Delete Reminders
    @Test
    fun return_dataReminders_is_empty_if_all_Reminders_delet()= runBlocking{
       //GIVEN - Delte all reminders
        DataSourceRepo.deleteAllReminders()

        //WHEN upload the data in repo
        val uploadData=DataSourceRepo.getReminders()

        uploadData as Result.Success
        Assert.assertThat(uploadData.data.isEmpty(),`is`(true))
    }



}