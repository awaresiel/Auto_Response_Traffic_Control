package com.example.myapplication.activities.viewmodels.recyclerviews

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.room.Model_TrafficSettings


class Recycler_Main_Activity(var userSettingsList: List<Model_TrafficSettings> , val callback: IRecycler_Main_Activity)
    : RecyclerView.Adapter<Recycler_Main_Activity.Adapter>() {

    private  val TAG = "Recycler_Main_Activity"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter {
        val inflater = LayoutInflater.from(parent.context)
        return Adapter(inflater.inflate(R.layout.content_main,parent,false))
    }

    override fun getItemCount(): Int {
        return userSettingsList.size
    }
    fun updatList(list: List<Model_TrafficSettings>){
        userSettingsList = list
        notifyDataSetChanged()
        Log.d(TAG, "updatList: update list")
    }

    override fun onBindViewHolder(holder: Adapter, position: Int) {
        if (userSettingsList.isNotEmpty()){
             holder.bind(userSettingsList[position])
        }
    }

    inner class Adapter(itemView: View) : RecyclerView.ViewHolder(itemView){
        private  val TAG = "Recycler_Main_Activity"
        var limit:TextView
        var time:TextView
        var operatorNumber:TextView
        var message:TextView

        init {
            limit = itemView.findViewById(R.id.textView_DisplayTrafficLimit)
            time = itemView.findViewById(R.id.textView_DisplayTime)
            operatorNumber = itemView.findViewById(R.id.textView_TextReplayNumber)
            message = itemView.findViewById(R.id.textView_message)

            var layout: LinearLayout = itemView.findViewById(R.id.content_main_Linear_Layout)
            layout.setOnClickListener { callback.onSettingsClick(userSettingsList.get(adapterPosition).operatorNumber) }
            layout.setOnLongClickListener {
                callback.onDelete( userSettingsList.get(adapterPosition) )
                     true
            }
        }

        fun bind(data : Model_TrafficSettings){
            limit.text = "Limit : " + data.trafficLimit.toString() + " " + data.size
            time.text = "Time of Reset: "+data.hours.toString() + " hours and " + data.minutes + " minutes"

           var text= if(data.MonitorAndReplay) "Monitor And Replay" else "Reply When Limit exceeded"

            operatorNumber.text = "Operator number: " + data.operatorNumber + " : " + text
            message.text = "message: " + data.message
        }

    }
}