package com.stefan.webviewdemo.pre_request

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.stefan.webviewdemo.pre_reuse.WebViewHolder
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object PreRequestUtil {

    private var listener: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun setPreRequestListener(listener: (String) -> Unit) {
        PreRequestUtil.listener = listener
    }

    fun preRequest(url: String) {
        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("chenshan", "HTML 加载失败")
                listener?.invoke("")
                listener = null
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                val body = response.body?.string()
                Log.e("chenshan", "HTML 加载成功 $code & $body")
                mainHandler.post {
                    listener?.invoke(body ?: "")
                    listener = null
                }
            }

        })
    }
}

class PreReqWebViewActivity : AppCompatActivity() {
    companion object {
        fun jump2WebView(context: Context, url: String) {
            PreRequestUtil.preRequest(url)
            context.startActivity(Intent(context, PreReqWebViewActivity::class.java).putExtra("url", url))
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
        webViewModel.data.observe(this) {
            webView.loadUrl("javascript:send2pageData('$it')")
        }

        val container = findViewById<FrameLayout>(R.id.fl_container)
        webView = WebViewHolder.instance.bind(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
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

        PreRequestUtil.setPreRequestListener {
            Log.e("chenshan", "加载什么？ $it")
            if (it.isEmpty()) {
                webView.loadUrl(intent.getStringExtra("url") ?: "")
            } else {
                webView.loadDataWithBaseURL(intent.getStringExtra("url") ?: "", it, "text/html", "utf-8", null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.removeJavascriptInterface("webDemoJSBridge")
        WebViewHolder.instance.release()
    }
}

internal class WebViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String>
        get() = _data

    fun requestPageData() {
        OkHttpClient().newCall(
            Request.Builder()
                .url("https://api3.sungohealth.com/content/article/getArticleInfo")
                .post(
                    FormBody.Builder()
                        .add("articleId", "727")
                        .build()
                )
                .build()
        ).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                _data.postValue(response.body?.string())
            }
        })
    }
}