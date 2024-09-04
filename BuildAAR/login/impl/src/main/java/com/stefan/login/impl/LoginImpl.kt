package com.stefan.login.impl

import android.util.Log
import com.google.auto.service.AutoService
import com.stefan.account.api.IAccountService
import com.stefan.login.api.ILogin
import java.util.ServiceLoader

@AutoService(ILogin::class)
class LoginImpl: ILogin {
    override fun jumpToLogin() {
        val userInfo = ServiceLoader.load(IAccountService::class.java).firstOrNull()?.getUserInfo()
        Log.e("stefan", "jumpToLogin -> $userInfo")
    }
}