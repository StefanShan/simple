package com.stefan.webviewdemo.parallel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stefan.webviewdemo.R
import com.stefan.webviewdemo.TimeUtil
import com.stefan.webviewdemo.pre_reuse.WebViewHolder
import kotlin.random.Random

class ParallelWebViewActivity : AppCompatActivity() {

    companion object{
        fun jump2WebView(context: Context, url: String){
            context.startActivity(Intent(context, ParallelWebViewActivity::class.java).putExtra("url", url))
        }
    }

    private lateinit var webView: WebView
    private lateinit var webViewModel: WebViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //并行请求数据
        webViewModel = ViewModelProvider(this)[WebViewModel::class.java]
        webViewModel.requestPageData()
        webViewModel.data.observe(this){
            webView.loadUrl("javascript:send2pageData('$it')")
        }

        val container = findViewById<FrameLayout>(R.id.fl_container)
        webView = WebViewHolder.instance.bind(this)
        webView.settings.javaScriptEnabled=true
        webView.addJavascriptInterface(JSBridge(webViewModel),"webDemoJSBridge")
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                //拦截h5内链接跳转，全部交由 webView 来加载，防止外链跳转至默认浏览器
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        container.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        webView.loadUrl(intent.getStringExtra("url")?:"")
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.removeJavascriptInterface("webDemoJSBridge")
        WebViewHolder.instance.release()
    }
}

internal class JSBridge(private val webViewModel: WebViewModel) {
    @JavascriptInterface
    fun getPageData(): String{
        return webViewModel.data.value?:""
    }

    @JavascriptInterface
    fun onDOMLoaded(){
        Log.e("chenshan", "onDOMLoaded -> ${TimeUtil.end()}")
    }

    @JavascriptInterface
    fun onPageLoaded(){
        Log.e("chenshan", "onPageLoaded -> ${TimeUtil.end()}")
    }
}

internal class WebViewModel : ViewModel(){
    private val _data = MutableLiveData<String>()
    val data: LiveData<String>
        get() = _data

    fun requestPageData(){
        //模拟客户端请求数据
        Handler(Looper.getMainLooper()).postDelayed({
            val data = "{\n" +
                    "    \"picUrl\": \"https://p6.music.126.net/obj/wonDlsKUwrLClGjCm8Kx/44658970730/341c/8047/eb07/a960603716d5d927a5bfb9c961329f95.jpg\",\n" +
                    "    \"title\": \"AI 入门到放弃之 Transformer\",\n" +
                    "    \"clickNum\": 266,\n" +
                    "    \"author\": \"测试数据\",\n" +
                    "    \"content\": \"让模型在处理一个序列元素时，能够考虑到序列中所有元素的信息，而不仅仅是相邻的元素。考虑所有元素信息的关键：自注意力机制 + 位置编码。Transformer模型主要由两部分组成：编码器（Encoder）\u200C和解码器（Decoder）\u200C。编码器负责将输入序列转换为一系列连续的向量表示，而解码器则利用这些向量表示以及先前生成的输出序列来生成最终的输出。\",\n" +
                    "    \"description\": \"让模型在处理一个序列元素时，能够考虑到序列中所有元素的信息，而不仅仅是相邻的元素\"\n" +
                    "}"
            _data.value = data
        }, Random.nextLong(50,200))
    }
}