package com.stefan.apt

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class SimpleRoute (
    val path: String = ""
)