package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var DataBase:RemindersDatabase
   private lateinit var Dao:RemindersDao
 val Reminder_1=ReminderDTO("Dao","FirstDesc","FirstLoc",11.00,22.00)
    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()
   @Before
   fun set_up(){
       DataBase=Room.inMemoryDatabaseBuilder(
           ApplicationProvider.getApplicationContext(),
           RemindersDatabase::class.java
       ).allowMainThreadQueries().build()
       Dao=DataBase.reminderDao()

   }
    @After
    fun teardown(){
        DataBase.close()
    }
    @Test
    fun insertReminder()= runBlockingTest {

        //Given Reminder
        Dao.saveReminder(Reminder_1)
        //Get the reminder by id
        val Reminder_Add=Dao.getReminderById(Reminder_1.id)

         //Then the the data added as Reminder_1
        assertThat<ReminderDTO>(Reminder_Add as ReminderDTO,notNullValue())
        assertThat(Reminder_Add.id,`is`(Reminder_1.id))
        assertThat(Reminder_Add.title,`is`(Reminder_1.title))
        assertThat(Reminder_Add.location,`is`(Reminder_1.location))
        assertThat(Reminder_Add.latitude,`is`(Reminder_1.latitude))
        assertThat(Reminder_Add.longitude,`is`(Reminder_1.longitude))


    }



}