package com.stefan.plugin

import org.jetbrains.kotlin.konan.properties.hasProperty
import java.io.File
import java.util.Properties

object LocalPropertyUtil{

    private sealed class State{
        object Default: State()
        object Loaded: State()
        object NotExists: State()
    }

    private var state: State = State.Default
    private lateinit var properties: Properties

    fun load(rootPath: File){
        val localPropertiesFile = File(rootPath, "local.properties")
        if (!localPropertiesFile.exists()){
            state = State.NotExists
            return
        }
        properties = Properties().apply {
            load(localPropertiesFile.inputStream())
        }
        state = State.Loaded
    }

    fun checkNotExists(): Boolean{
        assertLoaded()
        return state == State.NotExists
    }

    fun checkKey(vararg key: String, immediatelyBreak: Boolean = false): Boolean{
        assertLoaded()
        return if (immediatelyBreak) {
            key.any { !properties.hasProperty(it) }
        }else {
            key.all { !properties.hasProperty(it) }
        }.not()
    }

    fun getProperty(key: String, default: String? = null): String?{
        assertLoaded()
        if (!checkKey(key)) return null
        return properties.getProperty(key, default)
    }

    private fun assertLoaded(){
        if (state != State.Loaded){
            throw Throwable("not loaded local.properties file")
        }
    }

}