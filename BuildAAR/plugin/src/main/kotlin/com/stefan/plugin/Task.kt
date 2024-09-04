package com.stefan.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.util.LinkedList

//kotlin 实现时需要定义为 open，否则创建失败
//Could not create task of type 'MyTask'.
//> Class Settings_gradle.MyTask is final.

open class FindModifiedModuleTask: DefaultTask() {

    @Internal
    var modifyFile: String?=null

    @Internal
    lateinit var dependencyMap: Map<String, Set<String>>

    @TaskAction
    fun findModifiedModule() {
        //获取修改的模块
        val moduleNames = project.subprojects.filter {
            it.plugins.hasPlugin("com.android.library")
        }
        val modifiedModule = if (modifyFile.isNullOrEmpty()){
            moduleNames.map { it.name }
        }else {
            modifyFile!!.split("\n").mapNotNull { path ->
                return@mapNotNull moduleNames.find {
                    path.contains(it.rootProject.projectDir.toPath().relativize(it.projectDir.toPath()).toString())
                }?.name
            }.toSet()
        }
        //构建打包顺序
        val needBuildModule = mutableSetOf<String>()
        modifiedModule.forEach {
            needBuildModule.add(it)
            needBuildModule.addAll(dependencyMap[it] ?: emptyList())
        }
        val buildOder = LinkedList<String>()
        val inDegree = mutableMapOf<String, Int>()
        for (moduleName in needBuildModule) {
            inDegree.putIfAbsent(moduleName, 0)
            dependencyMap[moduleName]?.forEach {
                inDegree[it] = inDegree.getOrDefault(it, 0) + 1
            }
        }
        val queue = LinkedList<String>()
        inDegree.forEach {
            if (it.value == 0) {
                queue.add(it.key)
            }
        }
        while (queue.isNotEmpty()) {
            val moduleName = queue.pop()
            buildOder.add(moduleName)
            dependencyMap[moduleName]?.forEach {
                inDegree[it] = inDegree.getOrDefault(it, 0) - 1
                if (inDegree[it] == 0) {
                    queue.add(it)
                }
            }
        }
        println("$buildOder")
    }
}