package com.codelang.module.collect

import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.codelang.module.bean.Clazz
import com.codelang.module.bean.ModuleData
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvableDependencies
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.jar.JarFile

object ClazzCollectModule {


    /**
     * @return
     */
    fun collectClazz(project: Project, configurationName: String): List<Clazz> {
        val resolvableDeps =
            project.configurations.getByName(configurationName).incoming
        val jarData = collectDepJarModule(resolvableDeps)
        return parseClazz(jarData)
    }

    /**
     * 获取 clazz
     */
    private fun parseClazz(datas: List<ModuleData>): List<Clazz> {
        return datas.map {
            it.classReaders.map { classReader ->
                getClazz(it, classReader)
            }.toList()
        }.flatten().toList()
    }


    /**
     * 收集依赖 jar module
     */
    private fun collectDepJarModule(resolvableDeps: ResolvableDependencies): List<ModuleData> {
        // 获取 dependencies class.jar
        return resolvableDeps.artifactView { conf ->
            conf.attributes { attr ->
                attr.attribute(
                    AndroidArtifacts.ARTIFACT_TYPE,
                    AndroidArtifacts.ArtifactType.CLASSES_JAR.type
                )
            }
        }.artifacts.map { result ->
            val dep = result.variant.displayName.split(" ").find { it.contains(":") }
                ?: result.variant.displayName
            ModuleData(dep, unzipJar(result.file))
        }.toList()
    }


    private fun unzipJar(file: File): List<ClassReader> {
        // 获取 jar 中的 class 文件
        val jarFile = JarFile(file, false, JarFile.OPEN_READ)
        val jarEntries = jarFile.entries()
        val list = arrayListOf<ClassReader>()
        while (jarEntries.hasMoreElements()) {
            val entry = jarEntries.nextElement()
            if (!entry.isDirectory && entry.name.endsWith(".class") && !entry.name.endsWith("module-info.class")) {
                var ins: InputStream? = null
                try {
                    ins = jarFile.getInputStream(entry)
                    list.add(ClassReader(ins))
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (ins != null) {
                        try {
                            ins.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        return list
    }


    private fun getClazz(moduleData: ModuleData, classReader: ClassReader): Clazz {
        val clazz = Clazz()
        clazz.dep = moduleData.dep
        classReader.accept(ClazzNode(clazz), ClassReader.SKIP_DEBUG)
        return clazz
    }
}

class ClazzNode(private val clazz: Clazz) : ClassNode(Opcodes.ASM9) {
    override fun visitEnd() {
        clazz.className = name
        clazz.superName = superName
        clazz.interfaces = interfaces
        clazz.fields = fields
        clazz.methods = methods
        clazz.visibleAnnotations = visibleAnnotations
    }
}
