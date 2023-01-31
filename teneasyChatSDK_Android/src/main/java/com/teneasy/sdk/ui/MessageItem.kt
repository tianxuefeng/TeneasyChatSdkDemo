package com.teneasy.sdk.ui

import java.io.Serializable

class MessageItem(var isSend: Boolean, var msg: String, var payLoadId: Long, var time: String) :
    Serializable