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

        findViewById<TextView>(R.id.tv_jump_webview1).setOnClickListener {
            DynamicWebViewActivity.jump2WebView(this,"https://juejin.cn/user/3175045310722119")
        }

        findViewById<TextView>(R.id.tv_jump_webview2).setOnClickListener {
            DynamicWebViewActivity.jump2WebView(this,"https://juejin.cn/post/7043706765879279629")
        }

        findViewById<TextView>(R.id.tv_jump_webview3).setOnClickListener {
            DynamicWebViewActivity.jump2WebView(this,"https://juejin.cn/post/7086284339364757517")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("chenshan", "[MainActivity]onResume")
    }
}