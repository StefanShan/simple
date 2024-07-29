package com.stefan.webviewdemo.cache

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stefan.webviewdemo.R
import com.stefan.webviewdemo.TimeUtil
import com.stefan.webviewdemo.pre_reuse.WebViewHolder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class CacheWebViewActivity : AppCompatActivity() {

    companion object {
        fun jump2WebView(context: Context, url: String) {
            TimeUtil.start()
            context.startActivity(Intent(context, CacheWebViewActivity::class.java).putExtra("url", url))
        }
    }

    private lateinit var webView: WebView
    private val okHttpClient by lazy {
        OkHttpClient.Builder().cache(Cache(File(application.externalCacheDir, "RobustWebView"), 600L * 1024 * 1024)).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val container = findViewById<FrameLayout>(R.id.fl_container)
        val time = findViewById<TextView>(R.id.tv_time)
        webView = WebViewHolder.instance.bind(this)
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null) //启动硬件加速
        webView.settings.apply {
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //支持Https与Http混合请求
            cacheMode = LOAD_NO_CACHE //不缓存
            javaScriptEnabled = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                //拦截h5内链接跳转，全部交由 webView 来加载，防止外链跳转至默认浏览器
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if(time.visibility == View.GONE){
                    time.visibility = View.VISIBLE
                    time.text = "从点击 -> onPageFinished 总耗时 = ${TimeUtil.end()} ms"
                }
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                if (request != null) {
                    if (request.url.scheme == "https" || request.url.scheme == "http") {
                        val urlString = request.url.toString()
                        if (urlString.endsWith(".js", true) ||
                            urlString.endsWith(".css", true) ||
                            urlString.endsWith(".jpg", true) ||
                            urlString.endsWith(".png", true) ||
                            urlString.endsWith(".webp", true)
                        ) {
                            return getHttpResource(request)
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        container.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        webView.loadUrl(intent.getStringExtra("url") ?: "")
    }


    private fun getHttpResource(webResourceRequest: WebResourceRequest): WebResourceResponse? {
        try {
            val url = webResourceRequest.url.toString()
            val requestBuilder =
                Request.Builder()
                    .url(url).method(webResourceRequest.method, null)
            val requestHeaders = webResourceRequest.requestHeaders
            if (!requestHeaders.isNullOrEmpty()) {
                requestHeaders.forEach {
                    requestBuilder.addHeader(it.key, it.value)
                }
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val code = response.code
            if (code != 200) {
                return null
            }
            val body = response.body
            if (body != null) {
                val mimeType = response.header(
                    "content-type", body.contentType()?.type
                )
                val encoding = response.header(
                    "content-encoding",
                    "utf-8"
                )
                val responseHeaders = mutableMapOf<String, String>()
                for (header in response.headers) {
                    responseHeaders[header.first] = header.second
                }
                var message = response.message
                if (message.isBlank()) {
                    message = "OK"
                }
                val resourceResponse =
                    WebResourceResponse(mimeType, encoding, body.byteStream())
                resourceResponse.responseHeaders = responseHeaders
                resourceResponse.setStatusCodeAndReasonPhrase(code, message)
                return resourceResponse
            }
        } catch (e: Throwable) {
            Log.e("stefan", "请求异常 = $e")
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.removeJavascriptInterface("webDemoJSBridge")
        WebViewHolder.instance.release()
    }
}