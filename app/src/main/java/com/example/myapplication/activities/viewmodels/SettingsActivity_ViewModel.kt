package com.example.myapplication.activities.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.room.Model_TrafficSettings
import java.math.BigDecimal


class SettingsActivity_ViewModel(application: Application) : BaseViewModel(application) {
    private val TAG = "SettingsActivity_ViewMo"

    private var userSettings: Model_TrafficSettings? = null
    private lateinit var userDataDisplay: MutableLiveData<Model_TrafficSettings>

    override fun initializeUserSettings(): LiveData<Model_TrafficSettings>? {
        userDataDisplay = MutableLiveData()

        if (userSettings != null) {
            userDataDisplay.postValue(userSettings)
        }
        Log.d(TAG, "initializeUserSettings:   userDataDisplay= " + userDataDisplay)
        return userDataDisplay

    }

    override fun getUserSettings(number: String) {
        Log.d(TAG, "getUserSettings: usersettings operatornumber = = " + number)
        addDisposables(
            repository.getTrafficSettingsFromDatabaseByNumber(number).subscribe(
                {
                    userSettings = it
                    userDataDisplay.postValue(userSettings)
                },
                {
                    Toast.makeText(getApplication(), "Cant reach your settings ", Toast.LENGTH_LONG)
                        .show()
                    Log.d(TAG, "getUserSettings: operatornumber it.cause error-> " + it.cause)
                    Log.d(TAG, "getUserSettings: operatornumber it.message error-> " + it.message)
                }
            )
        )
    }

    override fun saveUserSettings(
        operatorNumber: String,
        hours: Int,
        minutes: Int,
        traffic: Long,
        message: String,
        expectedMessage: String,
        size: String,
        replayFlag:Boolean
    ) {

        var settings = Model_TrafficSettings()

        settings.operatorNumber = operatorNumber
        settings.hours = hours
        settings.minutes = minutes
        settings.trafficLimit = traffic
        settings.message = message
        settings.expectedMessage = expectedMessage
        settings.size=size
        settings.MonitorAndReplay = replayFlag

        if (userSettings != null) {
            settings.id = userSettings!!.id
        }

        addDisposables(
            repository.saveTrafficSettings(settings).subscribe(
                {
                    Log.d(TAG, "saveUserSettings: limit = " + traffic)
                    Log.d(TAG, "saveUserSettings: success ")
                    Toast.makeText(getApplication(), "Settings saved!", Toast.LENGTH_LONG).show()
                },
                {
                    Log.d(TAG, "saveUserSettings: error -> " + it.cause)
                    Toast.makeText(getApplication(), "Settings cant be saved!", Toast.LENGTH_LONG)
                        .show()
                }
            )
        )
    }


}