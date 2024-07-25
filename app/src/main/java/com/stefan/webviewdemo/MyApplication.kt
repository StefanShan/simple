package com.stefan.webviewdemo

import android.app.Application
import android.content.Context
import com.stefan.webviewdemo.pre_reuse.WebViewHolder


class MyApplication : Application(){

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        WebViewHolder.instance.init(this)
    }
}
