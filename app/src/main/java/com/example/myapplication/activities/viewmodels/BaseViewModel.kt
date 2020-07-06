package com.example.myapplication.activities.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.room.IROOM_Repository
import com.example.myapplication.room.Model_Database
import com.example.myapplication.room.ROOM_Repository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel(application: Application) : AndroidViewModel(application),
    IViewModel {
    private  val TAG = "BaseViewModel"

    var compositeDisposable: CompositeDisposable?
    var repository: IROOM_Repository

    init {
        compositeDisposable = CompositeDisposable()
        val database = Model_Database.getDatabaseInstance(application)
        val dao = database.traffiSettingsDAO()
        repository = ROOM_Repository(dao)
    }


   override fun addDisposables(disposable: Disposable){
        compositeDisposable?.add(disposable)
    }

    fun dispose() {
        compositeDisposable?.dispose()
        compositeDisposable = null
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: clearing")
        dispose()
        super.onCleared()
    }
}