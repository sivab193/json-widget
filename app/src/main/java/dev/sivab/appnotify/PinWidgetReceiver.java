package dev.sivab.appnotify;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PinWidgetReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        String source = intent.getStringExtra("source");
        if (id != AppWidgetManager.INVALID_APPWIDGET_ID && source != null) {
            Store.prefs(context).edit().putString("widget_source_" + id, source).apply();
            DataWidget.updateOne(context, AppWidgetManager.getInstance(context), id);
        }
    }
}
