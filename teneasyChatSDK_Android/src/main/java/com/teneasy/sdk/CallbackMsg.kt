package com.teneasy.sdk

import java.io.Serializable

class CallbackMsg : Serializable {
    var what = 0
    var msg: String? = null

    constructor() {}

    constructor(msg: String) {
        this.msg = msg
    }

    constructor(what: Int, msg: String?) {
        this.what = what
        this.msg = msg
    }
}