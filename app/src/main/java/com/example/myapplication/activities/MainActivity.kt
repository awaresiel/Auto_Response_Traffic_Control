package com.example.myapplication.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.viewmodels.MainActivity_ViewModel
import com.example.myapplication.activities.viewmodels.recyclerviews.IRecycler_Main_Activity
import com.example.myapplication.activities.viewmodels.recyclerviews.Recycler_Main_Activity
import com.example.myapplication.helpers.Constants
import com.example.myapplication.helpers.Constants.Companion.ACTION_START_SERVICE
import com.example.myapplication.room.Model_TrafficSettings
import com.example.myapplication.service.ITrafficMonitoringService
import com.example.myapplication.service.TrafficMonitoringService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), IRecycler_Main_Activity {

    private val TAG = "MainActivity"
    private val SEND_SMS_PERMISSION_REQUEST_CODE =1
    private var PERMISSION_GRANTED_FLAG = false

     var vm :MainActivity_ViewModel? = null
    lateinit var trafficDisplay : TextView
    lateinit var recyclerView:RecyclerView
    lateinit var recyclerViewAdapter:Recycler_Main_Activity
    lateinit var service: ITrafficMonitoringService
    var bound:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
            startActivity(intent)

        }

        findViews()
        askForSendSMSPermission()


        initRecyclerView()


    }

    private fun startSettingsActivity(number : String) {
        if (PERMISSION_GRANTED_FLAG) {
          var intent = Intent(this, SettingsActivity::class.java)
          if (!number.isNullOrBlank() && !number.isNullOrEmpty()) {
              intent.putExtra(Constants.SETTINGS_ACTIVITY_USER_ID_KEY, number)
          }
          startActivity(intent)
        }else{
            askForSendSMSPermission()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                startSettingsActivity("")
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun startService(){
        val startIntent = Intent(this, TrafficMonitoringService::class.java)
        startIntent.action = ACTION_START_SERVICE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        }else{
            startService(startIntent)
        }
        bindService(startIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            bound =false
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d(TAG, "onServiceConnected: ")
            var binder  = p1 as TrafficMonitoringService.MyBinder
            service = binder.getBinder()

            initViewModel(service)

            bound =true
        }
    }


    fun initViewModel(service:ITrafficMonitoringService){
         vm  = ViewModelProvider(this@MainActivity,ViewModelProvider.AndroidViewModelFactory(application)).get(
                MainActivity_ViewModel::class.java)
        vm?.setCallback(service)

        vm?.getTrafficData()?.observe(
            this@MainActivity,
            Observer<String> { data -> trafficDisplay.text = data })

        vm?.initializeUserSettings()?.observe(this, Observer<List<Model_TrafficSettings>> {
            if (it != null && it.isNotEmpty()) {
                for (value in it){
                    Log.d(TAG, "startObserving: it = " +value.operatorNumber)
                }
                recyclerViewAdapter.updatList(it)
            }
        })
    }


    fun initRecyclerView(){
        var layout = LinearLayoutManager(this)
        recyclerView.layoutManager = layout
        recyclerViewAdapter = Recycler_Main_Activity(ArrayList(),this)
        recyclerView.adapter = recyclerViewAdapter

    }

    fun askForSendSMSPermission() {
        Log.d(TAG, "askForSendSMSPermission: ")
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                PERMISSION_GRANTED_FLAG=true
                if (!bound){
                    startService()
                }

            }

            else -> {
                requestPermissions( arrayOf(Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS),
                    SEND_SMS_PERMISSION_REQUEST_CODE)

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            SEND_SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED  && grantResults[2] == PackageManager.PERMISSION_GRANTED ){
                    startService()
                    PERMISSION_GRANTED_FLAG = true
                } else {
                    shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSettingsClick(number: String) {
        startSettingsActivity(number)
    }

    override fun onDelete(data: Model_TrafficSettings) {

       AlertDialog.Builder(this)
        .setTitle("Do you want delete this item?")
        .setPositiveButton("Delete Settings", { dialog, id->  vm?.deleteUser(data) })
        .setNegativeButton("Cancel" , { d, id-> d.dismiss()})
        .create().show()
       }

    private fun stopService(){
        if (bound) {
            unbindService(connection)

            bound = false
        }
    }


    override fun onPause() {

        stopService()
        super.onPause()
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    fun findViews(){
        trafficDisplay = findViewById(R.id.textView_TrafficDisplay)
        recyclerView = findViewById(R.id.recyclerView_MainActivity)
    }


}