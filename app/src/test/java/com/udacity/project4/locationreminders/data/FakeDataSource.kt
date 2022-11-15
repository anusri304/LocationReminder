package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderDTOList = mutableListOf<ReminderDTO>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {
            if (shouldReturnError) {
                return Result.Error(("Exception retrieving reminders.Unable to retrieve reminders"))
            }
            return Result.Success(ArrayList(reminderDTOList))
        } catch (ex: Exception) {
            return Result.Error(ex.message)
        }

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            if (shouldReturnError) {
                return Result.Error(("Exception retrieving reminders.Unable to retrieve reminders"))
            } else {
                val reminder = reminderDTOList.find { it.id == id }
                if (reminder == null) {
                    return Result.Error(("No Reminder Found with id $id"))
                } else {
                    return Result.Success(reminder)
                }
            }
        } catch (ex: Exception) {
            return Result.Error(ex.message)
        }
    }

    override suspend fun deleteAllReminders() {
        reminderDTOList.clear()
    }


}