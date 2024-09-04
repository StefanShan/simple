package com.stefan.plugin

object ModuleManager {
    //需要转 aar 的模块
    private val gavModule = mutableListOf<String>()
    //需要转 project 的模块
    private val projectModule = mutableListOf<String>()

    fun getAllGavModule() = gavModule
    fun getAllProjectModule() = projectModule

    fun hasGavModule(module: String) = gavModule.contains(module)
    fun hasProjectModule(module: String) = projectModule.contains(module)

    fun addNeedToGavModule(module: List<String>) {
        gavModule.clear()
        gavModule.addAll(module.filter { !hasGavModule(it) })
    }

    fun addNeedToProjectModule(module: List<String>) {
        projectModule.clear()
        projectModule.addAll(module.filter {!hasProjectModule(it) })
    }
}