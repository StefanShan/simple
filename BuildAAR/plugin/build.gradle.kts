
plugins {
    `java-gradle-plugin` //此插件自动实现 maven标注、java插件、gradleApi依赖
    `kotlin-dsl` // 启用 Kotlin DSL 插件
    `maven-publish`
}

gradlePlugin{
    plugins {
        create("myPlugin") {
            id = "com.stefan.plugin"
            implementationClass = "com.stefan.plugin.ModuleAarPlugin"
        }
    }
}


group = "com.stefan"
version = "1.0.0"
publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("maven-repo"))
        }
        mavenLocal()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.9.0") // Kotlin Gradle Plugin API
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0") // Kotlin 标准库
    implementation("com.google.code.gson:gson:2.10.1")//Gson
}
