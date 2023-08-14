package se.umu.fina0006.textscanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedScanResultViewModel : ViewModel() {
    val scanStorage = MutableLiveData<String>()
}