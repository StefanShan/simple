package com.stefan.router

class RouteMergeMap: HashMap<String,String>(){

    override fun put(key: String, value: String): String? {
        if (containsKey(key)){
            throw RuntimeException("路由冲突：$key")
        }
        return super.put(key, value)
    }
}