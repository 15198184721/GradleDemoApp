package $package;

import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成的统计工具，由以下插件动态生成：
 * 生成插件：[MethodMonitorPlugin]
 * 生成方式：部分关键字替换
 * 生成作用：用于日志计时、输出逻辑判断、计算输出、输出信息组装等
 */
public class MethodMonitorUtils {
    // 是否只允许检查主进程,T:只检查主进程，F:所有进程
    private static boolean isOnlyAllowedMainThread = $isOnlyAllowedMainThread;

    // 是否需要输出调用栈
    private static boolean logPrintCallStack = $logPrintCallStack;

    // 日志输出的Tag
    private static String logTag = $logTag;

    // 日志简要模式输出
    private static Boolean logAbbreviatedMode = $logAbbreviatedMode;

    // 开启日志过滤模式，如果开启那么单词调用链只输出一次日志(实验性功能)
    private static Boolean logFilterMode = $logFilterMode;

    // 方法的阈值。也就是只有大于这个时间的方法才会被输出日志,单位毫秒
    private static int methodThresholdTime = $methodThresholdTime;

    // 记录方法的时间缓存
    private final static Map<String, Long> timeCacheMap = new HashMap<>();

    //唯一标识和方法之间的分隔符
    private static final String splitKey = "\\|";

    //
    private final static Map<Thread, List<String>> threadLinkCache = new HashMap<>();

    /**
     * 开始记录方法开始时间
     *
     * @param methodStartKey 参数，分两部分组成
     *                       <唯一的key>|<方法的位置,class#methodName>
     */
    public static void startMethod(String methodStartKey) {
        if (!checkLogPrint()) {
            return;
        }
        if(logFilterMode) {
            List<String> list = threadLinkCache.get(Thread.currentThread());
            if (list == null) {
                list = new ArrayList<>();
                threadLinkCache.put(Thread.currentThread(), list);
            }
            list.add(getMethodName(methodStartKey));
        }
        // 之所以在最后。是因为让结果尽可能准确
        long curTime = System.currentTimeMillis();
        timeCacheMap.put(getKey(methodStartKey), curTime);
    }

    /**
     * 计算方法结束时间
     *
     * @param methodStartKey
     */
    public static void endMethod(String methodStartKey) {
        long currentTime = System.currentTimeMillis();
        if (!checkLogPrint()) {
            return;
        }
        long startTime = 0L;
        String key = getKey(methodStartKey);
        if(logFilterMode) {
            List<String> list = threadLinkCache.get(Thread.currentThread());
            if (list == null || list.size() <= 0) {
                threadLinkCache.remove(Thread.currentThread());
                timeCacheMap.remove(key);
                return;
            }
        }
        if (timeCacheMap.get(key) == null) {
            return;
        }
        startTime = timeCacheMap.get(key).longValue();
        timeCacheMap.remove(key);
        long stepTime = currentTime - startTime;
        if (stepTime >= methodThresholdTime) {
            if(logFilterMode) {
                threadLinkCache.remove(Thread.currentThread());
            }
            Log.e(logTag, buildLog(methodStartKey, stepTime));
        } else {
            if(logFilterMode) {
                List<String> tList = threadLinkCache.get(Thread.currentThread());
                tList.remove(getMethodName(methodStartKey));
            }
        }
    }

    // 检查是否满足日志输出条件,T:允许输出，F:不允许输出
    private static boolean checkLogPrint() {
        if (isOnlyAllowedMainThread) {
            return isMainThread();
        }
        return true;
    }

    // 检查是否为主线程
    private static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    // 获取key
    private static String getKey(String methodStartKey) {
        String[] keys = methodStartKey.split(splitKey);
        if (keys.length > 0) {
            return keys[0];
        }
        return "";
    }

    // 获取被调用的方法
    private static String getMethodName(String methodStartKey) {
        String[] keys = methodStartKey.split(splitKey);
        if (keys.length > 1) {
            return keys[1];
        }
        return "<未知方法>";
    }

    // 获取调用栈信息
    private static String getCallStackMessage() {
        // 内部的几个输出方法需要忽略
        ArrayList<String> hlList = new ArrayList<>();
        hlList.add(MethodMonitorUtils.class.getName() + "#endMethod");
        hlList.add(MethodMonitorUtils.class.getName() + "#buildLog");
        hlList.add(MethodMonitorUtils.class.getName() + "#getCallStackMessage");
        // 组装调用链
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        String targetMsg = "";
        Boolean isAdd = false;
        for (int i = 0; i < stack.length; i++) {
            isAdd = true;
            targetMsg = stack[i].getClassName() + "#" + stack[i].getMethodName() + "->" + stack[i].getLineNumber() + "\n";
            for (String fStr : hlList) {
                if (targetMsg.startsWith(fStr)) {
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                sb.append(targetMsg);
            }
        }
        return sb.toString();
    }

    // 构建输出日志
    private static String buildLog(String methodStartKey, long stepTime) {
        StringBuffer sb = new StringBuffer();
        sb.append("警告：发现超过阈值方法：\n");
        if (!logAbbreviatedMode) {
            sb.append("唯一标识 = ");
            sb.append(getKey(methodStartKey) + "\n");
        }
        sb.append("方法 = ");
        sb.append(getMethodName(methodStartKey) + "\n");
        sb.append("执行耗时 = ");
        sb.append(stepTime + "\n");
        if (!logAbbreviatedMode) {
            sb.append("当前线程id = ");
            sb.append(Thread.currentThread().getId() + "\n");
            sb.append("当前线程名称 = ");
            sb.append(Thread.currentThread().getName() + "\n");
            sb.append("是否为主线程 = ");
            sb.append(isMainThread() + "\n");
        }
        if (!logAbbreviatedMode && logPrintCallStack) {
            sb.append("调用栈信息：\n")
                    .append(getCallStackMessage());
        }
        return sb.toString();
    }
}
