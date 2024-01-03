package com.codelang.lintcheck

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
 * Created by rocketzly on 2020/8/9.
 */
class ApiCallDemo {

    fun method(context: Context) {
        ActivityCompat.requestPermissions(
            context as AppCompatActivity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
    }

    fun method2(context: Context) {
        val telephonyManager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.getDeviceId()


        context.packageManager.getInstalledPackages(0)
    }

}