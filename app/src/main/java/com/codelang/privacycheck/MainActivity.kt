package com.codelang.privacycheck

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.codelang.jvmticheck.JvmtiHelper
import com.codelang.privacycheck.activity.ApkCheckActivity
import com.codelang.test.ApiCallDemo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // check
        findViewById<View>(R.id.btnCheck).setOnClickListener {
            ApiCallDemo().apply {
                method(this@MainActivity)
                method2(this@MainActivity)
            }
        }

        // query
        findViewById<View>(R.id.btnQuery).setOnClickListener {
            JvmtiHelper.init(this)
            startActivity(Intent(this, ApkCheckActivity::class.java) )
        }
    }
}