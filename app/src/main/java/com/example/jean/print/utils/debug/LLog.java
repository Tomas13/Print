package com.example.jean.print.utils.debug;

import android.content.Context;
import android.util.Log;

import com.example.jean.print.utils.io.IOUtil;

import java.util.Arrays;


public class LLog {

    /*************************************
     * PUBLIC STATIC VARIABLES
     *************************************/
    public static final int MIN_TAG_LENGTH = 30;

    /*************************************
     * PRIVATE STATIC VARIABLES
     *************************************/
    private static boolean sDebuggable = true;
    private static String sFileName;

    /*************************************
     * PUBLIC STATIC METHODS
     *************************************/
    public static void setDebuggable(boolean isDebuggable) {
        sDebuggable = isDebuggable;
    }

    public static void setLogFileName(Context context, String fileName) {
        sFileName = IOUtil.getDataStoragePath(context) + "/" + fileName;
        IOUtil.createFile(sFileName);
    }

    public static void e(Object o, String message) {
        if (sDebuggable) {
            Log.e(createTag(o), message);
            f(o, message);
        }
    }

    public static void f(Object o, String message) {
        if (sDebuggable && sFileName != null) {
            if (IOUtil.isFileExist(sFileName)) {
                IOUtil.appendStringToFile(createTag(o) + ":  " + message, sFileName);
            }
        }
    }

    public static void ef(Object o, String message) {
        if (sDebuggable) {
            e(o, message);
            f(o, message);
        }
    }

    public static void error(Object object, String message, Throwable e) {
        if (sDebuggable) {
            Log.e(createTag(object), message, e);
        }
    }

    public static void error(Object object, Throwable e) {
        if (sDebuggable) {
            error(object, e.getMessage(), e);
        }
    }

    public static void error(Object object, Object... args) {
        if (sDebuggable) {
            Log.e(createTag(object), createMessage(getMethodName(), args));
        }
    }

    public static void error(Class clazz, Object... args) {
        if (sDebuggable) {
            Log.e(createTag(clazz), createMessage(getMethodName(), args));
        }
    }

    public static void info(Object object, Object... args) {
        if (sDebuggable) {
            Log.i(createTag(object), createMessage(getMethodName(), args));
        }
    }

    public static void info(Class clazz, Object... args) {
        if (sDebuggable) {
            Log.i(createTag(clazz), createMessage(getMethodName(), args));
        }
    }

    public static void debug(Object object, Object... args) {
        if (sDebuggable) {
            Log.d(createTag(object), createMessage(getMethodName(), args));
        }
    }

    public static void debug(Class clazz, Object... args) {
        if (sDebuggable) {
            Log.d(createTag(clazz), createMessage(getMethodName(), args));
        }
    }

    public static void trace(Object object, Object... args) {
        if (sDebuggable) {
            Log.v(createTag(object), createMessage(getMethodName(), args));
        }
    }

    public static void trace(Class clazz, Object... args) {
        if (sDebuggable) {
            Log.v(createTag(clazz), createMessage(getMethodName(), args));
        }
    }

    /*************************************
     * PRIVATE STATIC METHODS
     *************************************/
    private static String getMethodName() {
        /**
         * Pavel:
         * I am not sure why, but sometimes I see in logs messages like this = "onStart_aroundBody28"
         * Is contains method name + "_aroundBody%%", where %% is some random number
         * I suggest this is problem of hugo plugin
         * Anyway I get rid of that by split and getting first part before "_"
         */
        return Thread.currentThread().getStackTrace()[4].getMethodName().split("_")[0];
    }

    private static String createTag(Object object) {
        if (object instanceof String) {
            return padStart((String) object, MIN_TAG_LENGTH);
        } else {
            return createTag(object.getClass());
        }
    }

    private static String createTag(Class clazz) {
        //return clazz.getSimpleName().isEmpty() ? clazz.getFeelTypeName() : clazz.getSimpleName();
        return clazz.getSimpleName().isEmpty() ? clazz.getName() : padStart(clazz.getSimpleName(), MIN_TAG_LENGTH);
    }

    private static String createMessage(String methodName, Object... args) {
        //return "? [" + new Date() + "] " + methodName + '(' + (args.length > 0 ? Arrays.toString(args) : "") + ')';
        return methodName + (args.length > 0 ? " (" + Arrays.toString(args) + ")" : "");
    }

    public static String padStart(String s, int length) {
        /**
         * Pavel: changed  to "-->" and move it to tag to better looking in IDEA output
         */
        StringBuilder sb = new StringBuilder();
        sb.append("-->");
        for (int i = 0; i < length - s.length(); i++) {
            sb.append("\u0020");
        }
        sb.append(s);
        return new String(sb);
    }

}
