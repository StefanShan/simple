package com.stefan.plugin

class ConfigJsonHolder private constructor(){

    companion object{
        val instance by lazy { ConfigJsonHolder() }
    }

    private var _configMap = mutableMapOf<String, ModuleAARConfig>()
    private var blocked = false

    fun init(configList: List<ModuleAARConfig>){
        if (blocked) return
        clearAll()
        addModuleConfigs(configList)
    }

    fun lock(){
        blocked = true
    }

    fun unblock(){
        blocked = false
    }

    fun getConfigList(): List<ModuleAARConfig> = _configMap.values.toList()

    fun getConfig(module: String): ModuleAARConfig? = _configMap[module]

    fun addModuleConfigs(configList: List<ModuleAARConfig>){
        _configMap.putAll(configList.associateBy { it.module })
    }

    fun addModuleConfig(config: ModuleAARConfig){
        _configMap.putIfAbsent(config.module, config)
    }

    fun updateModuleConfig(config: ModuleAARConfig){
        _configMap[config.module] = config
    }

    fun clearAll(){
        _configMap.clear()
    }

    fun clear(module: String){
        _configMap.remove(module)
    }
}