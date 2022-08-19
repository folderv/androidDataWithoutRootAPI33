package com.android.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class JavaMainActivity extends AppCompatActivity {

    private static final String TAG = "JavaMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_main);
    }
}