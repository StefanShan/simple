package com.stefan.apt

import java.io.File
import java.io.PrintStream
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.math.abs

class AnnotationProcessor : AbstractProcessor(){

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(SimpleRoute::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment): Boolean {
        if (set.isNullOrEmpty()) return false
        val routeList = mutableListOf<RouteBean>()
        //收集被注解类信息
        roundEnvironment.getElementsAnnotatedWith(SimpleRoute::class.java).filterNotNull().forEach {element: Element ->
            val annotation = element.getAnnotation(SimpleRoute::class.java)
            val clsName = element.toString()
            val path = annotation.path
            routeList.add(RouteBean(clsName, path))
        }
        //创建一个类记录被注解类信息
        /**
         * package com.stefan.simpleRouter;
         *
         * /**
         *  * Generated code, Don't modify!!!
         *  * JDK Version is 17.0.9.
         *  */
         * @androidx.annotation.Keep
         * public class RouteMap_566645941 implements com.stefan.apt.IRoute {
         *
         * 	public static void intoMap(java.util.Map<String, String> routeMap) {
         * 		routeMap.put("/main2", "com.stefan.simplerouter.MainActivity2");
         * 	}
         * }
         */
        if (routeList.isEmpty()) return true
        var ps: PrintStream? = null
        try {
            val className = "RouteMap_${abs(processingEnv.filer.createSourceFile("temp").toUri().hashCode())}"
            val jfo = processingEnv.filer.createSourceFile("com.stefan.simpleRouter.$className")
            val genJavaFile = File(jfo.toUri().toString())
            if (genJavaFile.exists()) {
                genJavaFile.delete()
            }

            ps = PrintStream(jfo.openOutputStream(), false, "UTF-8")
            ps.println("package com.stefan.simpleRouter;")
            ps.println()
            ps.println("/**")
            ps.println(" * Generated code, Don't modify!!!")
            ps.println(" * JDK Version is ${System.getProperty("java.version")}.")
            ps.println(" */")
            ps.println("@androidx.annotation.Keep")
            ps.println("public class $className implements com.stefan.apt.IRoute {")
            ps.println()

            ps.println("\tpublic static void intoMap(java.util.Map<String, String> routeMap) {")
            for (item in routeList) {
                ps.println("\t\trouteMap.put(\"${item.path}\", \"${item.className}\");")
            }
            ps.println("\t}")

            ps.println("}")
            ps.flush()
        } finally {
            ps?.close()
        }
        return true
    }
}