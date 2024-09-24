plugins {
    `java-gradle-plugin` //此插件自动实现 maven标注、java插件、gradleApi依赖
    `kotlin-dsl` // 启用 Kotlin DSL 插件
    `maven-publish`
}

gradlePlugin{
    plugins {
        create("myPlugin") {
            group = "com.stefan.plugin"
            version = "1.0.0"
            id = "com.stefan.plugin.router"
            implementationClass = "com.stefan.plugin.SimpleRouterPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("maven-repo"))
        }
//        mavenLocal()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.9.0") // Kotlin Gradle Plugin API
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0") // Kotlin 标准库
    implementation("com.android.tools.build:gradle:8.3.0") // Android Gradle 插件 https://developer.android.google.cn/build/releases/gradle-plugin?hl=zh-cn
    //asm
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-commons:9.6")
}