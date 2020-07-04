package com.example.myapplication.room

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface IROOM_Repository {

    fun getAllUsersSettings() : Flowable<List<Model_TrafficSettings>>
    fun saveTrafficSettings(data:Model_TrafficSettings): Completable
    fun getTrafficSettingsFromDatabaseByNumber (operatorNum : String):Single<Model_TrafficSettings>
    fun updateTrafficSettingsToDatabase(data : Model_TrafficSettings) :Completable
    fun deleteTrafficSettingsToDatabase(data:Model_TrafficSettings):Completable
}