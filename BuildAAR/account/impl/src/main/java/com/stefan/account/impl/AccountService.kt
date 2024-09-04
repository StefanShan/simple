package com.stefan.account.impl

import com.google.auto.service.AutoService
import com.stefan.account.api.IAccountService

@AutoService(IAccountService::class)
class AccountService: IAccountService {
    override fun getUserInfo(): String = "Hello World"
}