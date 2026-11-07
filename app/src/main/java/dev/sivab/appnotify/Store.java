package dev.sivab.appnotify;

import android.content.Context;
import android.content.SharedPreferences;

final class Store {
    private static final String PREFS = "app_notify";
    static final String DEFAULT_URL = "https://jsonplaceholder.typicode.com/todos/1";

    static final String AUTH_NONE = "none";
    static final String AUTH_BEARER = "bearer";
    static final String AUTH_API_KEY = "api_key";

    static final String[] AUTH_IDS = { AUTH_NONE, AUTH_BEARER, AUTH_API_KEY };
    static final String[] AUTH_LABELS = {
            "None (Public HTTPS endpoint)",
            "Bearer Token (Authorization: Bearer …)",
            "API Key in Header (X-API-Key / Custom)"
    };

    static final int[] INTERVAL_MINUTES = { 0, 15, 30, 60, 180, 360 };
    static final String[] INTERVAL_LABELS = {
            "Manual only · Best battery (no background polling)",
            "Every 15 minutes (inexact batch)",
            "Every 30 minutes · Recommended",
            "Every 1 hour",
            "Every 3 hours",
            "Every 6 hours"
    };

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String url(Context c) {
        return prefs(c).getString("url", DEFAULT_URL);
    }

    static String source(Context c) {
        return prefs(c).getString("source", Samples.ENDPOINT);
    }

    static String authMode(Context c) {
        return prefs(c).getString("auth_mode", AUTH_NONE);
    }

    static int authPosition(String mode) {
        for (int i = 0; i < AUTH_IDS.length; i++) {
            if (AUTH_IDS[i].equals(mode)) return i;
        }
        return 0;
    }

    static String authHeader(Context c) {
        String raw = prefs(c).getString("auth_header", "X-API-Key").trim();
        return raw.isEmpty() ? "X-API-Key" : raw;
    }

    static String authSecret(Context c) {
        return prefs(c).getString("auth_secret", "").trim();
    }

    static void clearAuthCredentials(Context c) {
        prefs(c).edit()
                .putString("auth_mode", AUTH_NONE)
                .remove("auth_secret")
                .remove("etag")
                .remove("last_modified")
                .apply();
    }

    static String authSummary(Context c) {
        if (!Samples.ENDPOINT.equals(source(c))) {
            return "Auth: Built-in sample feed";
        }
        String mode = authMode(c);
        boolean hasSecret = !authSecret(c).isEmpty();
        if (AUTH_BEARER.equals(mode)) {
            return hasSecret ? "Auth: Bearer Token configured" : "Auth: Bearer Token (missing token)";
        }
        if (AUTH_API_KEY.equals(mode)) {
            return hasSecret ? "Auth: Header [" + authHeader(c) + "] configured" : "Auth: Header [" + authHeader(c) + "] (missing key)";
        }
        return "Auth: Public HTTPS (none)";
    }

    static int refreshMinutes(Context c) {
        return prefs(c).getInt("refresh_minutes", 30);
    }

    static int intervalPosition(int minutes) {
        for (int i = 0; i < INTERVAL_MINUTES.length; i++) {
            if (INTERVAL_MINUTES[i] == minutes) return i;
        }
        return 2; // Default 30 minutes
    }

    static String scheduleSummary(Context c) {
        int mins = refreshMinutes(c);
        if (mins <= 0) return "Manual refresh only";
        if (mins < 60) return "Every " + mins + " min (inexact)";
        int hours = mins / 60;
        return "Every " + hours + (hours == 1 ? " hr (inexact)" : " hrs (inexact)");
    }

    static boolean respectBatterySaver(Context c) {
        return prefs(c).getBoolean("respect_battery_saver", true);
    }

    static boolean notifyOnlyDiff(Context c) {
        return prefs(c).getBoolean("notify_only_diff", true);
    }

    static boolean liveActive(Context c) {
        return prefs(c).getBoolean("live_active", false);
    }

    static void setLiveActive(Context c, boolean active) {
        prefs(c).edit().putBoolean("live_active", active).apply();
    }

    static String lastHttpStatus(Context c) {
        return prefs(c).getString("last_http_status", "Ready");
    }

    static boolean showWidget(Context c) {
        return prefs(c).getBoolean("widget_" + source(c), true);
    }

    static boolean showWidget(Context c, String source) {
        return prefs(c).getBoolean("widget_" + source, true);
    }

    static boolean showNotification(Context c) {
        String source = source(c);
        return prefs(c).getBoolean("notify_" + source, !Samples.FAMILY.equals(source));
    }

    static String text(Context c) {
        return prefs(c).getString("text", "No data yet. Tap Apply & Sync Live to load.");
    }

    static String updated(Context c) {
        return prefs(c).getString("updated", "Not updated yet");
    }

    static String widgetUpdated(Context c, int id) {
        return prefs(c).getString("widget_updated_" + id, updated(c));
    }

    static void setWidgetUpdated(Context c, int id, String value) {
        prefs(c).edit().putString("widget_updated_" + id, value).apply();
    }

    static long foodDeadline(Context c) {
        return prefs(c).getLong("food_deadline", 0L);
    }

    static long busDeadline(Context c) {
        return prefs(c).getLong("bus_deadline", 0L);
    }

    static boolean supportsPromotedLiveUpdate(String source) {
        return Samples.BUSES.equals(source) || Samples.FOOD.equals(source);
    }

    static long liveDeadline(Context c) {
        if (Samples.BUSES.equals(source(c))) return busDeadline(c);
        if (Samples.FOOD.equals(source(c))) return foodDeadline(c);
        return 0L;
    }

    static int wearableNotificationId(Context c) {
        return prefs(c).getInt("wearable_notification_id", 0);
    }

    static void setWearableNotificationId(Context c, int id) {
        prefs(c).edit().putInt("wearable_notification_id", id).apply();
    }
}
