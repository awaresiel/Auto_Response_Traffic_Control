package com.example.myapplication.activities.viewmodels.recyclerviews

import com.example.myapplication.room.Model_TrafficSettings
import java.math.BigInteger

interface IRecycler_Main_Activity {
    fun onSettingsClick(number:String)
    fun onDelete(data:Model_TrafficSettings)
}