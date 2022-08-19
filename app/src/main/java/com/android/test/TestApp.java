package com.android.test;

import android.app.Application;

public class TestApp extends Application {
    public static TestApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
