package com.stefan.account.impl

import org.junit.Test

import org.junit.Assert.*
import java.util.LinkedList

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        val dependencyMap = mutableMapOf<String, List<String>>()
        dependencyMap["b"] = listOf("d", "g")
        dependencyMap["f"] = listOf("d", "g", "e")
        dependencyMap["d"] = listOf("h", "e")
        dependencyMap["g"] = listOf("e")
        dependencyMap["h"] = listOf()
        dependencyMap["e"] = listOf()

        val modifiedModule = listOf("b", "f", "d")
        val buildOder = buildAssembleOrder(modifiedModule, dependencyMap)
        println(buildOder)
    }

    fun buildAssembleOrder(modifiedModule: List<String>, dependencyMap: Map<String, List<String>>): List<String>{
        val needBuildModule = mutableSetOf<String>()
        modifiedModule.forEach {
            needBuildModule.add(it)
            needBuildModule.addAll(dependencyMap[it]?: emptyList())
        }
        val buildOder = LinkedList<String>()
        val inDegree = mutableMapOf<String, Int>()
        for(moduleName in needBuildModule){
            inDegree.putIfAbsent(moduleName, 0)
            dependencyMap[moduleName]?.forEach {
                inDegree[it] = inDegree.getOrDefault(it, 0) + 1
            }
        }
        println("inDegree: $inDegree")
        val queue = LinkedList<String>()
        inDegree.forEach {
            if (it.value == 0){
                queue.add(it.key)
            }
        }
        while (queue.isNotEmpty()){
            val moduleName = queue.pop()
            buildOder.add(moduleName)
            dependencyMap[moduleName]?.forEach {
                inDegree[it] = inDegree.getOrDefault(it, 0) - 1
                if (inDegree[it] == 0){
                    queue.add(it)
                }
            }
        }
        return buildOder
    }
}