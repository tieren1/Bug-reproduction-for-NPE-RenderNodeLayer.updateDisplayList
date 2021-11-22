package com.terence.composeTest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Terence Peh on 19/11/21.
 */

class ChangePinViewModel : ViewModel() {
    private val _checkPinStatus = MutableLiveData(IDLE_STATE)
    val checkPinStatus: LiveData<Int>
        get() = _checkPinStatus

    fun checkPin(pin: String?) {

        if (pin.isNullOrBlank()) {
            _checkPinStatus.value = PIN_ERROR
            return
        }

        _checkPinStatus.value = PIN_VERIFYING

        viewModelScope.launch(Dispatchers.IO) {
            delay(100)

            _checkPinStatus.postValue(PIN_MATCH)
        }
    }

    fun clearStatus() {
        _checkPinStatus.postValue(IDLE_STATE)
    }

    companion object {
        const val IDLE_STATE = -1
        const val PIN_VERIFYING = 0
        const val PIN_MATCH = 1
        const val PIN_NO_MATCH = 2
        const val PIN_ERROR = 3

        const val PIN_APP_LOCK = 4
        const val PIN_APP_LOCK_PERM = 5
        const val PIN_LAST_TRY = 6
    }
}
