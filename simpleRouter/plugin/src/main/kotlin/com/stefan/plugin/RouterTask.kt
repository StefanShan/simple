package com.stefan.plugin

import com.stefan.plugin.Constants.ROUTE_API_CLASS
import com.stefan.plugin.Constants.ROUTE_API_CLASS_NAME
import com.stefan.plugin.Constants.ROUTE_API_METHOD_CALL
import com.stefan.plugin.Constants.ROUTE_API_METHOD_INJECT
import com.stefan.plugin.Constants.ROUTE_MAP_CLASS_PREFIX
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

object Constants {
    const val ROUTE_CLASS_PACKAGE = "com/stefan/simpleRouter"
    const val ROUTE_API_CLASS_NAME = "com/stefan/router/SimpleRouter"
    const val ROUTE_API_CLASS = "$ROUTE_API_CLASS_NAME.class"
    const val ROUTE_API_METHOD_INJECT = "initRouteMap"
    const val ROUTE_API_METHOD_CALL = "injectRoute"
    const val ROUTE_MAP_CLASS_PREFIX = "$ROUTE_CLASS_PACKAGE/RouteMap_"
}

abstract class RouterTask : DefaultTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile> // 所有的jar包

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory> // 所有的class文件

    @get:OutputFile
    abstract val output: RegularFileProperty // 输出的文件

    private val routeMapClassList = mutableListOf<String>()
    private var routeApiClass: File? = null

    @TaskAction
    fun taskAction() {
        //构建输出流
        val jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile)))
        //处理jar包
        allJars.get().forEach { file ->
            val jarFile = JarFile(file.asFile)
            jarFile.entries().asIterator().forEach { jarEntry ->
                if (!jarEntry.name.endsWith(".class") || jarEntry.name.contains("META-INF")) return@forEach
                if (jarEntry.name == ROUTE_API_CLASS) {
                    routeApiClass = file.asFile
                    return@forEach
                }
                if (jarEntry.name.startsWith(ROUTE_MAP_CLASS_PREFIX, true) && jarEntry.name.endsWith(".class")) {
                    routeMapClassList.add(jarEntry.name)
                }
                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                jarFile.getInputStream(jarEntry).use {
                    it.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
            jarFile.close()
        }
        //处理class文件
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { file ->
                if (!file.isFile) return@forEach
                val relativePath = directory.asFile.toURI().relativize(file.toURI()).path.replace(File.separatorChar, '/')
                if (relativePath.startsWith(ROUTE_MAP_CLASS_PREFIX, true) && relativePath.endsWith(".class")) {
                    routeMapClassList.add(relativePath)
                }
                jarOutput.putNextEntry(JarEntry(relativePath))
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
        }
        if (routeApiClass == null) return
        //字节码插入
        transform(routeApiClass!!, routeMapClassList, jarOutput)
        jarOutput.close()
    }

    private fun transform(routeApiClass: File, routeMapClassList: List<String>, jarOutput: JarOutputStream) {
        val jarFile = JarFile(routeApiClass)
        jarFile.entries().asIterator().forEach { jarEntry ->
            if (jarEntry.name == ROUTE_API_CLASS) {
                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                asmTransform(jarFile.getInputStream(jarEntry), routeMapClassList).inputStream().use {
                    it.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
        }
        jarFile.close()
    }

    private fun asmTransform(inputStream: InputStream, routeMapClassList: List<String>): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        val classVisitor = SimpleRouteClzVisitor(cw, routeMapClassList)
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }
}

private class SimpleRouteClzVisitor(cv: ClassVisitor, private val routeMapClassList: List<String>) : ClassVisitor(Opcodes.ASM9, cv) {
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == ROUTE_API_METHOD_INJECT) {
            mv = SimpleRouteMethodVisitor(mv, routeMapClassList)
        }
        return mv
    }
}

private class SimpleRouteMethodVisitor(mv: MethodVisitor, private val routeMapClassList: List<String>) : MethodVisitor(Opcodes.ASM9, mv) {

    override fun visitInsn(opcode: Int) {
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            routeMapClassList.map { it.replace("/", ".").substringBeforeLast(".") }.forEach {
                mv.visitLdcInsn(it)
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    ROUTE_API_CLASS_NAME,
                    ROUTE_API_METHOD_CALL,
                    "(Ljava/lang/String;)V",
                    false
                )
            }
            super.visitInsn(opcode)
        }
        mv.visitInsn(opcode)
    }

}

