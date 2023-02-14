package com.teneasy.chatuisdk.ui.main;

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teneasy.chatuisdk.R


class KeFuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kefu)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, KeFuFragment.newInstance())
                .commitNow()
        }
    }

}