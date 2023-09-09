package com.mars.united.international.gradles.plugins.methodmonitor.factory

import com.android.build.api.instrumentation.*
import com.mars.united.international.gradles.plugins.methodmonitor.classvistors.MethodTimeMonitorClassVisitor
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import com.mars.united.international.gradles.utils.LogUtil
import org.objectweb.asm.ClassVisitor

/**
 * 方法监控的ClassVisitor 工厂
 */
abstract class MethodMonitorClassVisitorFactory() :
    AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return MethodTimeMonitorClassVisitor(classContext,nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        // 检查是否在白名单
        for (processPackage in MethodMonitorConfigHelper.methodMonitorConfig.logWhiteList) {
            if (classData.className.startsWith(processPackage)) {
                return false
            }
        }
        // 检查是否需要处理的类
        for (processPackage in MethodMonitorConfigHelper.methodMonitorConfig.processPackages) {
            if (classData.className.startsWith(processPackage)) {
                LogUtil.logI("需要处理的类：${classData.className}")
                return true
            }
        }
        return false
    }
}