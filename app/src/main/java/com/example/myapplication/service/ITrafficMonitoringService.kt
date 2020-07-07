package com.example.myapplication.service

import android.app.usage.UsageStatsManager
import com.example.myapplication.room.Model_TrafficSettings

interface ITrafficMonitoringService {

     fun getTrafficData():String
     fun setListOfSettingsForMonitoring(list:List<Model_TrafficSettings>)
}