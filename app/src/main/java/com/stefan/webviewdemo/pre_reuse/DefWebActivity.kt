package com.stefan.webviewdemo.pre_reuse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stefan.webviewdemo.R
import com.stefan.webviewdemo.TimeUtil
class DefWebActivity : AppCompatActivity() {

    companion object{
        fun jump2WebView(context: Context, url: String){
            TimeUtil.start()
            context.startActivity(Intent(context, DefWebActivity::class.java).putExtra("url", url))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_def_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val webView = findViewById<WebView>(R.id.def_web_view)
        val time = findViewById<TextView>(R.id.tv_def_time)

        webView.webViewClient = object : WebViewClient(){
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

        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null) //启动硬件加速
        webView.settings.apply {
            javaScriptEnabled = true //启用JavaScript
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //支持Https与Http混合请求
            cacheMode = WebSettings.LOAD_NO_CACHE //不使用缓存
        }
        webView.loadUrl(intent.getStringExtra("url")?:"")
    }
}