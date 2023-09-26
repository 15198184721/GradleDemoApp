package com.mars.united.international.gradles.bases.generates

import com.mars.united.international.gradles.utils.LogUtil
import java.io.File

/**
 * 源代码生成基础类
 * 后期可考虑框架：
 *  javapoet
 * 或者模板：
 *  http://freemarker.foofun.cn/
 *
 */
abstract class BaseGenerateSourceCode(
    /**
     * 生成源代码所在包路径:a.b.c.d
     * 可以理解为顶部的：package a.b.c
     */
    protected val filePackage: String
) {

    init {
        try {
            // 检查路径是否存在
            val file = File(getGenerateSourceFileSaveDir())
            if (!file.exists()) {
                file.mkdirs()

            }
            // 检查包路径是否存在
            val filePack = File(file, filePackage.replace(".", File.separator))
            if (!filePack.exists()) {
                filePack.mkdirs()
            }
            LogUtil.logI("动态代码生成路径：${filePack.absolutePath}")
        } catch (_: Exception) {
        }
    }

    /**
     * 获取生成源文件保存的位置，需要注意的是此位置一定是可被生成管理的路径。也就是说是需要注册过的生成目录
     * 参考Task样例：https://github.com/android/gradle-recipes/blob/agp-8.0/Kotlin/addJavaSourceFromTask/app/build.gradle.kts
     */
    abstract fun getGenerateSourceFileSaveDir(): String

    /**
     * 生成代码
     */
    abstract fun generateCode()

    /**
     * 获取源文件需要保存的路径。<生成路径+包路径> 的全路径
     * @return 全路径
     */
    protected fun getSourceFilePackageSaveDir(): String {
        return File(
            getGenerateSourceFileSaveDir(),
            filePackage.replace(".", File.separator)
        ).absolutePath
    }
}