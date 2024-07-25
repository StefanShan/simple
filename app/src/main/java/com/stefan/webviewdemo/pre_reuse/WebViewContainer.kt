package com.stefan.webviewdemo.pre_reuse

import android.webkit.WebView
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

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

    private class KeyedSoftReference(webView: WebView, queue: ReferenceQueue<WebView>) : SoftReference<WebView>(webView, queue)
}