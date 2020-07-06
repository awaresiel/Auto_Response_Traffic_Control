package com.example.myapplication.service

data class MessageModel(private var sender:String,
                        private var messageBody:String,
                        private var displayMessageBody:String,
                        private var message:String){

    val getSender :String get() = sender
    val getMessageBody :String get() = messageBody
    val getDisplayMessageBody :String get() = displayMessageBody
    val getMessage :String get() = message

    override fun equals(other: Any?): Boolean {
        return this.message == other as String
    }
}