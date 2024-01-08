package com.codelang.privacycheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.codelang.test.ApiCallDemo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // check
        findViewById<View>(R.id.tvCheck).setOnClickListener {
            ApiCallDemo().apply {
                method(this@MainActivity)
                method2(this@MainActivity)
            }
        }
    }
}