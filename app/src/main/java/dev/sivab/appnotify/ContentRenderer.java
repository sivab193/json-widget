package dev.sivab.appnotify;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

final class ContentRenderer {
    static String title(String formatted) {
        try {
            JSONObject root = new JSONObject(formatted);
            switch (root.optString("type")) {
                case "buses": return "ROUTE " + root.optString("route");
                case "food_delivery": return "FOOD DELIVERY";
                case "events": return "EVENTS";
                case "family_locations": return "FAMILY LOCATIONS";
            }
        } catch (Exception ignored) { }
        return "PUBLIC ENDPOINT";
    }

    static String compact(String formatted) {
        try {
            JSONObject root = new JSONObject(formatted); String type = root.optString("type");
            if ("buses".equals(type)) {
                JSONObject bus = root.getJSONArray("arrivals").getJSONObject(0);
                return bus.optInt("minutes") + " min\n" + bus.optString("destination");
            }
            if ("food_delivery".equals(type)) return root.optInt("minutes_left") + " min\n" + root.optString("status");
            if ("events".equals(type)) {
                JSONObject event = root.getJSONArray("events").getJSONObject(0);
                return event.optString("time") + "\n" + event.optString("title");
            }
            if ("family_locations".equals(type)) {
                JSONObject person = root.getJSONArray("people").getJSONObject(0);
                return person.optString("name") + "\n" + person.optString("location");
            }
        } catch (Exception ignored) { }
        return JsonFormatter.summary(formatted);
    }
    static String widget(String formatted) {
        try {
            JSONObject root = new JSONObject(formatted);
            String type = root.optString("type");
            if ("buses".equals(type)) {
                StringBuilder out = new StringBuilder("Route ").append(root.optString("route")).append(" · ").append(root.optString("stop"));
                JSONArray items = root.getJSONArray("arrivals");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject bus = items.getJSONObject(i);
                    out.append("\n").append(i == 0 ? "● " : "○ ").append(bus.optInt("minutes")).append(" min · ").append(bus.optString("destination"));
                }
                return out.toString();
            }
            if ("food_delivery".equals(type))
                return root.optString("restaurant") + "\n" + root.optInt("minutes_left") + " min away · " + root.optString("status") + "\nCourier: " + root.optString("courier");
            if ("events".equals(type)) {
                StringBuilder out = new StringBuilder("Today"); JSONArray items = root.getJSONArray("events");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject event = items.getJSONObject(i);
                    out.append("\n").append(event.optString("time")).append(" · ").append(event.optString("title"));
                }
                return out.toString();
            }
            if ("family_locations".equals(type)) {
                StringBuilder out = new StringBuilder("Family locations"); JSONArray items = root.getJSONArray("people");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject person = items.getJSONObject(i);
                    out.append("\n").append(person.optString("name")).append(" · ").append(person.optString("location")).append(" · ").append(person.optString("updated"));
                }
                return out.toString();
            }
        } catch (Exception ignored) { }
        return JsonFormatter.summary(formatted);
    }

    static String notification(String formatted) {
        try {
            JSONObject root = new JSONObject(formatted); String type = root.optString("type");
            if ("buses".equals(type)) {
                JSONObject first = root.getJSONArray("arrivals").getJSONObject(0);
                return first.optInt("minutes") + " min · Route " + root.optString("route") + " to " + first.optString("destination") + " · " + first.optString("status");
            }
            if ("food_delivery".equals(type)) return root.optInt("minutes_left") + " min away · " + root.optString("restaurant") + " · " + root.optString("status");
            if ("events".equals(type)) {
                JSONObject first = root.getJSONArray("events").getJSONObject(0);
                return first.optString("time") + " · " + first.optString("title") + " · " + first.optString("location");
            }
        } catch (Exception ignored) { }
        return JsonFormatter.summary(formatted);
    }

    static String notification(Context context, String formatted) {
        try {
            JSONObject root = new JSONObject(formatted);
            if ("buses".equals(root.optString("type")) && Store.busDeadline(context) > 0) {
                JSONObject first = root.getJSONArray("arrivals").getJSONObject(0);
                long seconds = Math.max(0, (Store.busDeadline(context) - System.currentTimeMillis() + 999) / 1000);
                String eta = seconds > 0 ? ((seconds + 59) / 60) + " min" : "Arriving now";
                String vehicle = first.optString("vehicle");
                return eta + " · Route " + root.optString("route") + " to " + first.optString("destination")
                        + (vehicle.isEmpty() ? "" : " · " + vehicle);
            }
            if ("food_delivery".equals(root.optString("type")) && Store.foodDeadline(context) > 0) {
                long seconds = Math.max(0, (Store.foodDeadline(context) - System.currentTimeMillis() + 999) / 1000);
                return (seconds / 60) + " min " + (seconds % 60) + " sec · " + root.optString("restaurant") + " · " + (seconds > 0 ? root.optString("status") : "Arrived");
            }
        } catch (Exception ignored) { }
        return notification(formatted);
    }
}
