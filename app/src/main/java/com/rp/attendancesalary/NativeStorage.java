package com.rp.attendancesalary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class NativeStorage {
    private static final int REQUEST_CREATE_BACKUP = 4101;
    private static final int REQUEST_OPEN_BACKUP = 4102;

    private final Activity activity;
    private final WebView web;
    private final SharedPreferences prefs;
    private String pendingBackupJson;

    public NativeStorage(Activity activity, WebView web) {
        this.activity = activity;
        this.web = web;
        prefs = activity.getSharedPreferences("attendance_salary_native", Context.MODE_PRIVATE);
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

    @JavascriptInterface
    public void nativeBackup(String fileName, String json) {
        pendingBackupJson = json;
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            activity.startActivityForResult(intent, REQUEST_CREATE_BACKUP);
        });
    }

    @JavascriptInterface
    public void nativeRestore() {
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain", "*/*"});
            activity.startActivityForResult(intent, REQUEST_OPEN_BACKUP);
        });
    }

    public void handleActivityResult(int requestCode, Uri uri) {
        try {
            if (requestCode == REQUEST_CREATE_BACKUP && pendingBackupJson != null) {
                try (OutputStream out = activity.getContentResolver().openOutputStream(uri)) {
                    out.write(pendingBackupJson.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                pendingBackupJson = null;
                runJs("window.nativeBackupSuccess && window.nativeBackupSuccess()");
            } else if (requestCode == REQUEST_OPEN_BACKUP) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(activity.getContentResolver().openInputStream(uri), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line).append('\n');
                }
                String json = sb.toString();
                String quoted = org.json.JSONObject.quote(json);
                runJs("window.nativeRestoreData && window.nativeRestoreData(" + quoted + ")");
            }
        } catch (Exception e) {
            String msg = org.json.JSONObject.quote(e.getMessage() == null ? "File operation failed" : e.getMessage());
            runJs("window.nativeFileError && window.nativeFileError(" + msg + ")");
        }
    }

    private void runJs(String script) {
        activity.runOnUiThread(() -> web.evaluateJavascript(script, null));
    }
}
