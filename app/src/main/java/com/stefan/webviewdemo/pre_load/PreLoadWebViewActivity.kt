package com.stefan.webviewdemo.pre_load

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
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
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object PreLoadUtil {

    private var current: String = ""

    fun getPreLoadHTML(): String = current
    fun preload(url: String) {
        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("stefan", "HTML 加载失败")
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                val body = response.body?.string()
                Log.e("stefan", "HTML 加载成功 $code & $body")
                current = body ?: ""
            }

        })
    }
}

class PreLoadWebViewActivity : AppCompatActivity() {
    companion object {
        fun jump2WebView(context: Context, url: String) {
            TimeUtil.start()
            context.startActivity(Intent(context, PreLoadWebViewActivity::class.java).putExtra("url", url))
        }
    }

    private lateinit var webView: WebView

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
        webView = WebView(this)
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null) //启动硬件加速
        webView.settings.apply {
            javaScriptEnabled = true //启用JavaScript
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //支持Https与Http混合请求
            cacheMode = WebSettings.LOAD_NO_CACHE //不使用缓存
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
        }
        container.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        if (PreLoadUtil.getPreLoadHTML().isEmpty()) {
            webView.loadUrl(intent.getStringExtra("url") ?: "")
        } else {
            webView.loadDataWithBaseURL(intent.getStringExtra("url") ?: "", PreLoadUtil.getPreLoadHTML(), "text/html", "utf-8", null)
        }
    }
}