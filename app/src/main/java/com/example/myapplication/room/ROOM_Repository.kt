package com.example.myapplication.room

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import java.util.concurrent.Flow
import kotlin.math.log

class ROOM_Repository(private val dao:DAO) : IROOM_Repository {
    private  val TAG = "ROOM_Repository"

    override fun getAllUsersSettings() : Flowable<List<Model_TrafficSettings>> {
        val usersSettings = dao.loadAllSettings()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        return usersSettings
    }


    override fun saveTrafficSettings(data:Model_TrafficSettings):Completable{
        Log.d("TAG", "addModelDatabase: ")
       var completable= dao.addTrafficSettingsToDatabase(data)
           .subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread())

        return completable
    }

    override fun getTrafficSettingsFromDatabaseByNumber (operatorNum : String):Single<Model_TrafficSettings>{
        Log.d(TAG, "getTrafficSettingsFromDatabaseByNumber: operatornumber = " + operatorNum)
        val single = dao.getTrafficSettingsFromDatabaseByNumber(operatorNum)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        return single
    }

    override fun updateTrafficSettingsToDatabase(data : Model_TrafficSettings) :Completable{
        val completable = dao.updateTrafficSettingsToDatabase(data)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        return completable
    }

    override fun deleteTrafficSettingsToDatabase(data:Model_TrafficSettings):Completable{
        val completable= dao.deleteTrafficSettingsToDatabase(data)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        return completable
    }

}