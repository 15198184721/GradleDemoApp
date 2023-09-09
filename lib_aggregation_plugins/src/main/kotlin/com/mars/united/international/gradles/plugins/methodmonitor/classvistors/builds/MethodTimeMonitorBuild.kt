package com.mars.united.international.gradles.plugins.methodmonitor.classvistors.builds

import com.android.build.api.instrumentation.ClassContext
import com.mars.united.international.gradles.bases.builds.BaseMethodReBuild
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
    private val classContext: ClassContext,
    adapter: AdviceAdapter,
    mv: MethodVisitor
) : BaseMethodReBuild<AdviceAdapter>(adapter, mv) {

    // 当前方法执行的唯一索引字段的id
    private var currentInvokIndex = -1
    // 唯一标识和方法之间的分隔符
    private val splitKey = "|"

    override fun onMethodEnter(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ) {
        // 输出线程的线程栈
        // Thread.dumpStack()

        //---------------------- 开始插入代码 --------------------------------
        // 在onMethodEnter中插入代码:
        // String key = this+"_"+ UUID.randomUUID();
        // MethodMonitorUtils.startMethod(key);

        // 组装唯一标识的key
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/StringBuilder",
            "<init>",
            "()V",
            false
        )
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitLdcInsn("_")
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/util/UUID",
            "randomUUID",
            "()Ljava/util/UUID;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitLdcInsn(
            "${splitKey}${classContext.currentClassData.className}#$name->$descriptor"
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )

        // 创建一个临时局部变量。将变量引用保存下来
        // 基础类型可以用：Type.xxx_TYPE 代替
        currentInvokIndex = localAdapter.newLocal(
            Type.getType("Ljava.lang.String")
        )
        // 将栈顶值赋值给新的变量
        mv.visitVarInsn(Opcodes.ASTORE, currentInvokIndex)
        // 加载唯一标识到栈顶，并调用开始统计方法
        mv.visitVarInsn(Opcodes.ALOAD, currentInvokIndex)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/mars/united/international/gradles/plugins/methodmonitor/MethodMonitorUtils",
            "startMethod",
            "(Ljava/lang/String;)V",
            false
        )
        //---------------------- 结束插入代码 --------------------------------
    }

    override fun onMethodExit(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ) {

        // 插入代码：MethodMonitorUtils.endMethod(key)

        // 将本地变脸key装载到栈顶。并执行统计方法
        mv.visitVarInsn(Opcodes.ALOAD, currentInvokIndex)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/mars/united/international/gradles/plugins/methodmonitor/MethodMonitorUtils",
            "endMethod",
            "(Ljava/lang/String;)V",
            false
        )
    }
}