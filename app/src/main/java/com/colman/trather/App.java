package com.colman.trather;

import android.app.Application;

import com.colman.trather.services.SharedPref;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPref.init(this);
    }
}

