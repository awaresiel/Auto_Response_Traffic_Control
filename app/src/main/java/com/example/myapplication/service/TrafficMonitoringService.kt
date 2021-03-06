package com.example.myapplication.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.icu.text.DateFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.SmsManager
import android.text.style.TtsSpan
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.helpers.Constants
import com.example.myapplication.helpers.Constants.Companion.CHANNEL_ID
import com.example.myapplication.helpers.Constants.Companion.NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.myapplication.helpers.Constants.Companion.NOTIFICATION_CHANNEL_NAME
import com.example.myapplication.room.Model_TrafficSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class TrafficMonitoringService : Service(),
    ITrafficMonitoringService, IMessageListener {
    private val TAG = "TrafficMonitoringServic"

    private lateinit var settingsList: List<Model_TrafficSettings>
    private lateinit var messagesReceivedList: MutableList<MessageModel>

    // this is list of what is spent (gottent with calculate() method)
    private lateinit var CURENT_DATA_USAGE: ArrayList<BigInteger>

    // list of user defined limit + calculate() i.e defined limit + current spending
    private lateinit var DATA_LIMIT: ArrayList<BigInteger>
    private lateinit var receiver: BroadcastReceiver
    private val NOTIFICATION_ID = 101
    private var serviceRunning: Boolean = false;
    private var initializeOnStartup: Boolean = false
    private var LOADING_DATA: Boolean = false
    private lateinit var intervalScheduler: Disposable

    private var currentMessage = ""
    private var previousMessage = ""

    private var binder: MyBinder = MyBinder()

    inner class MyBinder() : Binder() {
        fun getBinder(): TrafficMonitoringService {
            return this@TrafficMonitoringService
        }
    }

    override fun getTrafficData(): String {
        return checkDataUsageSinceBoot()
    }

    override fun setListOfSettingsForMonitoring(list: List<Model_TrafficSettings>) {
        LOADING_DATA = true
        settingsList = list
        setupCurrentDataAndPreviousData()

        if (!initializeOnStartup) {
            startMonitoringDataUsage()
            initializeOnStartup = true
        }
        LOADING_DATA = false
    }

    override fun checkAllStates() {
        sendTextsOnStartup();
    }

    override fun onCreate() {
        super.onCreate()

        settingsList = ArrayList()
        messagesReceivedList = ArrayList()


        startNotificationService()

        receiver = MessagesMonitor()
        (receiver as MessagesMonitor).setIMessageListener(this)
        val filter = IntentFilter()
        filter.addAction("android.provider.Telephony.SMS_RECEIVED")

        registerReceiver(receiver, filter)


    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.action == Constants.ACTION_START_SERVICE) {

        } else {
            stopService()
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }


    private fun startNotificationService() {
        if (serviceRunning) {
            return
        } else {
            createNotificationChannel()
            var notificationIntent = Intent(
                applicationContext,
                MainActivity::class.java
            )
            notificationIntent.setAction(Constants.ACTION_START_SERVICE)
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

            val icon = BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_launcher_foreground
            )

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitoring Traffic...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(icon)
                .setOngoing(false)
                .setAutoCancel(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)

        }
    }


    private fun startMonitoringDataUsage() {

        intervalScheduler = Observable.interval(0, 2, TimeUnit.SECONDS, Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                ReplyWhenLimitExceded()
                checkTime()
            }
    }

    private fun checkTime() {
        for (index in 0 until settingsList.size) {
            var currentTime = Calendar.getInstance().time
            var resetTime = Calendar.getInstance()
            resetTime.set(Calendar.HOUR_OF_DAY, settingsList[index].hours)
            resetTime.set(Calendar.MINUTE, settingsList[index].minutes)
            resetTime.set(Calendar.SECOND, 0)
            Log.d(TAG, "startMonitoringDataUsage: current time = " + currentTime.time)
            Log.d(
                TAG,
                "startMonitoringDataUsage: time on position settingsList[0] = " + resetTime.time.time
            )
            if (resetTime.time.time == currentTime.time || currentTime.time.minus(resetTime.time.time)==1L )
            {
                sendTextsOnStartup()
            }
        }
    }

    /*
     *  constantly checking and adding current data traffic ussage to arraylist CURENT_DATA_USAGE[index] = calculate()
     *  if CURENT_DATA_USAGE[index] becomes bigger or equal to data limit (data limit is set in beggining like current traffic spenditure * limit
     *  that was set by user. Then and only then send a message and on that same index add one more time that same amount that was set by user->
     *   DATA_LIMIT[index] =
                        calculate() + (settingsList[index].trafficLimit * 1024).toBigInteger()
     */

    private fun ReplyWhenLimitExceded() {
        Log.d(
            TAG,
            "ReplyWhenLimitExceded:settingsList " + settingsList.size + " CURENT_DATA_USAGE= " + CURENT_DATA_USAGE.size + " DATA_LIMIT " + DATA_LIMIT.size
        )
        for (index in 0 until settingsList.size) {

            CURENT_DATA_USAGE[index] = calculate()

            if (CURENT_DATA_USAGE[index] >= DATA_LIMIT[index]) {
                Log.d(
                    TAG,
                    "ReplyWhenLimitExceded: condition =  DATA_LIMIT[index] >= CURENT_DATA_USAGE[index])  replying " + DATA_LIMIT[index]
                )

                if (settingsList[index].size == "GB") {
                    DATA_LIMIT[index] =
                        calculate() + (settingsList[index].trafficLimit * 1024).toBigInteger()

                    Log.d(
                        TAG,
                        "ReplyWhenLimitExceded: if DATA_LIMIT[index] = (settingsList[index].trafficLimit * 1024).toBigInteger() " + DATA_LIMIT[index]
                    )
                } else {
                    DATA_LIMIT[index] =
                        calculate() + settingsList[index].trafficLimit.toBigInteger()

                    Log.d(
                        TAG,
                        "ReplyWhenLimitExceded: else  DATA_LIMIT[index] = settingsList[index].trafficLimit.toBigInteger() " + DATA_LIMIT[index]
                    )
                }
                // if  DATA_LIMIT[index] is set to 0 then loop will continue forever hence it has to be set to bigger than 1
                // just replay if Monitor data and Replay is set to false, hence replay when limit exceeded was selected

                if (!settingsList[index].MonitorAndReplay && DATA_LIMIT[index] > 1.toBigInteger()) {
                    sendMessage(settingsList[index].message, settingsList[index].operatorNumber)
                    Log.d(TAG, "ReplyWhenLimitExceded: megabite size = " + DATA_LIMIT[index])

                }
            }
        }
    }

    fun sendMessage(message: String, number: String) {
        Log.d(TAG, "sendMessage: message "+ message)
        val smsManager = SmsManager.getDefault() as SmsManager
        smsManager.sendTextMessage(number, null, message, null, null)

    }

    private fun sendTextsOnStartup() {
        if(settingsList.isNullOrEmpty()){
            Toast.makeText(this,"There are no numbers in the list, cant send text",Toast.LENGTH_SHORT).show();
            return
        }
        for (item in settingsList) {
            if (item.MonitorAndReplay) {
                sendMessage(item.message, item.operatorNumber)
            }
        }
    }


    private fun replayOnTextArrived(message: MessageModel) {

        currentMessage = message.getMessage

        if (currentMessage == previousMessage) {
            Log.d(TAG, "replayOnTextArrived: same messages, returning " + previousMessage)
            return
        }

        for (index in 0 until settingsList.size) {
            if (settingsList[index].expectedMessage.contains(message.getMessage) && settingsList[index].MonitorAndReplay) {

                sendMessage(settingsList[index].message, settingsList[index].operatorNumber)
                messagesReceivedList.removeAt(index)
                Log.d(TAG, "replayOnTextArrived: replying")
                previousMessage = message.getMessage
            }
        }
    }


    override fun onMessageArrived(message: MessageModel) {
        Log.d(TAG, "onMessageArrived: " + message.getMessageBody)
        messagesReceivedList.add(message)

        replayOnTextArrived(message)

    }

    private fun setupCurrentDataAndPreviousData() {
        CURENT_DATA_USAGE = ArrayList()
        DATA_LIMIT = ArrayList()
        for (index in 0 until settingsList.size) {
            CURENT_DATA_USAGE.add(calculate())
            if (settingsList[index].size == "GB") {
                var result = calculate() + (settingsList[index].trafficLimit * 1024).toBigInteger()
                DATA_LIMIT.add(result)
            } else {
                var result = calculate() + settingsList[index].trafficLimit.toBigInteger()
                DATA_LIMIT.add(result)
            }
            Log.d(TAG, "setupCurrentDataAndPreviousData: DATA_LIMIT[index]= " + DATA_LIMIT.size)
        }

    }

    private fun checkDataUsageSinceBoot(): String {

        if (checkIfDataIsEnabled()) {
            var data = calculate()
            return "Data Usage since device boot: \n\n " + data + " mb"
        } else {
            return "Enable mobile data"
        }
    }

    private fun calculate(): BigInteger {

        var currentlySpent =
            (android.net.TrafficStats.getMobileRxBytes() + android.net.TrafficStats.getMobileTxBytes()) / 1048576
        return currentlySpent.toBigInteger()
    }

    private fun checkIfDataIsEnabled(): Boolean {

        var connection: Boolean = Settings.Global.getInt(contentResolver, "mobile_data", 0) == 1;
        return connection
    }

    private fun stopService() {
        if (intervalScheduler != null && !intervalScheduler.isDisposed) intervalScheduler.dispose()
        Log.d(TAG, "stopService: ")
        stopForeground(true)
        stopSelf()
        serviceRunning = false

    }


    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText = NOTIFICATION_CHANNEL_DESCRIPTION
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