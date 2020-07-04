package com.example.myapplication.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.helpers.Constants.Companion.SETTINGS_ACTIVITY_USER_ID_KEY
import com.example.myapplication.helpers.Constants.Companion.SET_MESSAGE_FOR_SENDING_KEY
import com.example.myapplication.helpers.Constants.Companion.SET_NUMBER_AND_LIMIT_KEY
import com.example.myapplication.helpers.Constants.Companion.SET_TIME_KEY
import com.example.myapplication.helpers.Constants.Companion.TIME_PICKER_BUNDLE_KEY
import com.example.myapplication.dialogs.ITimePickerDisplay
import com.example.myapplication.R
import com.example.myapplication.activities.viewmodels.SettingsActivity_ViewModel
import com.example.myapplication.dialogs.TimePickerDisplay
import com.example.myapplication.room.Model_TrafficSettings
import java.math.BigInteger


class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    ITimePickerDisplay {

    val TAG: String = "SettingsActivity.class"


    var MonitorAndReplay = true
    lateinit var messageText: EditText
    lateinit var resetTime: TextView
    lateinit var mobileNumber: EditText
    lateinit var trafficLimit: EditText
    lateinit var spinnerTrafficLimit: Spinner
    lateinit var spinnerMobileNumberOptions: Spinner
    lateinit var saveButton: Button
    lateinit var timeDataArray: IntArray
    lateinit var viewModel: SettingsActivity_ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        timeDataArray = IntArray(3)
        InitializeViews()
        var operatorNumberID = intent.getStringExtra(SETTINGS_ACTIVITY_USER_ID_KEY)
        Log.d(TAG, "onCreate: operatornumber = " + operatorNumberID)
        if (operatorNumberID != null) {
            initViewModel(operatorNumberID)
        } else {
            initViewModel("")
        }


    }

    private fun initViewModel(operatorNumberID: String) {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
                .get(SettingsActivity_ViewModel::class.java)

        if (!operatorNumberID.isNullOrEmpty() && !operatorNumberID.isNullOrBlank()) {
            Log.d(TAG, "onCreate: operatornumber 2 = " + operatorNumberID)
            viewModel.getUserSettings(operatorNumberID)
        }

        viewModel.initializeUserSettings()?.observe(this, Observer<Model_TrafficSettings> {
            if (it != null) {
                trafficLimit.setText(it.trafficLimit.toString())
                mobileNumber.setText(it.operatorNumber)
                messageText.setText(it.message)
                resetTime.setText(it.hours.toString() + " : " + it.minutes.toString())
                if (it.size == "GB")  spinnerTrafficLimit.setSelection(0) else  spinnerTrafficLimit.setSelection(1)
                if (it.MonitorAndReplay) spinnerMobileNumberOptions.setSelection(0) else  spinnerMobileNumberOptions.setSelection(1)

            }
        }
        )
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        p0?.setSelection(0)
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        var str = p0?.selectedItem.toString()
        if (str == "Monitor And Replay") MonitorAndReplay = true
        if (str == "Reply When Limit exceeded") MonitorAndReplay = false

    }

    fun saveUsersSettings() {
        var trafficLimitNumber = 0L
        var mobileNumberForReplays: String = "0"
        var message = "Message Not Set"
        if (trafficLimit.text.length <= 0 || mobileNumber.text.length <= 1 || messageText.text.length > 1000) {
            Toast.makeText(this, "Fields cant be empty ", Toast.LENGTH_LONG).show()
            return
        } else {
            Log.d(TAG, "InitializeViews: else part")

            trafficLimitNumber = trafficLimit.text.toString().toLong()
            var size = spinnerTrafficLimit.selectedItem.toString()
            mobileNumberForReplays = mobileNumber.text.toString()
            message = messageText.text.toString()

            viewModel.saveUserSettings(
                mobileNumberForReplays.toString(),
                timeDataArray[0],
                timeDataArray[1],
                trafficLimitNumber,
                message,
                 size,
                MonitorAndReplay
            )

            finish()

        }
    }


    private fun InitializeViews() {
        messageText = findViewById(R.id.EditText_Message)
        resetTime = findViewById(R.id.textView_timeReset)

        resetTime.setOnClickListener { launchTimePicker() }
        mobileNumber = findViewById(R.id.EditText_MobileNo)
        trafficLimit = findViewById(R.id.EditText_TrafficLimit)
        spinnerTrafficLimit = findViewById(R.id.spinner_TrafficLimit)
        spinnerTrafficLimit.onItemSelectedListener = this
        spinnerMobileNumberOptions = findViewById(R.id.spinner2_MobileNumberOptions)
        spinnerMobileNumberOptions.onItemSelectedListener = this
        saveButton = findViewById(R.id.button_save)

        saveButton.setOnClickListener {
            saveUsersSettings()
        }


    }


    private fun launchTimePicker() {

        var display = TimePickerDisplay()
        display.show(supportFragmentManager, "TimePickerDisplay")
        display.setListener(this)

    }


    override fun setTime(bundle: Bundle) {
        if (bundle.containsKey(TIME_PICKER_BUNDLE_KEY) && bundle.getIntArray(TIME_PICKER_BUNDLE_KEY) != null) {
            timeDataArray = bundle.getIntArray(TIME_PICKER_BUNDLE_KEY)!!

            val timeSet =
                "Traffic will reset at " + timeDataArray[0].toString() + " hours " + timeDataArray[1].toString() + " minutes "
            Log.d(TAG, "setTime: = " + timeSet)
            resetTime.text = timeSet
        }
    }
}

