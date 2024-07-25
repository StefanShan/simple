package com.stefan.webviewdemo

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.stefan.webviewdemo.parallel.ParallelWebViewActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0,systemBars.top,0,systemBars.bottom)
            insets
        }
//
//        findViewById<TextView>(R.id.tv_jump_webview1).setOnClickListener {
//            DynamicWebViewActivity.jump2WebView(this,"https://juejin.cn/user/3175045310722119")
//        }
//
//        findViewById<TextView>(R.id.tv_jump_webview2).setOnClickListener {
//            DefaultWebViewActivity.jump2WebView(this,"https://juejin.cn/user/3175045310722119")
////            DynamicWebViewActivity.jump2WebView(this,"https://juejin.cn/post/7043706765879279629")
//        }

        findViewById<TextView>(R.id.tv_jump_webview3).setOnClickListener {
            ParallelWebViewActivity.jump2WebView(this,"file:///android_asset/parallel_h5.html")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("chenshan", "[MainActivity]onResume")
    }
}