package com.sen5.ocup.util;

import android.util.Log;

/**
 * log工具
 */
public class LogUtils {
    //	/** log开关 */
    public  static  boolean isDebug = true;
    /** 实例 */
    public static LogUtils mLogUtil = new LogUtils();
    /** 默认tag */
    public final static String tag = "[SmartLife]";
    /** Log 默认输出等级 */
    private final static int logLevel = Log.VERBOSE;
    /** 设置默认调试人name */
    private String mDebugMan = "Picker.chen";
    /** 输出的log 信息，信息分两部分 */
    private static String[] logs =new String[2];

    /**
     * @return 返回一个EpgLog实例
     */
    public static LogUtils getInstance() {
        return mLogUtil;
    }

    /**
     * @return 创建自定义的log信息
     */
    private String[] getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            // ClassName
            logs[0] = st.getFileName().replace(".java", "");
            logs[1] = mDebugMan + "[MethodName:"+st.getMethodName()
                    +" line:"+st.getLineNumber() + "]";
            return logs;
        }
        return null;
    }

    /**
     * 输出i等级的log
     * @param str log
     */
    public void i(Object str) {
		/*查询是否开启log*/
        if (isDebug) {
			/*过滤log等级*/
            if (logLevel <= Log.INFO) {
				/*get自定义的log*/
                String log[] = getFunctionName();
				/*最后调用系统log进行输出*/
                if (log != null) {
                    Log.i(log[0], log[1] + " - " + str);
                } else {
                    Log.i(tag, str+"");
                }
            }
        }

    }

    public void d(Object str) {
        if (isDebug) {
            if (logLevel <= Log.DEBUG) {
                String log[] = getFunctionName();
                if (log != null) {
                    Log.d(log[0], log[1] + " - " + str);
                } else {
                    Log.d(tag, str+"");
                }
            }
        }
    }

    public void v(Object str) {
        if (isDebug) {
            if (logLevel <= Log.VERBOSE) {
                String log[] = getFunctionName();
                if (log != null) {
                    Log.v(log[0], log[1] + " - " + str);
                } else {
                    Log.v(tag, str+"");
                }
            }
        }
    }

    public void w(Object str) {
        if (isDebug) {
            if (logLevel <= Log.WARN) {
                String log[] = getFunctionName();
                if (log != null) {
                    Log.w(log[0], log[1] + " - " + str);
                } else {
                    Log.w(tag, str+"");
                }
            }
        }
    }

    public void e(Object str) {
        if (isDebug) {
            if (logLevel <= Log.ERROR) {
                String log[] = getFunctionName();
                if (log != null) {
                    Log.e(log[0], log[1] + " - " + str);
                } else {
                    Log.e(tag, str+"");
                }
            }
        }
    }

    public void e(Exception e) {
        if (isDebug) {
            if (logLevel <= Log.ERROR) {
                Log.e(tag, "error:", e);
            }
        }
    }

    /**
     * @param log log信息
     * @param tr 抛出异常
     */
    public void e(String log, Throwable tr) {
        if (isDebug) {
            String[] line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}"
                    + "[" + mDebugMan + line[0]+"  " +line[1]+ ":] " + log + "\n", tr);
        }
    }
}