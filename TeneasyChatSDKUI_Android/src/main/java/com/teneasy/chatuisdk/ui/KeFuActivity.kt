package com.teneasy.chatuisdk.ui.main;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teneasy.chatuisdk.R


class KeFuActivity : AppCompatActivity() {

    var TAG_FRAGMENT = "KeFuFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kefu)
        if (savedInstanceState == null) {
            // transaction.replace(R.id.fragment, fragment, TAG_FRAGMENT);
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, KeFuFragment.newInstance(), TAG_FRAGMENT)
                .commitNow()
        }
    }

    override fun onBackPressed() {
        val fragment: KeFuFragment? =
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as KeFuFragment?
        /*if (fragment.allowBackPressed()) { // and then you define a method allowBackPressed with the logic to allow back pressed or not
            super.onBackPressed()
        }*/
        if (fragment != null){
            fragment.exit()
        }
        super.onBackPressed()
    }
}