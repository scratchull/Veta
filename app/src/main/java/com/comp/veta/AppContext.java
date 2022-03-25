package com.comp.veta;

import android.app.Application;
import android.content.Context;

/**
 * This is mostly just a backup way of getting Context within the app
 * It is here for bug fixing
 */
public class AppContext extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        AppContext.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return AppContext.context;
    }
}