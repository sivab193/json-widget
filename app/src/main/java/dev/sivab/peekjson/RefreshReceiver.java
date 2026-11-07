package dev.sivab.peekjson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        // Pass userInitiated = false so background alarms respect Battery Saver, Data Saver, and ETag caching
        Fetcher.refresh(context, false, (text, error) -> pending.finish());
    }
}
