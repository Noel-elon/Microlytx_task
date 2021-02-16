package com.example.microlytxtask

import android.telephony.PhoneStateListener
import android.telephony.SignalStrength




internal class MyPhoneStateListener : PhoneStateListener() {

    var mSignalStrength = 0

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        super.onSignalStrengthsChanged(signalStrength)
        mSignalStrength = signalStrength.gsmSignalStrength
        mSignalStrength = 2 * mSignalStrength - 113 // -> dBm
    }
}