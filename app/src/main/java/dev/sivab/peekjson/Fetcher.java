package dev.sivab.peekjson;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.PowerManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Fetcher {
    interface Callback { void done(String formatted, Exception error); }
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    static boolean shouldSkipBackgroundSync(Context context) {
        if (Store.respectBatterySaver(context)) {
            PowerManager power = context.getSystemService(PowerManager.class);
            if (power != null && power.isPowerSaveMode()) {
                return true;
            }
            ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
            if (cm != null && cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED) {
                return true;
            }
        }
        return !isNetworkAvailable(context);
    }

    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        if (cm == null) return true;
        Network active = cm.getActiveNetwork();
        if (active == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    static void refresh(Context context, boolean userInitiated, Callback callback) {
        Context app = context.getApplicationContext();
        IO.execute(() -> {
            try {
                String source = Store.source(app);
                String sample = Samples.json(source);
                String formatted;
                String statusTag;

                if (sample != null) {
                    formatted = JsonFormatter.format(sample);
                    statusTag = "Sample feed · Ready";
                } else {
                    if (!userInitiated && shouldSkipBackgroundSync(app)) {
                        if (callback != null) callback.done(Store.text(app), null);
                        return;
                    }
                    String targetUrl = Store.url(app).trim();
                    if (!targetUrl.startsWith("https://")) {
                        throw new Exception("Endpoint must use HTTPS (https://)");
                    }

                    HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setUseCaches(true);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("User-Agent", "PeekJson/1.2 (Android; BatteryOptimized)");

                    // Attach authentication header if configured
                    String authMode = Store.authMode(app);
                    String secret = Store.authSecret(app);
                    if (Store.AUTH_BEARER.equals(authMode) && !secret.isEmpty()) {
                        String token = secret.regionMatches(true, 0, "Bearer ", 0, 7)
                                ? secret.substring(7).trim()
                                : secret;
                        connection.setRequestProperty("Authorization", "Bearer " + token);
                    } else if (Store.AUTH_API_KEY.equals(authMode) && !secret.isEmpty()) {
                        String headerName = sanitizeHeaderName(Store.authHeader(app));
                        connection.setRequestProperty(headerName, secret);
                    }

                    // Attach conditional HTTP headers (ETag / Last-Modified) to save battery & bandwidth
                    SharedPreferences prefs = Store.prefs(app);
                    String cachedUrl = prefs.getString("etag_url", "");
                    if (targetUrl.equals(cachedUrl)) {
                        String etag = prefs.getString("etag", "");
                        String lastMod = prefs.getString("last_modified", "");
                        if (!etag.isEmpty()) connection.setRequestProperty("If-None-Match", etag);
                        if (!lastMod.isEmpty()) connection.setRequestProperty("If-Modified-Since", lastMod);
                    }

                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        connection.disconnect();
                        String updated = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());
                        prefs.edit()
                                .putString("updated", updated)
                                .putString("last_http_status", "HTTP 304 Not Modified · Cached")
                                .apply();
                        DataWidget.updateAll(app);
                        if (callback != null) callback.done(Store.text(app), null);
                        return;
                    }

                    InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
                    StringBuilder raw = new StringBuilder();
                    if (stream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                raw.append(line).append('\n');
                            }
                        }
                    }
                    String responseEtag = connection.getHeaderField("ETag");
                    String responseLastMod = connection.getHeaderField("Last-Modified");
                    connection.disconnect();

                    if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
                        throw new Exception("HTTP " + code + " Unauthorized — Verify your API Key or Bearer Token.");
                    }
                    if (code < 200 || code >= 300) {
                        throw new Exception("HTTP " + code + " — Endpoint returned an error status.");
                    }

                    formatted = JsonFormatter.format(raw.toString());
                    statusTag = "HTTP " + code + (responseEtag != null ? " · ETag cached" : " · OK");

                    SharedPreferences.Editor cacheEdit = prefs.edit().putString("etag_url", targetUrl);
                    cacheEdit.putString("etag", responseEtag != null ? responseEtag : "");
                    cacheEdit.putString("last_modified", responseLastMod != null ? responseLastMod : "");
                    cacheEdit.apply();
                }

                String old = Store.text(app);
                boolean changed = !old.equals(formatted);
                String updated = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());
                Store.prefs(app).edit()
                        .putString("text", formatted)
                        .putString("updated", updated)
                        .putString("last_http_status", statusTag)
                        .apply();

                DataWidget.updateAll(app);

                if (!Store.showNotification(app)) {
                    Notifier.cancelLive(app);
                } else {
                    boolean shouldAlert = !Store.notifyOnlyDiff(app) || changed || userInitiated;
                    if (shouldAlert) {
                        String alert = ContentRenderer.notification(app, formatted);
                        Notifier.show(app, alert, false);
                        if (changed || userInitiated) {
                            Notifier.showWearableAlert(app, ContentRenderer.title(formatted), alert);
                        }
                    }
                }
                if (callback != null) callback.done(formatted, null);
            } catch (Exception e) {
                Store.prefs(app).edit()
                        .putString("last_http_status", "Sync error: " + e.getMessage())
                        .apply();
                if (callback != null) callback.done(null, e);
            }
        });
    }

    private static String sanitizeHeaderName(String raw) {
        String cleaned = raw.replaceAll("[^A-Za-z0-9\\-_]", "").trim();
        return cleaned.isEmpty() ? "X-API-Key" : cleaned;
    }
}
