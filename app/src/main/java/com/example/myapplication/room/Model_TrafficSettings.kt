package com.example.myapplication.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.helpers.Constants.Companion.DAO_TABLE_NAME

@Entity(tableName = DAO_TABLE_NAME)
 class Model_TrafficSettings {

    @PrimaryKey(autoGenerate = true)
    var id: Int=0
        get() = field
        set(value) {
            field = value
        }

    @ColumnInfo(name = "MonitorAndReplay")
    var MonitorAndReplay:Boolean =false
        get() = field
        set(value){
            field =value
        }

    @ColumnInfo(name = "size")
    var size:String="0"
    get() = field
    set(value){
      field =value
    }

    @ColumnInfo(name = "traffic_limit")
     var trafficLimit : Long =0
         get() = field
         set(value) {
             field = value
         }

    @ColumnInfo(name = "message")
    var message : String =""
        get() = field
        set(value) {
            field = value
        }

     @ColumnInfo(name = "hours")
     var hours : Int = 0
         get() = field
         set(value) {
             field = value
         }

     @ColumnInfo(name = "minutes")
     var minutes: Int= 0
         get() = field
         set(value) {
             field = value
         }

     @ColumnInfo(name = "operatorNumber")
     var operatorNumber:String = "0"
         get() = field
         set(value) {
             field = value
         }


 }