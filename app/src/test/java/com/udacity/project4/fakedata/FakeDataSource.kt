package com.udacity.project4.fakedata

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
//Fake data source create to simulate data source by using list (datareminders)
//(FakeDataSource) is class used to implement the same operations in list
class FakeDataSource(var ListOfReminders:MutableList<ReminderDTO>?):ReminderDataSource {

    var shouldReturnError = false

    fun set_shouldReturnError(value: Boolean) {
        if (value == true) {
            shouldReturnError = true
        } else {
            shouldReturnError = false
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        ListOfReminders?.let { return Result.Success(ArrayList(it)) }


        return Result.Success(listOf())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        ListOfReminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

      /*  return try {
            val Reminder = ListOfReminders?.find { it.id == id }
            if (shouldReturnError || Reminder == null) {
                throw Exception("id Not found")
            } else {
                Result.Success(Reminder)
            }
        } catch (ex:Exception){
            Result.Error(ex.localizedMessage)
        }*/
        if(shouldReturnError){
            return Result.Error("Test exception")
        }
        ListOfReminders?.firstOrNull { it->it.id==id }?.let{
            return Result.Success(it)
        }
        return Result.Error("Reminder not found!")
    }

        override suspend fun deleteAllReminders() {
            ListOfReminders?.clear()
        }

}