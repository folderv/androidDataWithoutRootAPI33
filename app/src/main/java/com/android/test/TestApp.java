package com.android.test;

import androidx.multidex.MultiDexApplication;

public class TestApp extends MultiDexApplication {
    public static TestApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
