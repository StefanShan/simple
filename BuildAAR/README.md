# 自动打包发布与手动切换源码/gav Gradle 插件
## 原理请阅读 -> [性能优化-编译优化之模块aar化](https://stefanshan.github.io/blog/doc/Android/%E6%A8%A1%E5%9D%97%E6%89%93%E5%8C%85aar.html)
# 此项目如何使用
1. 先发布 gradle plugin
```shell
./gradlew clean publish
```
2. 将根项目的 build.gradle.kts 中注释取消
```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinKapt) apply false
    //添加插件
//    id("com.stefan.plugin") version "1.0.0"
}

//插件配置
//autoBuildAAR {
//    whitelist = listOf("app", "plugin")
//}
```
3. 修改并执行 test.sh 脚本。首次执行先全部发一遍aar
```shell
sh test.sh
```