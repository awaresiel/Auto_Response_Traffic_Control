package com.example.myapplication.activities.viewmodels.recyclerviews

import androidx.lifecycle.LiveData
import com.example.myapplication.room.Model_TrafficSettings
import io.reactivex.disposables.Disposable

interface IViewModel {

     fun initializeUserSettings(): LiveData<*>?

     fun saveUserSettings(operatorNumber:String,hours:Int,minutes:Int,traffic:Long,message:String,size: String, replayFlag:Boolean){}

     fun getUserSettings(number : String){}

     fun deleteUser(data: Model_TrafficSettings) { }

     fun addDisposables(disposable: Disposable)
}