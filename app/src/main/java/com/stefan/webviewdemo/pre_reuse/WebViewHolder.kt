package com.stefan.webviewdemo.pre_reuse

import android.content.Context
import android.content.MutableContextWrapper
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebView

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

    fun bind(context: Context): WebView {
        val webView = webViewContainer.peek() ?: return WebView(MutableContextWrapper(context))
        webViewContainer.remove(webView)
        return webView.also {
            (it.context as MutableContextWrapper).baseContext = context
            it.clearHistory()
        }
    }

    fun recycle(webView: WebView){
        try {
            webView.apply {
                stopLoading()
                loadUrl(emptyUrl)
                clearHistory()
                removeAllViews()
                webChromeClient = null
                (parent as? ViewGroup)?.removeView(webView)
                (context as MutableContextWrapper).baseContext = webView.context.applicationContext
            }
        }finally {
            if (webViewContainer.size() < maxCacheSize){
                webViewContainer.add(webView)
            }
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
