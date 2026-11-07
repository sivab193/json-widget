package dev.sivab.peekjson;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText url;
    private EditText authHeader;
    private EditText authSecret;
    private TextView content;
    private TextView status;
    private TextView authDiagnostic;
    private TextView batteryStatusSummary;
    private TextView liveBadge;
    private TextView liveEligibility;
    private TextView authSecretLabel;
    private LinearLayout endpointContainer;
    private LinearLayout authHeaderContainer;
    private LinearLayout authSecretContainer;
    private Button save;
    private Button stopLive;
    private Spinner source;
    private Spinner authMode;
    private Spinner refreshInterval;
    private CheckBox revealSecret;
    private CheckBox batteryRespectPowerSave;
    private CheckBox batteryNotifyOnlyDiff;
    private CheckBox showWidget;
    private CheckBox showNotification;
    private boolean sendTestAfterPermission;
    private boolean applyAfterPermission;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        url = findViewById(R.id.url);
        authHeader = findViewById(R.id.authHeader);
        authSecret = findViewById(R.id.authSecret);
        content = findViewById(R.id.content);
        status = findViewById(R.id.status);
        authDiagnostic = findViewById(R.id.authDiagnostic);
        batteryStatusSummary = findViewById(R.id.batteryStatusSummary);
        liveBadge = findViewById(R.id.liveBadge);
        liveEligibility = findViewById(R.id.liveEligibility);
        authSecretLabel = findViewById(R.id.authSecretLabel);
        endpointContainer = findViewById(R.id.endpointContainer);
        authHeaderContainer = findViewById(R.id.authHeaderContainer);
        authSecretContainer = findViewById(R.id.authSecretContainer);
        save = findViewById(R.id.save);
        stopLive = findViewById(R.id.stopLive);
        source = findViewById(R.id.source);
        authMode = findViewById(R.id.authMode);
        refreshInterval = findViewById(R.id.refreshInterval);
        revealSecret = findViewById(R.id.revealSecret);
        batteryRespectPowerSave = findViewById(R.id.batteryRespectPowerSave);
        batteryNotifyOnlyDiff = findViewById(R.id.batteryNotifyOnlyDiff);
        showWidget = findViewById(R.id.showWidget);
        showNotification = findViewById(R.id.showNotification);

        // 1. Setup Feed Source Spinner
        ArrayAdapter<String> sourcesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Samples.LABELS);
        sourcesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        source.setAdapter(sourcesAdapter);
        source.setSelection(Samples.position(Store.source(this)));
        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSourceControls(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 2. Setup Authentication Mode Spinner
        ArrayAdapter<String> authAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Store.AUTH_LABELS);
        authAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authMode.setAdapter(authAdapter);
        authMode.setSelection(Store.authPosition(Store.authMode(this)));
        authMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAuthControls(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 3. Setup Refresh Frequency Spinner
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Store.INTERVAL_LABELS);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshInterval.setAdapter(intervalAdapter);
        refreshInterval.setSelection(Store.intervalPosition(Store.refreshMinutes(this)));
        refreshInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBatteryTelemetryPreview(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Populate initial values from Store
        url.setText(Store.url(this));
        authHeader.setText(Store.authHeader(this));
        authSecret.setText(Store.authSecret(this));
        batteryRespectPowerSave.setChecked(Store.respectBatterySaver(this));
        batteryNotifyOnlyDiff.setChecked(Store.notifyOnlyDiff(this));
        content.setText(Store.text(this));

        revealSecret.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = authSecret.getSelectionEnd();
            if (isChecked) {
                authSecret.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            } else {
                authSecret.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            if (pos >= 0) authSecret.setSelection(Math.min(pos, authSecret.getText().length()));
        });

        findViewById(R.id.clearAuth).setOnClickListener(v -> {
            Store.clearAuthCredentials(this);
            authMode.setSelection(0);
            authSecret.setText("");
            authHeader.setText("X-API-Key");
            refreshDiagnosticsUI();
            Toast.makeText(this, "Saved API key / token cleared from device", Toast.LENGTH_SHORT).show();
        });

        save.setOnClickListener(v -> saveAndRefresh());
        stopLive.setOnClickListener(v -> stopLiveSession());
        findViewById(R.id.addWidget).setOnClickListener(v -> addSelectedWidget());
        findViewById(R.id.testNotification).setOnClickListener(v -> sendTestNotification());
        findViewById(R.id.notificationSettings).setOnClickListener(v -> openNotificationSettings());
        findViewById(R.id.privacyInfoBtn).setOnClickListener(v -> showPrivacyAndPlayStoreDialog());

        Notifier.ensureChannel(this);
        Scheduler.schedule(this);
        refreshDiagnosticsUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDiagnosticsUI();
    }

    private void sendTestNotification() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            sendTestAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        } else {
            String stored = Store.text(this);
            Notifier.showWearableAlert(this, ContentRenderer.title(stored), ContentRenderer.notification(this, stored));
            Toast.makeText(this, "Sent test alert to Galaxy Fit / wearable channel", Toast.LENGTH_SHORT).show();
        }
    }

    private void openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= 36) {
            try {
                startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
                return;
            } catch (Exception ignored) { }
        }
        startActivity(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, Notifier.CHANNEL));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 10 && sendTestAfterPermission) {
            sendTestAfterPermission = false;
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                String stored = Store.text(this);
                Notifier.showWearableAlert(this, ContentRenderer.title(stored), ContentRenderer.notification(this, stored));
            }
        }
        if (requestCode == 10 && applyAfterPermission) {
            applyAfterPermission = false;
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) saveAndRefresh();
        }
    }

    private void saveAndRefresh() {
        if (showNotification.isChecked() && Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            applyAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
            return;
        }
        String selectedSource = Samples.IDS[source.getSelectedItemPosition()];
        String endpointUrl = url.getText().toString().trim();
        String selectedAuthMode = Store.AUTH_IDS[authMode.getSelectedItemPosition()];
        String headerName = authHeader.getText().toString().trim();
        String secretValue = authSecret.getText().toString().trim();
        int intervalMins = Store.INTERVAL_MINUTES[refreshInterval.getSelectedItemPosition()];

        if (Samples.ENDPOINT.equals(selectedSource)) {
            if (!endpointUrl.startsWith("https://")) {
                Toast.makeText(this, "Use a valid HTTPS URL (https://) for security", Toast.LENGTH_LONG).show();
                return;
            }
            if (!Store.AUTH_NONE.equals(selectedAuthMode) && secretValue.isEmpty()) {
                Toast.makeText(this, "Enter your Bearer Token or API Key, or switch Auth to None", Toast.LENGTH_LONG).show();
                return;
            }
            if (Store.AUTH_API_KEY.equals(selectedAuthMode) && headerName.isEmpty()) {
                headerName = "X-API-Key";
                authHeader.setText(headerName);
            }
        }

        // Invalidate ETag cache if URL or Auth changed
        boolean authOrUrlChanged = !endpointUrl.equals(Store.url(this))
                || !selectedAuthMode.equals(Store.authMode(this))
                || !secretValue.equals(Store.authSecret(this))
                || !headerName.equals(Store.authHeader(this));

        SharedPreferences.Editor editor = Store.prefs(this).edit()
                .putString("url", endpointUrl)
                .putString("source", selectedSource)
                .putString("auth_mode", selectedAuthMode)
                .putString("auth_header", headerName.isEmpty() ? "X-API-Key" : headerName)
                .putString("auth_secret", secretValue)
                .putInt("refresh_minutes", intervalMins)
                .putBoolean("respect_battery_saver", batteryRespectPowerSave.isChecked())
                .putBoolean("notify_only_diff", batteryNotifyOnlyDiff.isChecked())
                .putBoolean("widget_" + selectedSource, showWidget.isChecked())
                .putBoolean("notify_" + selectedSource, showNotification.isChecked());

        if (authOrUrlChanged) {
            editor.remove("etag").remove("last_modified").remove("etag_url");
        }
        if (Samples.FOOD.equals(selectedSource)) {
            editor.putLong("food_deadline", System.currentTimeMillis() + 18L * 60L * 1000L);
        }
        if (Samples.BUSES.equals(selectedSource)) {
            editor.putLong("bus_deadline", System.currentTimeMillis() + 8L * 60L * 1000L);
        }
        editor.apply();

        // Reschedule battery-aware background alarm with the updated interval
        Scheduler.schedule(this);

        save.setEnabled(false);
        status.setText("Syncing endpoint…");
        Fetcher.refresh(this, true, (text, error) -> runOnUiThread(() -> {
            save.setEnabled(true);
            if (error != null) {
                status.setText("Could not load endpoint · " + error.getMessage());
                content.setText(error.getMessage());
            } else {
                content.setText(text);
                Intent live = new Intent(this, LiveUpdateService.class);
                if (showNotification.isChecked() && Store.supportsPromotedLiveUpdate(selectedSource)) {
                    Store.setLiveActive(this, true);
                    startForegroundService(live);
                } else {
                    stopService(live);
                    if (showNotification.isChecked()) {
                        Notifier.show(this, ContentRenderer.notification(this, text), false);
                    } else {
                        Notifier.cancelLive(this);
                    }
                }
            }
            refreshDiagnosticsUI();
        }));
    }

    private void stopLiveSession() {
        String selectedSource = Samples.IDS[source.getSelectedItemPosition()];
        showNotification.setChecked(false);
        Store.prefs(this).edit().putBoolean("notify_" + selectedSource, false).apply();
        Intent live = new Intent(this, LiveUpdateService.class);
        stopService(live);
        Notifier.cancelLive(this);
        refreshDiagnosticsUI();
        Toast.makeText(this, "Live foreground session stopped", Toast.LENGTH_SHORT).show();
    }

    private void addSelectedWidget() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        if (!manager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, "Your launcher does not support pinning widgets from an app. Add PeekJson from the Home-screen widget picker.", Toast.LENGTH_LONG).show();
            return;
        }
        String selected = Samples.IDS[source.getSelectedItemPosition()];
        Intent callbackIntent = new Intent(this, PinWidgetReceiver.class).putExtra("source", selected);
        PendingIntent callback = PendingIntent.getBroadcast(
                this,
                700 + source.getSelectedItemPosition(),
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        manager.requestPinAppWidget(new ComponentName(this, DataWidget.class), null, callback);
    }

    private void updateSourceControls(int position) {
        String selected = Samples.IDS[position];
        boolean isCustomEndpoint = Samples.ENDPOINT.equals(selected);
        endpointContainer.setVisibility(isCustomEndpoint ? View.VISIBLE : View.GONE);

        showWidget.setChecked(Store.prefs(this).getBoolean("widget_" + selected, true));
        showNotification.setEnabled(true);
        showNotification.setChecked(Store.prefs(this).getBoolean("notify_" + selected, true));
        showNotification.setText(Store.supportsPromotedLiveUpdate(selected)
                ? "Track as a Live Update on lock screen and supported system surfaces"
                : "Send a standard lock-screen and wearable notification");
        refreshDiagnosticsUI();
    }

    private void updateAuthControls(int authPos) {
        String mode = Store.AUTH_IDS[authPos];
        if (Store.AUTH_NONE.equals(mode)) {
            authHeaderContainer.setVisibility(View.GONE);
            authSecretContainer.setVisibility(View.GONE);
        } else if (Store.AUTH_BEARER.equals(mode)) {
            authHeaderContainer.setVisibility(View.GONE);
            authSecretContainer.setVisibility(View.VISIBLE);
            authSecretLabel.setText("Bearer Token (sent as Authorization: Bearer <token>)");
        } else {
            authHeaderContainer.setVisibility(View.VISIBLE);
            authSecretContainer.setVisibility(View.VISIBLE);
            authSecretLabel.setText("API Key Value");
        }
    }

    private void updateBatteryTelemetryPreview(int intervalPosition) {
        int mins = Store.INTERVAL_MINUTES[intervalPosition];
        PowerManager pm = getSystemService(PowerManager.class);
        boolean powerSaveOn = pm != null && pm.isPowerSaveMode();
        String saverState = powerSaveOn ? " · Battery Saver currently ON" : "";
        if (mins <= 0) {
            batteryStatusSummary.setText("Zero background wakeups: API only called when you tap Refresh or tap a widget" + saverState + ".");
        } else {
            batteryStatusSummary.setText("Inexact non-wakeup alarm every " + mins + " min + HTTP ETag / 304 conditional caching" + saverState + ".");
        }
    }

    private void refreshDiagnosticsUI() {
        boolean isLive = Store.liveActive(this) && Store.showNotification(this);
        liveBadge.setText(isLive ? "LIVE SESSION" : "STANDBY");
        stopLive.setEnabled(isLive);
        stopLive.setAlpha(isLive ? 1.0f : 0.5f);

        status.setText(Store.lastHttpStatus(this) + " · Updated: " + Store.updated(this));
        authDiagnostic.setText(Store.authSummary(this) + " · Sync: " + Store.scheduleSummary(this));
        liveEligibility.setText(Notifier.promotionStatus(this));
        updateBatteryTelemetryPreview(refreshInterval.getSelectedItemPosition());
    }

    private void showPrivacyAndPlayStoreDialog() {
        String message = "DATA SAFETY & PRIVACY POLICY\n\n"
                + "1. On-Device Credential Storage:\n"
                + "Bearer tokens and API keys are stored strictly in private app sandbox storage (MODE_PRIVATE) with Android cloud backup disabled (allowBackup=false). Credentials are never logged or shared with third parties.\n\n"
                + "2. Strict HTTPS Transport:\n"
                + "Cleartext HTTP traffic is blocked at the OS manifest level. Custom endpoints and headers only transmit over TLS/HTTPS directly to the URL you configure.\n\n"
                + "3. Battery & Foreground Service Policy:\n"
                + "• Background sync uses non-wakeup inexact alarms (ELAPSED_REALTIME) so your device stays in deep Doze sleep.\n"
                + "• Conditional HTTP requests (If-None-Match / ETag & If-Modified-Since) avoid re-downloading unchanged JSON.\n"
                + "• Background refreshes automatically pause when Android Battery Saver or Data Saver is active.\n"
                + "• You can stop the live foreground notification at any time using 'Stop Live' in the app or directly from the notification shade.";

        new AlertDialog.Builder(this)
                .setTitle("Privacy, Security & Battery Architecture")
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }
}
