package dev.sivab.peekjson;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

final class Notifier {
    static final String CHANNEL = "live_updates_v2";
    private static final String WEARABLE_CHANNEL = "wearable_updates_v3";

    static void ensureChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Live endpoint status", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Battery-friendly persistent live status on notification shade and lock screen");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.enableVibration(false);
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);

        NotificationChannel wearable = new NotificationChannel(WEARABLE_CHANNEL, "Galaxy Fit updates", NotificationManager.IMPORTANCE_DEFAULT);
        wearable.setDescription("Short-lived JSON updates intended for wearable mirroring when data changes");
        wearable.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        wearable.enableVibration(true);
        manager.createNotificationChannel(wearable);
    }

    private static Notification.Action buildStopAction(Context context) {
        Intent stopIntent = new Intent(context, LiveUpdateService.class).setAction(LiveUpdateService.ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(
                context,
                901,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new Notification.Action.Builder(null, "Stop Live", stopPending).build();
    }

    private static Notification.Action buildRefreshAction(Context context) {
        Intent refreshIntent = new Intent(context, LiveUpdateService.class).setAction(LiveUpdateService.ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getService(
                context,
                902,
                refreshIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new Notification.Action.Builder(null, "Refresh Now", refreshPending).build();
    }

    static void show(Context context, String text, boolean test) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ensureChannel(context);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        PendingIntent open = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        String title = test ? "PeekJson · Test Alert" : ContentRenderer.title(Store.text(context));
        boolean liveEligible = !test && Store.supportsPromotedLiveUpdate(Store.source(context));

        Notification publicVersion = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(title)
                .setContentText(text)
                .build();

        Notification.Builder builder = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(test || !liveEligible)
                .setOngoing(liveEligible)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPublicVersion(publicVersion)
                .setCategory(Notification.CATEGORY_STATUS);

        if (liveEligible) {
            builder.addAction(buildRefreshAction(context));
            builder.addAction(buildStopAction(context));
        }

        if (!test && Samples.FOOD.equals(Store.source(context)) && Store.foodDeadline(context) > 0) {
            builder.setWhen(Store.foodDeadline(context))
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true);
        }
        if (liveEligible && Build.VERSION.SDK_INT >= 36) {
            applyPromotedOngoing(builder);
        }
        manager.notify(test ? 1002 : 1001, builder.build());
    }

    static Notification liveNotification(Context context) {
        String formatted = Store.text(context);
        String text = ContentRenderer.notification(context, formatted);
        PendingIntent open = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification.Builder builder = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(ContentRenderer.title(formatted))
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_STATUS)
                .addAction(buildRefreshAction(context))
                .addAction(buildStopAction(context));

        if (Samples.FOOD.equals(Store.source(context)) && Store.foodDeadline(context) > 0) {
            builder.setWhen(Store.foodDeadline(context))
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true);
        }
        if (Build.VERSION.SDK_INT >= 36 && Store.supportsPromotedLiveUpdate(Store.source(context))) {
            applyPromotedOngoing(builder);
        }
        return builder.build();
    }

    private static void applyPromotedOngoing(Notification.Builder builder) {
        android.os.Bundle extras = new android.os.Bundle();
        extras.putBoolean("android.requestPromotedOngoing", true);
        builder.addExtras(extras);
        try {
            Notification.Builder.class.getMethod("setRequestPromotedOngoing", boolean.class).invoke(builder, true);
        } catch (Exception ignored) { }
        try {
            Notification.Builder.class.getMethod("setShortCriticalText", CharSequence.class).invoke(builder, "Live");
        } catch (Exception ignored) { }
    }

    static void cancelLive(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(1001);
            int wearableId = Store.wearableNotificationId(context);
            if (wearableId != 0) manager.cancel("wearable", wearableId);
        }
        Store.setLiveActive(context, false);
    }

    static String promotionStatus(Context context) {
        if (!Store.supportsPromotedLiveUpdate(Store.source(context))) {
            return "Standard notification · Live promotion is reserved for active bus and delivery tracking";
        }
        if (Build.VERSION.SDK_INT < 36) {
            return "Standard lock-screen notification · Promoted Live Updates require Android 16+";
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        return manager != null && manager.canPostPromotedNotifications()
                ? "Promoted Live Updates allowed by Android · Samsung Now Bar remains device-controlled"
                : "Live Updates are disabled in system notification settings";
    }

    static void showWearableAlert(Context context, String title, String text) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ensureChannel(context);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        PendingIntent open = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification notification = new Notification.Builder(context, WEARABLE_CHANNEL)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(false)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setCategory(Notification.CATEGORY_EVENT)
                .setTimeoutAfter(90000L)
                .build();

        int previousId = Store.wearableNotificationId(context);
        if (previousId != 0) manager.cancel("wearable", previousId);
        manager.cancel(1003);
        int minuteId = 2000 + (int) ((System.currentTimeMillis() / 60000L) % 100000L);
        manager.notify("wearable", minuteId, notification);
        Store.setWearableNotificationId(context, minuteId);
    }
}
