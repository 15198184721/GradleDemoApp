package com.mars.united.international.gradles.plugins.methodmonitor.classvistors.builds

import com.mars.united.international.gradles.bases.builds.BaseMethodReBuild
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 方法时间统计构建,构建时间统计
 * 方案：
 *  1、插入时间记录
 *  2、插入结束耗时计算
 *  3、增加主线程判断
 *  4、增加日志输出
 */
class MethodTimeMonitorBuild(
    adapter: AdviceAdapter,
    mv: MethodVisitor
) : BaseMethodReBuild<AdviceAdapter>(adapter, mv) {

    // 保存 startTime 的局部变量索引
    private var startTimeLocal = -1

    //从配置中读取tag
    val tag = MethodMonitorConfigHelper.methodMonitorConfig.logTag

    override fun onMethodEnter(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ) {
        // 输出线程的线程栈
        // Thread.dumpStack()

        // 在onMethodEnter中插入代码 val startTime = System.currentTimeMillis()
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/System",
            "currentTimeMillis",
            "()J",
            false
        )
        // 创建一个新的局部变量来保存 startTime
        startTimeLocal = localAdapter.newLocal(Type.LONG_TYPE)
        // 插入代码
        mv.visitVarInsn(Opcodes.LSTORE, startTimeLocal)
    }

    override fun onMethodExit(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ) {

        // 在onMethodExit中插入代码 Log.e("tag", "Method: $name, timecost: " + (System.currentTimeMillis() - startTime))
        mv.visitTypeInsn(
            Opcodes.NEW,
            "java/lang/StringBuilder"
        )
        mv.visitInsn(Opcodes.DUP)
        mv.visitLdcInsn("$signature Method: $name, methodTime: ")
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/StringBuilder",
            "<init>",
            "(Ljava/lang/String;)V",
            false
        );
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/System",
            "currentTimeMillis",
            "()J",
            false
        );
        mv.visitVarInsn(Opcodes.LLOAD, startTimeLocal)
        mv.visitInsn(Opcodes.LSUB)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(J)Ljava/lang/StringBuilder;",
            false
        );
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        );
        mv.visitLdcInsn(tag)
        mv.visitInsn(Opcodes.SWAP)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "android/util/Log",
            "e",
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )
        mv.visitInsn(AdviceAdapter.POP)

    }
}