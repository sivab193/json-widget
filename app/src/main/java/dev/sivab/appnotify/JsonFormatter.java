package dev.sivab.appnotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class JsonFormatter {
    static String format(String raw) throws JSONException {
        String value = raw.trim();
        if (value.startsWith("{")) return new JSONObject(value).toString(2);
        if (value.startsWith("[")) return new JSONArray(value).toString(2);
        throw new JSONException("The response is not a JSON object or array");
    }

    static String summary(String formatted) {
        String oneLine = formatted.replaceAll("[{}\\[\\]\" ]", " ").replaceAll("\\s+", " ").trim();
        return oneLine.length() > 180 ? oneLine.substring(0, 177) + "…" : oneLine;
    }
}
