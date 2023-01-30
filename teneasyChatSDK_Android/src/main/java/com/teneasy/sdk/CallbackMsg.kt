package com.teneasy.sdk

import java.io.Serializable

class CallbackMsg : Serializable {
    var what = 0
    var msg: String? = null

    constructor() {}
    constructor(what: Int) {
        this.what = what
    }

    constructor(what: Int, msg: String?) {
        this.what = what
        this.msg = msg
    }
}