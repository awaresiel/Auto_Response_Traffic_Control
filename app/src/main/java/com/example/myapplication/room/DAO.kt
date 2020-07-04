package com.example.myapplication.room

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


@Dao()
 interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTrafficSettingsToDatabase(data:Model_TrafficSettings) : Completable

    @Query("SELECT * FROM user_settings" )
    fun loadAllSettings() : Flowable<List<Model_TrafficSettings>>

    @Query("SELECT * FROM user_settings WHERE operatorNumber = :number")
   fun getTrafficSettingsFromDatabaseByNumber(number:String): Single<Model_TrafficSettings>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTrafficSettingsToDatabase(data:Model_TrafficSettings) : Completable
    @Delete
    fun deleteTrafficSettingsToDatabase(data:Model_TrafficSettings) : Completable

}