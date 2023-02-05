package com.teneasy.sdk.ui

import api.common.CMessage
import java.io.Serializable

class MessageItem{//:Serializable

        var isSend : Boolean = false
        var cMsg: CMessage.Message? = null
        //使用payLoadId来进行发送消息，消息回执的匹配
        var payLoadId: Long = 0
        var status : Int = 0
    }