package com.stefan.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project

class SimpleRouterPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        /**
         * [gradle8.0 transform替换官方文档](https://developer.android.google.cn/build/releases/gradle-plugin-api-updates?hl=zh-cn)
         * [官方Demo](https://github.com/android/gradle-recipes/blob/agp-7.4/Kotlin/modifyProjectClasses/app/build.gradle.kts#L105)
         */
        target.extensions.getByType(AndroidComponentsExtension::class.java).let { androidComponentsExtension ->
            androidComponentsExtension.onVariants {
                val taskProvider = target.tasks.register("${it.name}RouterTask", RouterTask::class.java)
                it.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(taskProvider)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        RouterTask::allJars,
                        RouterTask::allDirectories,
                        RouterTask::output
                    )
            }
        }
    }
}