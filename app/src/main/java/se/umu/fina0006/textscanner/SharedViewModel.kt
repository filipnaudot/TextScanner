package se.umu.fina0006.textscanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var lastEmittedValue: String? = null
    val scanStorage = MutableLiveData<String>()
}