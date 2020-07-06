package com.example.myapplication.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log

class MessagesMonitor() : BroadcastReceiver() {
    private val TAG = "MessagesMonitor"

    private val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
   private var listener: IMessageListener? =null

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAG, "onReceive: receiving")
        if (p1?.action.equals(SMS_RECEIVED)) {

            val data = p1?.extras

            if (data != null) {
                val pdus = data.get("pdus") as Array<*>
                val format = data.getString("format")

                for (index in pdus.indices) {
                    val smsMessage =
                        SmsMessage.createFromPdu(pdus[index] as ByteArray?, format)


                    if (smsMessage != null) {
                        var sender = smsMessage.originatingAddress
                        var messageBody = smsMessage.messageBody
                        var displayMessageBody = smsMessage.displayMessageBody
                        var message = smsMessage.messageBody

                        val messageReceived =
                            MessageModel(sender!!, messageBody, displayMessageBody, message)

                        listener?.onMessageArrived(messageReceived)
                    }
                }
            }
        }
    }

    //message|replayOnTextArrived|onMessageArrived
    fun setIMessageListener(l: IMessageListener) {

        listener = l
    }
}

