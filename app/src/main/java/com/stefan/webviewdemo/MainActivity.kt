package com.stefan.webviewdemo

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stefan.webviewdemo.cache.CacheWebViewActivity
import com.stefan.webviewdemo.parallel.ParallelWebViewActivity
import com.stefan.webviewdemo.pre_load.PreLoadUtil
import com.stefan.webviewdemo.pre_load.PreLoadWebViewActivity
import com.stefan.webviewdemo.pre_request.PreReqWebViewActivity
import com.stefan.webviewdemo.pre_reuse.DynamicWebViewActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        //预创建 WebView
        findViewById<TextView>(R.id.tv_jump_webview1).setOnClickListener {
            DynamicWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //并行加载
        findViewById<TextView>(R.id.tv_jump_webview2).setOnClickListener {
            ParallelWebViewActivity.jump2WebView(this, "file:///android_asset/parallel_h5.html")
        }

        //本地缓存
        findViewById<TextView>(R.id.tv_jump_webview3).setOnClickListener {
            CacheWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //预请求
        // 预请求不符合预期，HTML请求之后响应太慢。
        findViewById<TextView>(R.id.tv_jump_webview4).setOnClickListener {
            PreReqWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //预加载
        PreLoadUtil.preload("https://juejin.cn/user/3175045310722119")
        findViewById<TextView>(R.id.tv_jump_webview4).setOnClickListener {
            PreLoadWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("chenshan", "[MainActivity]onResume")
    }
}