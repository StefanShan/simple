package com.stefan.webviewdemo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DynamicWebViewActivity : AppCompatActivity() {

    companion object{
        fun jump2WebView(context: Context, url: String){
            context.startActivity(Intent(context, DynamicWebViewActivity::class.java).putExtra("url", url))
        }
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dynamic_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val container = findViewById<FrameLayout>(R.id.fl_container)

        webView = WebViewHolder.instance.bind(this)
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null) //启动硬件加速
        webView.settings.apply {
            javaScriptEnabled = true //启用JavaScript
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //支持Https与Http混合请求
        }
        container.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        webView.loadUrl(intent.getStringExtra("url")?:"")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //需要代理判断 canGoBack
        if (keyCode == KeyEvent.KEYCODE_BACK && WebViewHolder.instance.canGoBack(webView)) {
           webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        WebViewHolder.instance.recycle(webView)
        super.onDestroy()
    }
}