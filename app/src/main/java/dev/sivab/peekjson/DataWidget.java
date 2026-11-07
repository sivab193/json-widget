package dev.sivab.peekjson;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;

public class DataWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        String now = now();
        for (int id : ids) { Store.setWidgetUpdated(context, id, now); updateOne(context, manager, id); }
        if (Store.refreshMinutes(context) > 0) {
            Fetcher.refresh(context, false, null);
        }
    }
    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int appWidgetId, Bundle newOptions) {
        updateOne(context, manager, appWidgetId);
    }
    @Override public void onDeleted(Context context, int[] appWidgetIds) {
        android.content.SharedPreferences.Editor editor = Store.prefs(context).edit();
        for (int id : appWidgetIds) editor.remove("widget_source_" + id).remove("widget_updated_" + id);
        editor.apply();
    }
    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("dev.sivab.peekjson.REFRESH".equals(intent.getAction())) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            String mapped = id == AppWidgetManager.INVALID_APPWIDGET_ID ? Store.source(context) : Store.prefs(context).getString("widget_source_" + id, Store.source(context));
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) Store.setWidgetUpdated(context, id, now());
            if (Samples.ENDPOINT.equals(mapped)) {
                Fetcher.refresh(context, true, (text, error) -> {
                    if (id != AppWidgetManager.INVALID_APPWIDGET_ID) updateOne(context, AppWidgetManager.getInstance(context), id);
                });
            } else if (id != AppWidgetManager.INVALID_APPWIDGET_ID) updateOne(context, AppWidgetManager.getInstance(context), id);
        }
    }
    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, DataWidget.class));
        for (int id : ids) { Store.setWidgetUpdated(context, id, Store.updated(context)); updateOne(context, manager, id); }
    }
    static void updateOne(Context context, AppWidgetManager manager, int id) {
        Bundle options = manager.getAppWidgetOptions(id);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250);
        int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 150);
        boolean compact = width < 110 || height < 80;
        String mappedSource = Store.prefs(context).getString("widget_source_" + id, Store.source(context));
        String stored = Store.text(context);
        String sample = Samples.json(mappedSource);
        if (sample != null) {
            try { stored = JsonFormatter.format(sample); } catch (Exception ignored) { }
        }
        String body = Store.showWidget(context, mappedSource)
                ? (compact ? ContentRenderer.compact(stored) : ContentRenderer.widget(stored))
                : "Hidden\nOpen the app to change visibility";
        boolean visible = Store.showWidget(context, mappedSource);
        boolean deliveryLayout = visible && !compact && Samples.FOOD.equals(mappedSource) && Store.foodDeadline(context) > 0;
        boolean rowsLayout = visible && !compact && (Samples.BUSES.equals(mappedSource) || Samples.EVENTS.equals(mappedSource));
        int layout = deliveryLayout ? R.layout.widget_delivery : (rowsLayout ? R.layout.widget_rows : R.layout.widget);
        RemoteViews views = new RemoteViews(context.getPackageName(), layout);
        views.setTextViewText(R.id.widgetTitle, ContentRenderer.title(stored));
        if (deliveryLayout) {
            try {
                JSONObject food = new JSONObject(stored);
                views.setTextViewText(R.id.deliveryRestaurant, food.optString("restaurant"));
                views.setTextViewText(R.id.deliveryMeta, food.optString("status") + "  ·  Courier " + food.optString("courier"));
            } catch (Exception ignored) { }
            long base = android.os.SystemClock.elapsedRealtime() + (Store.foodDeadline(context) - System.currentTimeMillis());
            views.setChronometer(R.id.widgetCountdown, base, "%s remaining", true);
            views.setChronometerCountDown(R.id.widgetCountdown, true);
        } else if (rowsLayout) {
            populateRows(views, mappedSource, stored, height);
        } else {
            views.setTextViewText(R.id.widgetText, body);
            views.setTextViewTextSize(R.id.widgetText, TypedValue.COMPLEX_UNIT_SP, compact ? 16 : (height > 220 ? 15 : 14));
            views.setInt(R.id.widgetText, "setMaxLines", compact ? 2 : (height > 220 ? 10 : 6));
            views.setViewVisibility(R.id.widgetCountdown, View.GONE);
        }
        views.setViewVisibility(R.id.widgetUpdated, compact ? View.GONE : View.VISIBLE);
        views.setTextViewText(R.id.widgetUpdated, "Updated " + Store.widgetUpdated(context, id) + "  ·  Tap to refresh");
        Intent refresh = new Intent(context, DataWidget.class).setAction("dev.sivab.peekjson.REFRESH")
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        PendingIntent pending = PendingIntent.getBroadcast(context, id, refresh, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetRoot, pending);
        manager.updateAppWidget(id, views);
    }

    private static void populateRows(RemoteViews views, String source, String formatted, int height) {
        try {
            JSONObject root = new JSONObject(formatted); JSONArray items;
            if (Samples.BUSES.equals(source)) {
                views.setTextViewText(R.id.headLeft, "ETA"); views.setTextViewText(R.id.headMain, "DESTINATION"); views.setTextViewText(R.id.headRight, "STATUS");
                items = root.getJSONArray("arrivals");
                for (int i = 0; i < Math.min(3, items.length()); i++) {
                    JSONObject item = items.getJSONObject(i);
                    setRow(views, i + 1, item.optInt("minutes") + " min", item.optString("destination"), item.optString("status"));
                }
            } else {
                views.setTextViewText(R.id.headLeft, "TIME"); views.setTextViewText(R.id.headMain, "EVENT"); views.setTextViewText(R.id.headRight, "PLACE");
                items = root.getJSONArray("events");
                for (int i = 0; i < Math.min(3, items.length()); i++) {
                    JSONObject item = items.getJSONObject(i);
                    setRow(views, i + 1, item.optString("time"), item.optString("title"), item.optString("location"));
                }
            }
            views.setViewVisibility(R.id.row3, View.VISIBLE);
        } catch (Exception ignored) { }
    }

    private static void setRow(RemoteViews views, int row, String left, String main, String right) {
        int leftId = row == 1 ? R.id.left1 : row == 2 ? R.id.left2 : R.id.left3;
        int mainId = row == 1 ? R.id.main1 : row == 2 ? R.id.main2 : R.id.main3;
        int rightId = row == 1 ? R.id.right1 : row == 2 ? R.id.right2 : R.id.right3;
        views.setTextViewText(leftId, left); views.setTextViewText(mainId, main); views.setTextViewText(rightId, right);
    }
    private static String now() { return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date()); }
}
