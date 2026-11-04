package dev.sivab.appnotify;

final class Samples {
    static final String ENDPOINT = "endpoint";
    static final String BUSES = "buses";
    static final String FOOD = "food";
    static final String EVENTS = "events";
    static final String FAMILY = "family";

    static final String[] LABELS = {
            "Custom HTTPS JSON Endpoint (Public or Authenticated)",
            "Route 21 · Upcoming Transit Buses",
            "Food Delivery · Live Chronometer Countdown",
            "Upcoming Events & Meetings",
            "Family Locations · Widget-Only Table"
    };
    static final String[] IDS = { ENDPOINT, BUSES, FOOD, EVENTS, FAMILY };

    static int position(String id) {
        for (int i = 0; i < IDS.length; i++) if (IDS[i].equals(id)) return i;
        return 0;
    }

    static String json(String id) {
        switch (id) {
            case BUSES: return "{\"type\":\"buses\",\"route\":\"21\",\"stop\":\"Central Station\",\"arrivals\":[" +
                    "{\"destination\":\"Downtown\",\"minutes\":3,\"status\":\"On time\"}," +
                    "{\"destination\":\"Downtown\",\"minutes\":12,\"status\":\"2 min late\"}," +
                    "{\"destination\":\"Downtown\",\"minutes\":24,\"status\":\"On time\"}]}";
            case FOOD: return "{\"type\":\"food_delivery\",\"restaurant\":\"Green Bowl Kitchen\",\"courier\":\"Maya\",\"minutes_left\":18,\"status\":\"On the way\"}";
            case EVENTS: return "{\"type\":\"events\",\"events\":[" +
                    "{\"title\":\"Product stand-up\",\"time\":\"9:30 AM\",\"location\":\"Zoom\"}," +
                    "{\"title\":\"Dentist appointment\",\"time\":\"1:15 PM\",\"location\":\"Carmel\"}," +
                    "{\"title\":\"Dinner with family\",\"time\":\"6:30 PM\",\"location\":\"Home\"}]}";
            case FAMILY: return "{\"type\":\"family_locations\",\"people\":[" +
                    "{\"name\":\"Appa\",\"location\":\"Hosur\",\"updated\":\"Just now\"}," +
                    "{\"name\":\"Amma\",\"location\":\"Madurai\",\"updated\":\"2 min ago\"}," +
                    "{\"name\":\"Shuk\",\"location\":\"Coimbatore\",\"updated\":\"5 min ago\"}]}";
            default: return null;
        }
    }
}
