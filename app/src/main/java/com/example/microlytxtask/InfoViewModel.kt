package com.example.microlytxtask

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class InfoViewModel(private val application: Application) : ViewModel() {

    private val telephonyManager: TelephonyManager
    private val connectivityManager: ConnectivityManager
    private val info = MutableLiveData<Info>()
    val _info: LiveData<Info>
        get() = info

    private val error = MutableLiveData<String>()
    val _error: LiveData<String>
        get() = error


    init {
        getInfo()
        telephonyManager =
            application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        connectivityManager =
            application.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    }


    fun getMobileCountryCode(): String {

        return telephonyManager.networkCountryIso
    }

    fun getMobileNetworkCode(): String {

        var mnc = ""
        val networkOperator = telephonyManager.networkOperator
        if (!TextUtils.isEmpty(networkOperator)) {
            val mcc = networkOperator.substring(0, 3).toInt()
            mnc = networkOperator.substring(3).toInt().toString()
        }
        return mnc
    }

    fun getCountryZipCode(): String {
        var countryID = ""
        var countryZipCode = ""

        countryID = telephonyManager.simCountryIso.toUpperCase(Locale.ROOT)
        val rl = application.resources.getStringArray(R.array.CountryCodes)
        for (i in rl.indices) {
            val g = rl[i].split(",").toTypedArray()
            if (g[1].trim { it <= ' ' } == countryID.trim { it <= ' ' }) {
                countryZipCode = g[0]
                break
            }
        }
        return countryZipCode
    }

    fun getCellID(): String {

        var location = GsmCellLocation()
        if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_GSM) {
            if (ActivityCompat.checkSelfPermission(
                    application,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                location = telephonyManager.cellLocation as GsmCellLocation

            }

        }
        return location.cid.toString()
    }


    fun getNetworkType(): String {

        val info = connectivityManager.activeNetworkInfo
        if (info == null || !info.isConnected) return "-" // not connected
        if (info.type == ConnectivityManager.TYPE_WIFI) return "WIFI"
        if (info.type == ConnectivityManager.TYPE_MOBILE) {
            val networkType = info.subtype
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "?"
            }
        }
        return "?"
    }

    fun getSignalStrength(): String {
        val mPhoneStateListener = MyPhoneStateListener()

        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

        return mPhoneStateListener.mSignalStrength.toString()
    }


    fun getOperatorName(): String = telephonyManager.networkOperatorName

    fun isNetworkConnected(): Boolean {
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    private fun getInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = InfoApi.infoService.getInfo()
                info.postValue(response)
            } catch (e: Exception) {
                error.postValue(" There's a problem with your internet connection.")
            }


        }


    }


}

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return InfoViewModel(application) as T
    }
}