package com.example.myapplication.activities.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.room.Model_Database
import com.example.myapplication.room.Model_TrafficSettings
import com.example.myapplication.room.ROOM_Repository
import com.example.myapplication.service.ITrafficMonitoringService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity_ViewModel(application : Application) : BaseViewModel(application) {
    private  val TAG = "MainActivity_ViewModel"

   private lateinit var service : ITrafficMonitoringService
    private var traffic: MutableLiveData<String>? = null
    private var usersConfigurations: MutableLiveData<List<Model_TrafficSettings>>? = null


    fun setCallback(s : ITrafficMonitoringService){
        Log.d(TAG, "setCallback: ")
        service =s
    }

    override fun initializeUserSettings(): LiveData<List<Model_TrafficSettings>>?{
        Log.d(TAG, "initializeUserSettings: initialization")
      if (usersConfigurations==null) {
            usersConfigurations = MutableLiveData()
      }
            addDisposables (
                repository.getAllUsersSettings()
                    .subscribe(
                        {

                            usersConfigurations?.postValue(it)
                            service.setListOfSettingsForMonitoring(it)

                        },
                        {
                            for (item in it.stackTrace){
                                Log.d(TAG, "initializeUserSettings:=========>  "+item)
                            }

                            Toast.makeText(getApplication(),"Please Configure Monitoring Settings",Toast.LENGTH_LONG).show()
                        }
                    )
            )

            return usersConfigurations

    }

    override fun deleteUser(data:Model_TrafficSettings) {
        addDisposables(
            repository.deleteTrafficSettingsToDatabase(data)
                .subscribe(
                    {
                        initializeUserSettings()
                        Log.d(TAG, "deleteUser: user deleted ")
                        Toast.makeText(getApplication(),"user deleted! ",Toast.LENGTH_LONG).show()
                    },
                    { Log.d(TAG, "deleteUser: error -> " + it )
                        Toast.makeText(getApplication(),"cant delete user",Toast.LENGTH_LONG).show()
                    }
                )
        )
    }

    fun getTrafficData(): LiveData<String>? {
        Log.d(TAG, "getData: ")
        if (traffic == null) {

            traffic = MutableLiveData()
            addDisposables (
                Observable.interval(0,2,TimeUnit.SECONDS,Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map { service.getTrafficData() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data ->  traffic?.postValue(data)  }
            )

            return traffic
        }else{
            return traffic
        }
    }

}

