package com.example.myapplication.service

import android.app.*
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.helpers.Constants
import com.example.myapplication.helpers.Constants.Companion.CHANNEL_ID
import com.example.myapplication.helpers.Constants.Companion.NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.myapplication.helpers.Constants.Companion.NOTIFICATION_CHANNEL_NAME
import com.example.myapplication.room.Model_TrafficSettings
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import java.util.concurrent.TimeUnit


class TrafficMonitoringService : Service(),
    ITrafficMonitoringService {
    private  val TAG = "TrafficMonitoringServic"

    private var trafficlimit = 0L
        get() = field
        set(value) {
            field = value
        }

    private lateinit var settingsList: List<Model_TrafficSettings>

    // this is list of what is spent (gottent with calculate() method)
    private lateinit var CURENT_DATA_USAGE : ArrayList<BigInteger>
    // list of user defined limit + calculate() i.e defined limit + current spending
    private lateinit var DATA_LIMIT : ArrayList<BigInteger>
    private val NOTIFICATION_ID = 101
    private var serviceRunning:Boolean = false;
    private lateinit var intervalScheduler: Disposable
    private var binder: MyBinder = MyBinder()

      inner class MyBinder():Binder(){
        fun getBinder(): TrafficMonitoringService {
            return this@TrafficMonitoringService
        }
    }

    override fun getTrafficData(): String {
        return checkDataUsageSinceBoot()
    }

    override fun setListOfSettingsForMonitoring(list: List<Model_TrafficSettings>) {
        settingsList =list
        Log.d(TAG, "setListOfSettingsForMonitoring: = " + list)
        setupCurrentDataAndPreviousData()
        startMonitoringDataUsage()
    }



    override fun onCreate() {
        super.onCreate()
        settingsList = ArrayList()
        CURENT_DATA_USAGE = ArrayList()
        DATA_LIMIT = ArrayList()

        startNotificationService()
        Log.d(TAG, "onCreate: service started")

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: before if statement")
        if (intent !=null && intent.action == Constants.ACTION_START_SERVICE){
            Log.d(TAG, "onStartCommand: ")

        } else{
           stopService()
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
  }


    private fun startNotificationService(){
        if (serviceRunning){
            return
        }else{
            createNotificationChannel()
            var notificationIntent = Intent(applicationContext,
                MainActivity::class.java)
            notificationIntent.setAction(Constants.ACTION_START_SERVICE)
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK)

            val pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0)

            val icon = BitmapFactory.decodeResource(resources,
                R.drawable.ic_launcher_foreground
            )

            val notification:Notification = NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Monitoring Traffic...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(icon)
                .setOngoing(false)
                .setAutoCancel(true)
                .build()

            startForeground(NOTIFICATION_ID,notification)

        }
    }


    private fun startMonitoringDataUsage(){

        intervalScheduler = Observable.interval(0,2, TimeUnit.SECONDS,Schedulers.io())
      .subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread())
          .subscribe { data ->
              ReplyWhenLimitExceded()
          }
  }

    private fun ReplyWhenLimitExceded(){
        for (index in 0 until settingsList.size){
             CURENT_DATA_USAGE[index] = calculate()
            if ( CURENT_DATA_USAGE[index] >= DATA_LIMIT[index] ){
                Log.d(TAG, "ReplyWhenLimitExceded: condition =  DATA_LIMIT[index] >= CURENT_DATA_USAGE[index])  replying ")
             //   val smsManager = SmsManager.getDefault() as SmsManager
              //  smsManager.sendTextMessage(settingsList[index].operatorNumber, null, settingsList[index].message, null, null)
                if ( settingsList[index].size == "GB"){
                    DATA_LIMIT[index] = calculate()+ (settingsList[index].trafficLimit * 1024).toBigInteger()

                    Log.d(TAG, "ReplyWhenLimitExceded: if DATA_LIMIT[index] += (settingsList[index].trafficLimit * 1024).toBigInteger() " + DATA_LIMIT[index])
                }
                else {
                    DATA_LIMIT[index] = calculate() + settingsList[index].trafficLimit.toBigInteger()

                    Log.d(TAG, "ReplyWhenLimitExceded: else  DATA_LIMIT[index] += settingsList[index].trafficLimit.toBigInteger() " + DATA_LIMIT[index])
                }

            }
        }
    }

    private fun setupCurrentDataAndPreviousData(){
        for (index in 0 until settingsList.size){
            CURENT_DATA_USAGE.add(calculate())
            if ( settingsList[index].size == "GB"){
                var result = calculate() + (settingsList[index].trafficLimit * 1024).toBigInteger()
                  DATA_LIMIT.add(result)
            }
            else  {
                var result = calculate() + settingsList[index].trafficLimit.toBigInteger()
                DATA_LIMIT.add(result)
            }
        }

    }

    private  fun checkDataUsageSinceBoot():String {

        if (checkIfDataIsEnabled()) {
            var data =calculate()

          //  Log.d(TAG, "checkDataUsageSinceBoot: <===========================================================>")
           return "Data Usage since device boot: \n\n " + data + " mb"
        }else{
            return "Enable mobile data"
        }
    }

    private fun calculate():BigInteger{

         var currentlySpent= (android.net.TrafficStats.getMobileRxBytes() + android.net.TrafficStats.getMobileTxBytes()) / 1048576
        return currentlySpent.toBigInteger()
    }

    private fun checkIfDataIsEnabled():Boolean{

        var connection: Boolean = Settings.Global.getInt(contentResolver, "mobile_data", 0) == 1;
      //  Log.d(TAG, "checkIfDataIsEnabled: is mobile data enabled = " + connection)
        return connection
    }

    private fun stopService(){
       if (intervalScheduler !=null && !intervalScheduler.isDisposed) intervalScheduler.dispose()
        Log.d(TAG, "stopService: ")
        stopForeground(true)
        stopSelf()
        serviceRunning = false

    }



    override fun onDestroy() {
        stopService()
        Log.d(TAG, "onDestroy: stopping service ")
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText =NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
        }
    }

}