package com.mars.united.international.gradles.bases.builds

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.LocalVariablesSorter

/**
 * 方法构建的基本构建体
 */
abstract class BaseMethodReBuild<T : LocalVariablesSorter>(
    /** 本地环境信息适配器 */
    protected val localAdapter: T,
    /** 方法字节码结构 */
    protected val mv: MethodVisitor
) {

    /**
     * 方法进入
     */
    abstract fun onMethodEnter(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    )

    /**
     * 方法退出
     */
    abstract fun onMethodExit(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    )
}