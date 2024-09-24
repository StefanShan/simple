package com.stefan.router

import android.app.Application
import android.content.Context
import android.content.Intent

object SimpleRouter {

    private val routeMergeMap = RouteMergeMap()
    fun init(context: Application){
        println("SimpleRoute init")
        initRouteMap()
    }

    @JvmStatic
    private fun initRouteMap(){
        //gradle plugin insert code:
        //  injectRoute("com.stefan.simplerouter.RouteMap_2123799187")
    }

    @JvmStatic
    private fun injectRoute(clz: String){
        Class.forName(clz).getMethod("intoMap", Map::class.java).invoke(null, routeMergeMap)
    }

    fun jumpTo(context: Context, path: String){
        if(!routeMergeMap.containsKey(path)){
            println("not find path: $path")
            return
        }
        context.startActivity(Intent(context, Class.forName(routeMergeMap[path]!!)))
    }

    fun getRouteMap() = routeMergeMap

}
