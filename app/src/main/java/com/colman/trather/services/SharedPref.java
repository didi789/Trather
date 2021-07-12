package com.colman.trather.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPref {

    private static SharedPreferences sharedPref;

    public static void init(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean containsKey(String key) {
        return sharedPref.contains(key);
    }

    public static void putInt(String key, Integer value) {
        sharedPref.edit().putInt(key, value).apply();
    }

    public static Integer getInt(String key, Integer defValue) {
        return sharedPref.getInt(key, defValue);
    }

    public static void putFloat(String key, Float value) {
        sharedPref.edit().putFloat(key, value).apply();
    }

    public static Float getFloat(String key, Float defValue) {
        return sharedPref.getFloat(key, defValue);
    }

    public static Integer updateInt(String key, Integer operation, Integer defValue) {
        int updatedInt = sharedPref.getInt(key, defValue) + operation;
        sharedPref.edit().putInt(key, updatedInt).apply();
        return updatedInt;
    }

    public static void putBoolean(String key, boolean value) {
        sharedPref.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return sharedPref.getBoolean(key, defValue);
    }

    public static void putLong(String key, long value) {
        sharedPref.edit().putLong(key, value).apply();
    }

    public static long getLong(String key, long defValue) {
        return sharedPref.getLong(key, defValue);
    }

    public static void putString(String key, String value) {
        sharedPref.edit().putString(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        return sharedPref.getString(key, defValue);
    }

    public static void removeKey(String key) {
        sharedPref.edit().remove(key).apply();
    }

}
