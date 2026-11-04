package dev.sivab.appnotify;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class LiveUpdateService extends Service {
    static final String ACTION_STOP = "dev.sivab.appnotify.STOP_LIVE";
    static final String ACTION_REFRESH = "dev.sivab.appnotify.REFRESH_LIVE";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String lastDeliveredSummary = "";

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!Store.showNotification(LiveUpdateService.this)) {
                stopLive();
                return;
            }

            String source = Store.source(LiveUpdateService.this);
            // If monitoring custom HTTPS endpoint and background refresh is enabled, fetch with battery awareness
            if (Samples.ENDPOINT.equals(source) && Store.refreshMinutes(LiveUpdateService.this) > 0) {
                Fetcher.refresh(LiveUpdateService.this, false, (formatted, error) -> {
                    handler.post(() -> updateNotificationAndScheduleNext());
                });
            } else {
                updateNotificationAndScheduleNext();
            }
        }
    };

    private void updateNotificationAndScheduleNext() {
        if (!Store.showNotification(this)) {
            stopLive();
            return;
        }
        String formatted = Store.text(this);
        String message = ContentRenderer.notification(this, formatted);
        startForeground(1001, Notifier.liveNotification(this));

        // Only alert wearable when the rendered summary actually changed or if diff-only is disabled
        if (!message.equals(lastDeliveredSummary) || !Store.notifyOnlyDiff(this)) {
            lastDeliveredSummary = message;
            Notifier.showWearableAlert(this, ContentRenderer.title(formatted), message);
        }

        // Auto-stop live session once food delivery countdown expires
        if (Samples.FOOD.equals(Store.source(this))) {
            long remainingMs = Store.foodDeadline(this) - System.currentTimeMillis();
            if (remainingMs <= 0) {
                handler.postDelayed(this::stopLive, 5000L);
                return;
            }
            // System Chronometer renders second-by-second countdown natively without waking the app.
            // Schedule next check only when countdown finishes or after 5 minutes for wearable sync.
            long nextCheck = Math.min(remainingMs + 1000L, 5L * 60L * 1000L);
            handler.postDelayed(tick, nextCheck);
            return;
        }

        int refreshMins = Store.refreshMinutes(this);
        if (refreshMins > 0) {
            long intervalMs = Math.max(15L, refreshMins) * 60L * 1000L;
            handler.postDelayed(tick, intervalMs);
        }
        // When refreshMins == 0 (Manual only), no background timer is posted — zero idle CPU wakeups.
    }

    @Override public void onCreate() {
        super.onCreate();
        Notifier.ensureChannel(this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            Store.prefs(this).edit().putBoolean("notify_" + Store.source(this), false).apply();
            stopLive();
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_REFRESH.equals(intent.getAction())) {
            Store.setLiveActive(this, true);
            startForeground(1001, Notifier.liveNotification(this));
            Fetcher.refresh(this, true, (text, error) -> handler.post(this::updateNotificationAndScheduleNext));
            return START_STICKY;
        }

        handler.removeCallbacks(tick);
        Store.setLiveActive(this, true);
        startForeground(1001, Notifier.liveNotification(this));
        updateNotificationAndScheduleNext();
        return START_STICKY;
    }

    private void stopLive() {
        handler.removeCallbacks(tick);
        Store.setLiveActive(this, false);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override public void onDestroy() {
        handler.removeCallbacks(tick);
        Store.setLiveActive(this, false);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }
}
