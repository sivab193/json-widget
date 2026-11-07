package dev.sivab.peekjson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

final class Scheduler {
    static void schedule(Context context) {
        AlarmManager alarms = context.getSystemService(AlarmManager.class);
        if (alarms == null) return;
        PendingIntent pending = PendingIntent.getBroadcast(
                context,
                42,
                new Intent(context, RefreshReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        int minutes = Store.refreshMinutes(context);
        if (minutes <= 0) {
            // Manual-only mode: cancel any periodic background alarms to maximize battery life
            alarms.cancel(pending);
            return;
        }
        long intervalMs = Math.max(15L, minutes) * 60L * 1000L;
        // Use ELAPSED_REALTIME (non-wakeup) with setInexactRepeating so Android batches network
        // refreshes into natural device maintenance windows without waking the CPU from deep sleep/Doze.
        alarms.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + intervalMs,
                intervalMs,
                pending
        );
    }
}
