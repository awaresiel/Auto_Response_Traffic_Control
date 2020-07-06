package com.example.myapplication.service

interface IMessageListener {

    fun onMessageArrived(message:MessageModel)
}