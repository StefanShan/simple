package com.stefan.webviewdemo

import android.app.Application
import android.content.Context
import android.content.MutableContextWrapper
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.net.URI


class MyApplication : Application(){

    var webViewContainer = mutableListOf<SoftReference<WebView>>()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        WebViewHolder.instance.init(this)
    }
}

class WebViewHolder private constructor(){
    companion object{
        val instance by lazy {  WebViewHolder() }
    }
    private val maxCacheSize = 3
    private val emptyUrl = "about:blank"
    private val webViewContainer = WebViewContainer()

    fun init(appContext: Context){
        if (webViewContainer.isEmpty()){
            Looper.getMainLooper().queue.addIdleHandler {
                webViewContainer.add(WebView(MutableContextWrapper(appContext)))
                return@addIdleHandler false
            }
        }
    }

    fun bind(context: Context): WebView{
        val webView = webViewContainer.peek() ?: return WebView(MutableContextWrapper(context))
        webViewContainer.remove(webView)
        (webView.context as MutableContextWrapper).baseContext = context
        webView.clearHistory()
        return webView
    }

    fun recycle(webView: WebView){
        try {
            webView.stopLoading()
            webView.loadUrl(emptyUrl)
            webView.clearHistory()
            webView.pauseTimers()
            webView.removeAllViews()
            webView.webChromeClient = null
            (webView.parent as? ViewGroup)?.removeView(webView)
            (webView.context as MutableContextWrapper).baseContext = webView.context.applicationContext
        }finally {
            if (webViewContainer.size() < maxCacheSize)
            webViewContainer.add(webView)
        }
    }

    fun canGoBack(webView: WebView): Boolean{
        if (webView.canGoBack()){
            val backForwardList = webView.copyBackForwardList()
            if (backForwardList.getItemAtIndex(backForwardList.currentIndex - 1).url != emptyUrl){
                return true
            }
        }
        return false
    }

    fun release(){
        webViewContainer.clear()
    }
}

class WebViewContainer {
    private var queue = ReferenceQueue<WebView>()
    private var webViewContainer = mutableMapOf<Int, KeyedSoftReference>()

    fun add(webView: WebView){
        webViewContainer[webView.hashCode()] = KeyedSoftReference(webView, queue)
    }

    fun peek(): WebView?{
        removeSoftReachableObjects()
        return webViewContainer.firstNotNullOfOrNull { it.value.get() }
    }

    fun remove(webView: WebView){
        webViewContainer.remove(webView.hashCode())
    }

    fun clear(){
        webViewContainer.values.forEach {
            it.get()?.removeAllViews()
            it.get()?.destroy()
            it.clear()
        }
        webViewContainer.clear()
    }

    fun size (): Int{
        removeSoftReachableObjects()
        return webViewContainer.size
    }

    fun isEmpty(): Boolean{
        removeSoftReachableObjects()
        return webViewContainer.isEmpty()
    }

    private fun removeSoftReachableObjects(){
        var ref: KeyedSoftReference?
        do {
            ref = queue.poll() as? KeyedSoftReference
            if (ref!= null){
                webViewContainer.remove(ref.get()?.hashCode())
            }
        }while (ref != null)
    }

    private class KeyedSoftReference(webView: WebView, queue: ReferenceQueue<WebView>) : SoftReference<WebView>(webView, queue){
    }
}