package com.example.myapplication.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.myapplication.helpers.Constants.Companion.TIME_PICKER_BUNDLE_KEY
import java.util.*

class TimePickerDisplay : DialogFragment() ,
    TimePickerDialog.OnTimeSetListener {

    private  val TAG = "TimePickerDisplay"
    private lateinit var callback: ITimePickerDisplay

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(
            activity,
            this,
            hour,
            minute,
            DateFormat.is24HourFormat(activity)
        )
    }

    fun setListener(l: ITimePickerDisplay){
        callback =l
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val bundle:Bundle = Bundle()
       val array = IntArray(3)
        array[0]=p1
        array[1]=p2
        bundle.putIntArray(TIME_PICKER_BUNDLE_KEY,array)
        callback.setTime(bundle)

    }



}