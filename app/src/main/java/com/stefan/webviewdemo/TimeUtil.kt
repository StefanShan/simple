package com.stefan.webviewdemo

object TimeUtil {

    private var startTime = 0L
    fun start(){
        startTime = System.currentTimeMillis()
    }

    fun end(): Long = System.currentTimeMillis() - startTime
}