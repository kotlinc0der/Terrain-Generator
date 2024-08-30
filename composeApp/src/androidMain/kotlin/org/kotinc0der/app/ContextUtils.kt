package org.kotinc0der.app

import android.content.Context

class ContextUtils {
    lateinit var context: Context
    fun initContext(context: Context) {
        this.context = context
    }
    companion object {
        val instance = ContextUtils()
    }

}