package com.rp.attendancesalary;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;

public class NativeStorage {
    private final SharedPreferences prefs;

    public NativeStorage(Context context) {
        prefs = context.getSharedPreferences("attendance_salary_native", Context.MODE_PRIVATE);
    }

    @JavascriptInterface
    public String getItem(String key) {
        return prefs.getString(key, null);
    }

    @JavascriptInterface
    public void setItem(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    @JavascriptInterface
    public void removeItem(String key) {
        prefs.edit().remove(key).apply();
    }

    @JavascriptInterface
    public void clear() {
        prefs.edit().clear().apply();
    }
}
