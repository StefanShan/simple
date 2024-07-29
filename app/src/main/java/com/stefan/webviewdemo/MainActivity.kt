package com.stefan.webviewdemo

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stefan.webviewdemo.cache.CacheWebViewActivity
import com.stefan.webviewdemo.parallel.ParallelWebViewActivity
import com.stefan.webviewdemo.parallel.SerialWebActivity
import com.stefan.webviewdemo.pre_load.PreLoadUtil
import com.stefan.webviewdemo.pre_load.PreLoadWebViewActivity
import com.stefan.webviewdemo.pre_request.PreReqWebViewActivity
import com.stefan.webviewdemo.pre_reuse.DefWebActivity
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

        /**
         * 以下是串行加载与并向加载的区别
         */

        //串行加载
        // 20次实验（去掉最快和最慢的一次）平均耗时 2061ms
        findViewById<TextView>(R.id.tv_jump_webview2_1).setOnClickListener {
            SerialWebActivity.jump2WebView(this, "file:///android_asset/def_h5.html")
        }

        //并行加载
        // 20次实验（去掉最快和最慢的一次）平均耗时 1285ms
        findViewById<TextView>(R.id.tv_jump_webview2).setOnClickListener {
            ParallelWebViewActivity.jump2WebView(this, "file:///android_asset/parallel_h5.html")
        }

        /**
         * 以下是默认 WebView 与 预创建、本地缓存、预请求、预加载分别的对比
         */

        //默认 WebView
        // 20次实验（去掉最快和最慢的一次）平均耗时 3030ms
        findViewById<TextView>(R.id.tv_jump_webview).setOnClickListener {
            DefWebActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //预创建 WebView
        //  20次实验（去掉最快和最慢的一次）平均耗时 2818ms
        findViewById<TextView>(R.id.tv_jump_webview1).setOnClickListener {
            DynamicWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //本地缓存
        // 20次实验（去掉最快和最慢的一次）平均耗时 2657ms
        findViewById<TextView>(R.id.tv_jump_webview3).setOnClickListener {
            CacheWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //预请求
        // 预请求不符合预期，HTML请求之后响应太慢。可能h5优化后会符合预期。
        // 20次实验（去掉最快和最慢的一次）平均耗时 3490ms
        findViewById<TextView>(R.id.tv_jump_webview4).setOnClickListener {
            PreReqWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }

        //预加载
        // 20次实验（去掉最快和最慢的一次）平均耗时 2356ms
        PreLoadUtil.preload("https://juejin.cn/user/3175045310722119")
        findViewById<TextView>(R.id.tv_jump_webview5).setOnClickListener {
            PreLoadWebViewActivity.jump2WebView(this, "https://juejin.cn/user/3175045310722119")
        }
    }
}