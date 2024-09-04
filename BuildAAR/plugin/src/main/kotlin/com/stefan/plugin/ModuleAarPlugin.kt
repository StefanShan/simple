package com.stefan.plugin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import java.io.File
import kotlin.collections.List
import kotlin.collections.Set
import kotlin.collections.emptySet
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.lastOrNull
import kotlin.collections.map
import kotlin.collections.mapIndexed
import kotlin.collections.mutableMapOf
import kotlin.collections.partition
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.toMutableList

class ModuleAarPlugin : Plugin<Project> {

    private lateinit var excludeModules: List<String>
    private lateinit var applicationId: String
    private val dependenciesMap = mutableMapOf<String, Set<String>>()
    private val reverseDependenciesMap = mutableMapOf<String, Set<String>>()

    override fun apply(project: Project) {

        val extension = project.extensions.create("autoBuildAAR", PluginExtension::class.java)

        project.gradle.projectsEvaluated {
            //解析 module_aar.json 获取模块源码与aar的映射
            val moduleAARFile = File(rootProject.projectDir, "module_aar.json")
            if (!moduleAARFile.exists()) return@projectsEvaluated
            ConfigJsonHolder.instance.init(
                Gson().fromJson(moduleAARFile.readText(), object : TypeToken<List<ModuleAARConfig>>() {}.type)
            )
            //解析 local.properties 获取源码引入的模块
            LocalPropertyUtil.load(rootProject.projectDir)
            if (LocalPropertyUtil.checkNotExists() || !LocalPropertyUtil.checkKey("allLocalModule", "localModule")) {
                //local.properties 不存在 or 没有配置allLocalModule 和 localModule => 全部切换成 aar
                ModuleManager.addNeedToGavModule(rootProject.allprojects.map { it.name }.filter { it != rootProject.name })
            } else if (LocalPropertyUtil.getProperty("allLocalModule") == "true") {
                //全部切换成 源码
                ModuleManager.addNeedToProjectModule(rootProject.allprojects.map { it.name }.filter { it != rootProject.name })
            } else {
                //获取配置的源码模块
                val localModules = if (!LocalPropertyUtil.checkKey("localModule")) emptyList() else
                    Gson().fromJson<List<String>>(LocalPropertyUtil.getProperty("localModule"), object : TypeToken<List<String>>() {}.type)
                //遍历所有模块，将不在配置或白名单的模块进行分组
                rootProject.allprojects.map { it.name }.filter { it != rootProject.name }.partition { it !in excludeModules && it !in localModules}.let {
                    ModuleManager.addNeedToGavModule(it.first)
                    ModuleManager.addNeedToProjectModule(it.second)
                }
            }
            gradle.allprojects {
                val subProject = this
                if (subProject.name != rootProject.name) {
                    subProject.configurations.all {
                        //获取project方式依赖的关系
                        if (subProject.plugins.hasPlugin("com.android.library") && name.contains("implementation") || name.contains("compileOnly")) {
                            dependencies.filter { dep -> dep.group == rootProject.name }.forEach { dep ->
                                reverseDependenciesMap[subProject.name] =
                                    reverseDependenciesMap.getOrDefault(subProject.name, emptySet()) + dep.name
                                dependenciesMap[dep.name] = dependenciesMap.getOrDefault(dep.name, emptySet()) + subProject.name
                            }
                        }
                        //依赖替换
                        resolutionStrategy.dependencySubstitution {
                            ModuleManager.getAllGavModule().forEach {
                                ConfigJsonHolder.instance.getConfig(it)?.gav?.let { gav ->
                                    substitute(project(":$it")).using(module(gav))
                                }
                            }
                            ModuleManager.getAllProjectModule().forEach {
                                ConfigJsonHolder.instance.getConfig(it)?.gav?.let { gav ->
                                    substitute(module(gav)).using(project(":$it"))
                                }
                            }
                        }
                    }
                }
            }
        }

        project.gradle.afterProject {
            val subProject = this
            if (name != rootProject.name && plugins.hasPlugin("com.android.library")) {
                //配置 maven-publish
                // 需要在 projectEvaluated 之前注入，否则报错：
                // Failed to apply plugin class 'org.gradle.api.publish.plugins.PublishingPlugin'.
                //> Cannot run Project.afterEvaluate(Action) when the project is already evaluated.
                plugins.apply("maven-publish")
                components.whenObjectAdded {
                    if (this.name != "release") return@whenObjectAdded
                    extensions.configure<PublishingExtension> {
                        publications {
                            create<MavenPublication>("releaseAar") {
                                groupId = applicationId
                                artifactId = this@afterProject.name
                                version = ConfigJsonHolder.instance.getConfig(subProject.name)?.gav?.split(":")?.lastOrNull()?: "0.0.1"
                                from(this@whenObjectAdded)
                            }
                        }
                        repositories {
                            mavenLocal()
                        }
                    }
                }
            }
            if (name == rootProject.name) {
                extension.apply {
                    applicationId = mavenGroupId ?: "com.${rootProject.name}.module"
                    excludeModules = whitelist ?: subProject.allprojects.find {
                        it.plugins.hasPlugin("com.android.application")
                    }?.name.run {
                        return@run this?.split(",") ?: emptyList()
                    }
                }
                /**
                 * 创建脚本使用的 gradle Task
                 */
                /* 获取需要打包的模块 */
                tasks.register("findModifiedModule", FindModifiedModuleTask::class.java) {
                    modifyFile = if (subProject.hasProperty("modifyFile")) {
                        subProject.property("modifyFile")?.toString()
                    } else null
                    dependencyMap = dependenciesMap
                    doLast {
                        ConfigJsonHolder.instance.lock()
                    }
                }
                /* 创建 maven 发布配置，并更新 ConfigHolder */
                tasks.register("configMaven") {
                    if (!subProject.hasProperty("module")) return@register
                    val moduleName = subProject.property("module")?.toString() ?: return@register
                    val moduleProject = subProject.allprojects.find { it.name == moduleName } ?: return@register
                    val moduleConfig = ConfigJsonHolder.instance.getConfig(moduleName)
                    moduleProject.extensions.getByType(PublishingExtension::class.java).apply {
                       (publications.getByName("releaseAar") as MavenPublication).let {
                           it.version = incrementVersion(it.version)
                       }
                    }
                    //发布aar
                    finalizedBy(moduleProject.tasks.findByName("publishToMavenLocal"))
                    //更新 ConfigHolder
                    val mavenVersion = moduleProject.extensions.getByType(PublishingExtension::class.java).let {
                        (it.publications.getByName("releaseAar") as MavenPublication).version
                    }
                    moduleProject.tasks.findByName("publishToMavenLocal")?.doLast {
                        if (moduleConfig == null) {
                            //新组件
                            ConfigJsonHolder.instance.addModuleConfig(
                                ModuleAARConfig(
                                    moduleName,
                                    moduleProject.rootProject.projectDir.toPath().relativize(moduleProject.projectDir.toPath()).toString(),
                                    "${applicationId}:${moduleName}:${mavenVersion}"
                                )
                            )
                        } else {
                            //旧组件,更新版本号
                            ConfigJsonHolder.instance.updateModuleConfig(
                                moduleConfig.copy(
                                    gav = moduleConfig.gav.split(":").mapIndexed { index, s ->
                                        if (index == 2) mavenVersion else s
                                    }.joinToString(":")
                                )
                            )
                        }
                    }
                }
                /* 刷新 module_aar.json 文件 */
                tasks.register("updateConfig") {
                    doLast {
                        //更新 module_aar.json
                        if (ConfigJsonHolder.instance.getConfigList().isEmpty()) return@doLast
                        val moduleAARFile = File(project.projectDir, "module_aar.json")
                        if (!moduleAARFile.exists()) {
                            moduleAARFile.createNewFile()
                        }
                        //将 ConfigHolder 转成 json 覆盖写入文件
                        moduleAARFile.writeText(Gson().toJson(ConfigJsonHolder.instance.getConfigList()))
                        ConfigJsonHolder.instance.unblock()
                    }
                }
            }
        }
    }


    private fun incrementVersion(originalVersion: String?): String? {
        if (originalVersion == null) return null
        // 拆分版本号成 x, y, z
        val versionParts = originalVersion.split(".").map { it.toInt() }.toMutableList()
        if (versionParts.size != 3) {
            throw IllegalArgumentException("版本号格式不正确，应为 x.y.z 格式")
        }

        // 递增第三级版本号
        versionParts[2] += 1

        // 如果第三级版本号超过99，则重置为0并递增第二级版本号
        if (versionParts[2] > 99) {
            versionParts[2] = 0
            versionParts[1] += 1
        }

        // 如果第二级版本号超过99，则重置为0并递增第一级版本号
        if (versionParts[1] > 99) {
            versionParts[1] = 0
            versionParts[0] += 1
        }

        // 返回更新后的版本号
        return versionParts.joinToString(".")
    }
}