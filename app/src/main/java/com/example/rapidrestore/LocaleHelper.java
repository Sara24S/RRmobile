package com.example.rapidrestore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;
public class LocaleHelper {
    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);  // ⚠️ Important for RTL support

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
    public static void persistLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putString("lang", languageCode).apply();
    }

    public static void loadLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("lang", "en");
        setLocale(context, lang);
    }
}