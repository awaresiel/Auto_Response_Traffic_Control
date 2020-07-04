package com.example.myapplication.helpers

 class Constants {
    companion object {

        const val DAO_TABLE_NAME = "user_settings"
        const val DAO_DATABASE_NAME = "user_settings_database"

         const val ACTION_START_SERVICE = "com.example.myapplication"
         const val NOTIFICATION_CHANNEL_NAME = "NOTIFICATION"
         const val NOTIFICATION_CHANNEL_DESCRIPTION = "TRAFFIC LIMIT REACHED, SENDING TEXT.."
         const val CHANNEL_ID = "100"

         const val TIME_PICKER_BUNDLE_KEY = "TIME_PICKER_BUNDLE_KEY"
         const val SETTINGS_ACTIVITY_USER_ID_KEY = "SETTINGS_ACTIVITY_RESULT_BUNDLE_KEY"

         const  val SET_TIME_KEY = "SET_TIME_KEY"
         const  val SET_NUMBER_AND_LIMIT_KEY = "SET_NUMBER_AND_LIMIT_KEY"
         const  val SET_MESSAGE_FOR_SENDING_KEY = "SET_MESSAGE_FOR_SENDING_KEY"
    }
}