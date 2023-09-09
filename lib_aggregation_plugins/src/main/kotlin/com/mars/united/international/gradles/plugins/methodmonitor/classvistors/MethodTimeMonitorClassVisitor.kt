package com.mars.united.international.gradles.plugins.methodmonitor.classvistors

import com.android.build.api.instrumentation.ClassContext
import com.mars.united.international.gradles.bases.BaseAsmClassNode
import com.mars.united.international.gradles.plugins.methodmonitor.classvistors.builds.MethodTimeMonitorBuild
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 实际方法时间统计的转换器
 */
class MethodTimeMonitorClassVisitor(
    private val classContext: ClassContext,
    private val nextVisitor: ClassVisitor
) : BaseAsmClassNode(
    Opcodes.ASM5
) {

    private val methodWriteList: MutableList<String> by lazy {
        MethodMonitorConfigHelper.methodMonitorConfig.logWhiteList.filter {
            it.contains("#")
        }.toMutableList()
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextVisitor)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<clinit>" && MethodMonitorConfigHelper.methodMonitorConfig.jumpOverClinitMethod) {
            // 排除Class加载方法
            return methodVisitor
        }
        if (name == "<init>" && MethodMonitorConfigHelper.methodMonitorConfig.jumpOverConstructionMethod) {
            // 构造方法
            return methodVisitor
        }
        val currMethod = "${classContext.currentClassData.className}#$name"
        for (processPackage in methodWriteList) {
            if (currMethod == processPackage) {
                // 方法白名单，放弃处理
                return methodVisitor
            }
        }
        println("MethodMonitorPlugin 正在处理方法：${classContext.currentClassData.className}#$name")
        val newMethodVisitor =
            object : AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, descriptor) {

                private val methodBuildUtil: MethodTimeMonitorBuild =
                    MethodTimeMonitorBuild(classContext, this, mv)

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
