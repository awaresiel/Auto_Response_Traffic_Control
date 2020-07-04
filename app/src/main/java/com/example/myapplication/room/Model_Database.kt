package com.example.myapplication.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.helpers.Constants.Companion.DAO_DATABASE_NAME
import com.example.myapplication.helpers.Constants.Companion.DAO_TABLE_NAME

@Database(entities = arrayOf(Model_TrafficSettings::class),version = 1,exportSchema = false)
abstract class Model_Database : RoomDatabase() {
         abstract fun traffiSettingsDAO():DAO

    companion object{
        private const val TAG = "Model_Database"

        val callback: RoomDatabase.Callback = object : RoomDatabase.Callback(){
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "onOpen: startObserving DATABASE OPENED")
            }
        }

        val migration = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                //nothing to migrate yet
            }
        }

        @Volatile
        private var INSTANCE:Model_Database?=null

        fun getDatabaseInstance(context:Context):Model_Database{
            val tempInstance = INSTANCE
            if (tempInstance!=null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,Model_Database::class.java, DAO_DATABASE_NAME)
                    .addMigrations(migration)
                    .addCallback(callback)
                    .build()

                INSTANCE=instance
                return instance
            }
        }
    }


}
