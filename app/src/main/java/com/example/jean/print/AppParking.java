package com.example.jean.print;

import android.app.Application;
import android.content.Context;

import kz.otgroup.parking.utils.debug.LLog;

/**
 * Created by kkurtukov on 17.10.2016.
 */

public class AppParking extends Application {

    private static final String TAG = AppParking.class.getSimpleName();

    /*************************************
     * PRIVATE STATIC FIELDS
     *************************************/
    private static AppParking sInstance;

    /*************************************
     * PUBLIC METHODS
     *************************************/
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        initDebuggingTools();
    }

    /*************************************
     * PUBLIC STATIC METHODS
     *************************************/
    public static Context getContext() {
        return sInstance;
    }

    /*************************************
     * PRIVATE METHODS
     *************************************/

    private void initDebuggingTools() {
        initLogger();
    }

    private void initLogger() {
        LLog.setLogFileName(this, "log.txt");
        LLog.setDebuggable(true);
        LLog.e(TAG, "onCreate");
    }
}
