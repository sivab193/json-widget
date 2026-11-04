package dev.sivab.appnotify;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class WidgetConfigActivity extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);

        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        Spinner source = findViewById(R.id.widgetSource);
        TextView summary = findViewById(R.id.widgetConfigSummary);
        if (summary != null) {
            summary.setText(Store.authSummary(this) + " · Background sync: " + Store.scheduleSummary(this));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Samples.LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        source.setAdapter(adapter);
        String saved = Store.prefs(this).getString("widget_source_" + widgetId, Store.source(this));
        source.setSelection(Samples.position(saved));

        findViewById(R.id.saveWidget).setOnClickListener(v -> {
            Store.prefs(this).edit().putString("widget_source_" + widgetId, Samples.IDS[source.getSelectedItemPosition()]).apply();
            DataWidget.updateOne(this, AppWidgetManager.getInstance(this), widgetId);
            Intent result = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
