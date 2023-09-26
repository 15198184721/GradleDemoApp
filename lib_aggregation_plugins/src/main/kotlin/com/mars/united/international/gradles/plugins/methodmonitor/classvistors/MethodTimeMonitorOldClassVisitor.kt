package com.mars.united.international.gradles.plugins.methodmonitor.classvistors

import com.mars.united.international.gradles.plugins.methodmonitor.classvistors.builds.MethodTimeMonitorBuild
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 实际方法时间统计的转换器
 */
class MethodTimeMonitorOldClassVisitor(private val nextVisitor: ClassVisitor) : ClassVisitor(
    Opcodes.ASM5
) {

    override fun visitEnd() {
        super.visitEnd()
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<clinit>" || name == "<init>") {
            return methodVisitor
        }
        println("MethodMonitorPlugin 正在处理方法：$name")
        val newMethodVisitor =
            object : AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, descriptor) {

                private val methodBuildUtil: MethodTimeMonitorBuild =
                    MethodTimeMonitorBuild(this, mv)

                @Override
                override fun onMethodEnter() {
                    methodBuildUtil.onMethodEnter(access, name, descriptor, signature, exceptions)
                }

                @Override
                override fun onMethodExit(opcode: Int) {
                    methodBuildUtil.onMethodExit(access, name, descriptor, signature, exceptions)
                }
            }
        return newMethodVisitor
    }
}
