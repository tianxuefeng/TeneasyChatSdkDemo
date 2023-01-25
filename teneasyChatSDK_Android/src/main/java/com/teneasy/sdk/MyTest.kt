package com.teneasy.sdk

import android.content.Context
import android.widget.Toast

class MyTest {
    fun sayHello(context: Context){
        Toast.makeText(context, "Good sdk! Good!", Toast.LENGTH_LONG).show()
    }
}