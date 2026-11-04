import http.server
import json
import os
import urllib.request
import urllib.error
from urllib.parse import urlparse

PORT = 3000
APK_PATH = "/app/applet/app/build/outputs/apk/debug/app-debug.apk"

HTML_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>App Notify — Android Preview & APK</title>
  <style>
    :root {
      --bg-page: #F8FAFC;
      --surface-card: #FFFFFF;
      --surface-subtle: #F1F5F9;
      --border-subtle: #E2E8F0;
      --navy: #0F172A;
      --blue: #2563EB;
      --pale-blue: #EFF6FF;
      --muted: #64748B;
      --success: #059669;
      --danger: #DC2626;
    }
    @media (prefers-color-scheme: dark) {
      :root {
        --bg-page: #090D16;
        --surface-card: #111827;
        --surface-subtle: #1E293B;
        --border-subtle: #334155;
        --navy: #F8FAFC;
        --blue: #60A5FA;
        --pale-blue: #172033;
        --muted: #94A3B8;
        --success: #34D399;
        --danger: #F87171;
      }
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      background: var(--bg-page);
      color: var(--navy);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      padding: 20px 16px 48px;
    }
    .shell {
      max-width: 960px;
      margin: 0 auto;
      display: grid;
      grid-template-columns: 1fr;
      gap: 20px;
    }
    @media (min-width: 820px) {
      .shell {
        grid-template-columns: 440px 1fr;
        align-items: start;
      }
    }
    .phone-frame {
      background: var(--bg-page);
      border: 2px solid var(--border-subtle);
      border-radius: 28px;
      padding: 18px;
      box-shadow: 0 12px 32px rgba(0,0,0,0.12);
    }
    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
    }
    h1 {
      margin: 0;
      font-size: 26px;
      font-weight: 800;
      letter-spacing: -0.02em;
    }
    .subtitle {
      margin: 3px 0 0;
      font-size: 13px;
      color: var(--muted);
    }
    .badge {
      background: var(--pale-blue);
      color: var(--blue);
      border: 1px solid var(--border-subtle);
      border-radius: 999px;
      padding: 5px 11px;
      font-size: 11px;
      font-weight: 700;
      white-space: nowrap;
    }
    .card {
      background: var(--surface-card);
      border: 1px solid var(--border-subtle);
      border-radius: 18px;
      padding: 16px 18px;
      margin-top: 14px;
    }
    .section-label {
      color: var(--blue);
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      margin-bottom: 10px;
    }
    label.field-label {
      display: block;
      color: var(--muted);
      font-size: 12px;
      font-weight: 700;
      margin-top: 12px;
      margin-bottom: 6px;
    }
    select, input[type="text"], input[type="password"], input[type="url"] {
      width: 100%;
      height: 44px;
      background: var(--surface-subtle);
      color: var(--navy);
      border: 1px solid var(--border-subtle);
      border-radius: 12px;
      padding: 0 12px;
      font-size: 13.5px;
      outline: none;
    }
    .check-row {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 10px;
      font-size: 13px;
      cursor: pointer;
    }
    .check-row input {
      width: 17px;
      height: 17px;
      accent-color: var(--blue);
    }
    .btn-row {
      display: flex;
      gap: 10px;
      margin-top: 12px;
    }
    button, .apk-btn {
      height: 44px;
      border-radius: 12px;
      font-size: 13.5px;
      font-weight: 700;
      cursor: pointer;
      border: 1px solid transparent;
      padding: 0 14px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-decoration: none;
    }
    .btn-primary {
      background: #2563EB;
      color: #FFFFFF;
      flex: 1;
    }
    .btn-secondary {
      background: var(--surface-subtle);
      color: var(--navy);
      border-color: var(--border-subtle);
    }
    .btn-danger {
      background: var(--surface-subtle);
      color: var(--danger);
      border-color: var(--border-subtle);
    }
    .w-full { width: 100%; margin-top: 8px; }
    .panel {
      background: var(--pale-blue);
      border: 1px solid var(--border-subtle);
      border-radius: 16px;
      padding: 14px;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 12.5px;
      white-space: pre-wrap;
      word-break: break-word;
      margin-top: 10px;
      max-height: 260px;
      overflow-y: auto;
    }
    .meta-text {
      font-size: 12px;
      color: var(--muted);
      margin-top: 6px;
      line-height: 1.45;
    }
    .widget-preview {
      background: var(--pale-blue);
      border: 1px solid var(--border-subtle);
      border-radius: 20px;
      padding: 16px;
      margin-top: 10px;
    }
    .widget-title {
      color: var(--blue);
      font-size: 11px;
      font-weight: 800;
      letter-spacing: 0.06em;
    }
    .widget-body {
      margin-top: 8px;
      font-size: 14px;
      line-height: 1.5;
      white-space: pre-line;
      font-weight: 500;
    }
    .widget-footer {
      margin-top: 10px;
      font-size: 11px;
      color: var(--muted);
    }
    .modal-backdrop {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.6);
      align-items: center;
      justify-content: center;
      padding: 16px;
      z-index: 50;
    }
    .modal {
      background: var(--surface-card);
      border: 1px solid var(--border-subtle);
      border-radius: 18px;
      max-width: 520px;
      width: 100%;
      padding: 22px;
    }
  </style>
</head>
<body>
  <div class="shell">
    <!-- Left Column: Interactive Native Android MainActivity Preview -->
    <div class="phone-frame">
      <div class="header-row">
        <div>
          <h1>App Notify</h1>
          <div class="subtitle">Authenticated JSON feeds for widgets, lock screen &amp; wearables</div>
        </div>
        <span id="liveBadge" class="badge">STANDBY</span>
      </div>

      <!-- CARD 1: Data Source & Authentication -->
      <div class="card">
        <div class="section-label">1 · DATA SOURCE &amp; AUTHENTICATION</div>
        <label class="field-label" for="source">Selected Feed</label>
        <select id="source" aria-label="Select JSON data source">
          <option value="endpoint">Custom HTTPS JSON Endpoint (Public or Authenticated)</option>
          <option value="buses">Route 21 · Upcoming Transit Buses</option>
          <option value="food">Food Delivery · Live Chronometer Countdown</option>
          <option value="events">Upcoming Events &amp; Meetings</option>
          <option value="family">Family Locations · Widget-Only Table</option>
        </select>

        <div id="endpointContainer">
          <label class="field-label" for="url">HTTPS JSON Endpoint URL</label>
          <input id="url" type="url" value="https://jsonplaceholder.typicode.com/todos/1" placeholder="https://api.example.com/v1/status" />

          <label class="field-label" for="authMode">Endpoint Authentication</label>
          <select id="authMode" aria-label="Select endpoint authentication method">
            <option value="none">None (Public HTTPS endpoint)</option>
            <option value="bearer">Bearer Token (Authorization: Bearer …)</option>
            <option value="api_key">API Key in Header (X-API-Key / Custom)</option>
          </select>

          <div id="authHeaderContainer" style="display:none;">
            <label class="field-label" for="authHeader">Header Name</label>
            <input id="authHeader" type="text" value="X-API-Key" placeholder="X-API-Key" />
          </div>

          <div id="authSecretContainer" style="display:none;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px;">
              <label id="authSecretLabel" class="field-label" for="authSecret" style="margin:0;">Bearer Token / API Key</label>
              <button type="button" id="clearAuth" style="height:26px;padding:0 8px;font-size:11px;" class="btn-danger">Clear token</button>
            </div>
            <input id="authSecret" type="password" style="margin-top:6px;font-family:monospace;" placeholder="Paste token or key (transmitted over HTTPS only)" />
            <label class="check-row">
              <input type="checkbox" id="revealSecret" />
              <span>Show API key / token characters</span>
            </label>
          </div>
        </div>
      </div>

      <!-- CARD 2: Background Sync Frequency & Battery Optimization -->
      <div class="card">
        <div class="section-label">2 · REFRESH FREQUENCY &amp; BATTERY SAVER</div>
        <label class="field-label" for="refreshInterval">Background API Call Interval</label>
        <select id="refreshInterval" aria-label="Select background refresh interval">
          <option value="0">Manual only · Best battery (no background polling)</option>
          <option value="15">Every 15 minutes (inexact batch)</option>
          <option value="30" selected>Every 30 minutes · Recommended</option>
          <option value="60">Every 1 hour</option>
          <option value="180">Every 3 hours</option>
          <option value="360">Every 6 hours</option>
        </select>

        <label class="check-row">
          <input type="checkbox" id="batteryRespectPowerSave" checked />
          <span>Pause background sync during Battery Saver or Data Saver</span>
        </label>
        <label class="check-row">
          <input type="checkbox" id="batteryNotifyOnlyDiff" checked />
          <span>Alert wearables &amp; shade only when JSON payload changes</span>
        </label>
        <div id="batteryStatusSummary" class="meta-text">
          Inexact non-wakeup alarm every 30 min + HTTP ETag / 304 conditional caching.
        </div>
      </div>

      <!-- CARD 3: Surfaces & Live Controls -->
      <div class="card">
        <div class="section-label">3 · SURFACES &amp; LIVE CONTROLS</div>
        <label class="check-row">
          <input type="checkbox" id="showWidget" checked />
          <span>Display feed on Home &amp; Lock-screen widgets</span>
        </label>
        <label class="check-row">
          <input type="checkbox" id="showNotification" checked />
          <span>Display Live Update on Lock screen &amp; Galaxy Fit</span>
        </label>

        <div class="btn-row">
          <button type="button" id="save" class="btn-primary">Apply &amp; Sync Live</button>
          <button type="button" id="stopLive" class="btn-danger" disabled style="opacity:0.5;">Stop Live</button>
        </div>
        <button type="button" id="testNotification" class="btn-secondary w-full">Test Wearable Alert</button>
      </div>
    </div>

    <!-- Right Column: Live Widget Surface, Lock-Screen Notification, Payload & APK Download -->
    <div>
      <div class="card" style="margin-top:0;">
        <div class="header-row">
          <div>
            <div class="section-label" style="margin-bottom:4px;">NATIVE ANDROID APK BUILD</div>
            <div style="font-weight:700;font-size:15px;">dev.sivab.appnotify · v1.1.0 (API 26–36)</div>
            <div class="meta-text">Compiled with Gradle &amp; signed for installation</div>
          </div>
          <a href="/download/app-debug.apk" class="apk-btn btn-primary" style="flex:0 0 auto;">Download APK</a>
        </div>
      </div>

      <div class="card">
        <div class="section-label">HOME &amp; LOCK-SCREEN WIDGET PREVIEW (TAP TO REFRESH)</div>
        <div id="widgetSurface" class="widget-preview" role="button" tabindex="0" aria-label="Home screen widget preview">
          <div id="widgetTitle" class="widget-title">PUBLIC ENDPOINT</div>
          <div id="widgetBody" class="widget-body">Tap Apply &amp; Sync Live to load data.</div>
          <div id="widgetUpdated" class="widget-footer">Updated just now · Tap widget to refresh</div>
        </div>
      </div>

      <div class="card">
        <div class="section-label">LOCK-SCREEN LIVE UPDATE &amp; WEARABLE PREVIEW</div>
        <div class="widget-preview" style="border-radius:14px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span id="notifTitle" style="font-size:12px;font-weight:700;color:var(--blue);">App Notify · Live</span>
            <span style="font-size:11px;color:var(--muted);">VISIBILITY_PUBLIC · Promoted</span>
          </div>
          <div id="notifBody" style="margin-top:6px;font-size:13px;">Standby — tap Apply &amp; Sync Live to start foreground session.</div>
        </div>
      </div>

      <div class="card">
        <div class="section-label">4 · LIVE PAYLOAD &amp; DIAGNOSTICS</div>
        <div id="status" style="font-size:13px;font-weight:700;">Ready</div>
        <div id="authDiagnostic" class="meta-text">Auth: Public HTTPS (none) · Sync: Every 30 min (inexact)</div>
        <div id="content" class="panel">Tap Apply &amp; Sync Live to load data.</div>
      </div>

      <div class="card">
        <div class="section-label">5 · PRIVACY, SECURITY &amp; PLAY STORE INFO</div>
        <div class="meta-text">
          Credentials stay exclusively on-device in private storage (<code>MODE_PRIVATE</code>, <code>allowBackup="false"</code>), are never logged, and only transmit over TLS/HTTPS to your configured endpoint.
        </div>
        <button type="button" id="privacyInfoBtn" class="btn-secondary w-full" style="color:var(--blue);">
          View Privacy Policy &amp; Data Safety Details
        </button>
      </div>
    </div>
  </div>

  <!-- Privacy Modal -->
  <div id="privacyModal" class="modal-backdrop">
    <div class="modal" role="dialog" aria-labelledby="modalTitle">
      <h2 id="modalTitle" style="margin:0 0 12px;font-size:18px;">Privacy, Security &amp; Battery Architecture</h2>
      <div class="meta-text" style="color:var(--navy);font-size:13px;line-height:1.55;">
        <p><strong>1. On-Device Credential Storage:</strong><br/>
        Bearer tokens and API keys are stored strictly in private app sandbox storage (<code>MODE_PRIVATE</code>) with Android cloud backup disabled (<code>allowBackup=false</code>). Credentials are never logged or shared with third parties.</p>
        <p><strong>2. Strict HTTPS Transport:</strong><br/>
        Cleartext HTTP traffic is blocked at the OS manifest level. Custom endpoints and headers only transmit over TLS/HTTPS directly to the URL you configure.</p>
        <p><strong>3. Battery &amp; Foreground Service Policy:</strong><br/>
        • Background sync uses non-wakeup inexact alarms (<code>ELAPSED_REALTIME</code>) so your device stays in deep Doze sleep.<br/>
        • Conditional HTTP requests (<code>If-None-Match</code> / ETag &amp; <code>If-Modified-Since</code>) avoid re-downloading unchanged JSON.<br/>
        • Background refreshes automatically pause when Android Battery Saver or Data Saver is active.<br/>
        • You can stop the live foreground notification at any time using <strong>Stop Live</strong> in the app or directly from the notification shade.</p>
      </div>
      <button type="button" id="closeModal" class="btn-primary w-full" style="margin-top:14px;">Got it</button>
    </div>
  </div>

  <script>
    const SAMPLES = {
      buses: {
        type: "buses", route: "21", stop: "Central Station",
        arrivals: [
          { destination: "Downtown", minutes: 3, status: "On time" },
          { destination: "Downtown", minutes: 12, status: "2 min late" },
          { destination: "Downtown", minutes: 24, status: "On time" }
        ]
      },
      food: {
        type: "food_delivery", restaurant: "Green Bowl Kitchen",
        courier: "Maya", minutes_left: 18, status: "On the way"
      },
      events: {
        type: "events",
        events: [
          { title: "Product stand-up", time: "9:30 AM", location: "Zoom" },
          { title: "Dentist appointment", time: "1:15 PM", location: "Carmel" },
          { title: "Dinner with family", time: "6:30 PM", location: "Home" }
        ]
      },
      family: {
        type: "family_locations",
        people: [
          { name: "Appa", location: "Hosur", updated: "Just now" },
          { name: "Amma", location: "Madurai", updated: "2 min ago" },
          { name: "Shuk", location: "Coimbatore", updated: "5 min ago" }
        ]
      }
    };

    const sourceEl = document.getElementById("source");
    const urlEl = document.getElementById("url");
    const authModeEl = document.getElementById("authMode");
    const authHeaderEl = document.getElementById("authHeader");
    const authSecretEl = document.getElementById("authSecret");
    const revealSecretEl = document.getElementById("revealSecret");
    const refreshIntervalEl = document.getElementById("refreshInterval");
    const showWidgetEl = document.getElementById("showWidget");
    const showNotificationEl = document.getElementById("showNotification");
    const saveBtn = document.getElementById("save");
    const stopLiveBtn = document.getElementById("stopLive");
    const liveBadge = document.getElementById("liveBadge");
    const statusEl = document.getElementById("status");
    const authDiagEl = document.getElementById("authDiagnostic");
    const contentEl = document.getElementById("content");
    const widgetTitleEl = document.getElementById("widgetTitle");
    const widgetBodyEl = document.getElementById("widgetBody");
    const widgetUpdatedEl = document.getElementById("widgetUpdated");
    const notifTitleEl = document.getElementById("notifTitle");
    const notifBodyEl = document.getElementById("notifBody");
    const batterySummaryEl = document.getElementById("batteryStatusSummary");

    let cachedEtag = "";
    let cachedUrl = "";
    let lastFormatted = "";

    function updateUIControls() {
      const src = sourceEl.value;
      document.getElementById("endpointContainer").style.display = src === "endpoint" ? "block" : "none";
      if (src === "family") {
        showNotificationEl.checked = false;
        showNotificationEl.disabled = true;
      } else {
        showNotificationEl.disabled = false;
      }

      const mode = authModeEl.value;
      document.getElementById("authHeaderContainer").style.display = mode === "api_key" ? "block" : "none";
      document.getElementById("authSecretContainer").style.display = mode === "none" ? "none" : "block";
      document.getElementById("authSecretLabel").textContent =
        mode === "bearer" ? "Bearer Token (Authorization: Bearer <token>)" : "API Key Value";

      const mins = Number(refreshIntervalEl.value);
      batterySummaryEl.textContent = mins === 0
        ? "Zero background wakeups: API only called when you tap Refresh or tap a widget."
        : `Inexact non-wakeup alarm every ${mins} min + HTTP ETag / 304 conditional caching.`;

      const authText = src !== "endpoint"
        ? "Auth: Built-in sample feed"
        : mode === "bearer"
          ? `Auth: Bearer Token ${authSecretEl.value.trim() ? "configured" : "(missing token)"}`
          : mode === "api_key"
            ? `Auth: Header [${authHeaderEl.value.trim() || "X-API-Key"}] ${authSecretEl.value.trim() ? "configured" : "(missing key)"}`
            : "Auth: Public HTTPS (none)";
      const schedText = mins === 0 ? "Manual refresh only" : `Every ${mins} min (inexact)`;
      authDiagEl.textContent = `${authText} · Sync: ${schedText}`;
    }

    sourceEl.addEventListener("change", updateUIControls);
    authModeEl.addEventListener("change", updateUIControls);
    refreshIntervalEl.addEventListener("change", updateUIControls);
    authHeaderEl.addEventListener("input", updateUIControls);
    authSecretEl.addEventListener("input", updateUIControls);
    revealSecretEl.addEventListener("change", () => {
      authSecretEl.type = revealSecretEl.checked ? "text" : "password";
    });
    document.getElementById("clearAuth").addEventListener("click", () => {
      authModeEl.value = "none";
      authSecretEl.value = "";
      authHeaderEl.value = "X-API-Key";
      cachedEtag = "";
      updateUIControls();
    });

    function renderSurfaces(formattedStr, statusLabel) {
      lastFormatted = formattedStr;
      contentEl.textContent = formattedStr;
      const nowStr = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });
      statusEl.textContent = `${statusLabel} · Updated: ${nowStr}`;
      widgetUpdatedEl.textContent = `Updated ${nowStr} · Tap widget to refresh`;

      let title = "PUBLIC ENDPOINT";
      let wBody = formattedStr.replace(/[{}\[\]"]/g, " ").replace(/\s+/g, " ").trim().slice(0, 180);
      let nBody = wBody;

      try {
        const obj = JSON.parse(formattedStr);
        if (obj.type === "buses") {
          title = `ROUTE ${obj.route}`;
          wBody = `Route ${obj.route} · ${obj.stop}\n` + obj.arrivals.map((b, i) => `${i === 0 ? "●" : "○"} ${b.minutes} min · ${b.destination} (${b.status})`).join("\n");
          nBody = `${obj.arrivals[0].minutes} min · Route ${obj.route} to ${obj.arrivals[0].destination} · ${obj.arrivals[0].status}`;
        } else if (obj.type === "food_delivery") {
          title = "FOOD DELIVERY";
          wBody = `${obj.restaurant}\n${obj.minutes_left}:00 remaining · ${obj.status}\nCourier: ${obj.courier}`;
          nBody = `${obj.minutes_left} min 00 sec · ${obj.restaurant} · ${obj.status}`;
        } else if (obj.type === "events") {
          title = "EVENTS";
          wBody = "Today\n" + obj.events.map(e => `${e.time} · ${e.title} (${e.location})`).join("\n");
          nBody = `${obj.events[0].time} · ${obj.events[0].title} · ${obj.events[0].location}`;
        } else if (obj.type === "family_locations") {
          title = "FAMILY LOCATIONS";
          wBody = obj.people.map(p => `${p.name} · ${p.location} · ${p.updated}`).join("\n");
          nBody = "Widget-only feed (Live notification disabled by default)";
        }
      } catch (_) {}

      widgetTitleEl.textContent = title;
      widgetBodyEl.textContent = showWidgetEl.checked ? wBody : "Hidden — Enable 'Display feed on Home & Lock-screen widgets'";
      notifTitleEl.textContent = `${title} · LIVE`;
      notifBodyEl.textContent = showNotificationEl.checked ? nBody : "Live notification paused";
    }

    async function syncFeed() {
      updateUIControls();
      const src = sourceEl.value;
      if (src !== "endpoint") {
        const formatted = JSON.stringify(SAMPLES[src], null, 2);
        renderSurfaces(formatted, "Sample feed · Ready");
        const isLive = showNotificationEl.checked;
        liveBadge.textContent = isLive ? "LIVE SESSION" : "STANDBY";
        stopLiveBtn.disabled = !isLive;
        stopLiveBtn.style.opacity = isLive ? "1" : "0.5";
        return;
      }

      const targetUrl = urlEl.value.trim();
      if (!targetUrl.startsWith("https://")) {
        statusEl.textContent = "Validation error: Use a valid HTTPS URL (https://)";
        return;
      }
      const mode = authModeEl.value;
      const secret = authSecretEl.value.trim();
      if (mode !== "none" && !secret) {
        statusEl.textContent = "Validation error: Enter your Bearer Token or API Key, or switch Auth to None";
        return;
      }

      saveBtn.disabled = true;
      statusEl.textContent = "Syncing endpoint…";
      try {
        const res = await fetch("/api/proxy-fetch", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            url: targetUrl,
            authMode: mode,
            authHeader: authHeaderEl.value.trim() || "X-API-Key",
            authSecret: secret,
            etag: cachedUrl === targetUrl ? cachedEtag : ""
          })
        });
        const data = await res.json();
        if (!res.ok || data.error) {
          throw new Error(data.error || `HTTP ${res.status}`);
        }
        if (data.status === 304 && lastFormatted) {
          renderSurfaces(lastFormatted, "HTTP 304 Not Modified · Cached");
        } else {
          cachedUrl = targetUrl;
          cachedEtag = data.etag || "";
          renderSurfaces(data.formatted, data.statusTag || "HTTP 200 · OK");
        }
        const isLive = showNotificationEl.checked;
        liveBadge.textContent = isLive ? "LIVE SESSION" : "STANDBY";
        stopLiveBtn.disabled = !isLive;
        stopLiveBtn.style.opacity = isLive ? "1" : "0.5";
      } catch (err) {
        statusEl.textContent = `Could not load endpoint · ${err.message}`;
        contentEl.textContent = err.message;
      } finally {
        saveBtn.disabled = false;
      }
    }

    saveBtn.addEventListener("click", syncFeed);
    document.getElementById("widgetSurface").addEventListener("click", syncFeed);
    stopLiveBtn.addEventListener("click", () => {
      showNotificationEl.checked = false;
      liveBadge.textContent = "STANDBY";
      stopLiveBtn.disabled = true;
      stopLiveBtn.style.opacity = "0.5";
      notifBodyEl.textContent = "Live foreground session stopped.";
    });
    document.getElementById("testNotification").addEventListener("click", () => {
      statusEl.textContent = "Sent test alert to Galaxy Fit / wearable channel";
    });
    document.getElementById("privacyInfoBtn").addEventListener("click", () => {
      document.getElementById("privacyModal").style.display = "flex";
    });
    document.getElementById("closeModal").addEventListener("click", () => {
      document.getElementById("privacyModal").style.display = "none";
    });

    // Initial load
    syncFeed();
  </script>
</body>
</html>
"""

class Handler(http.server.BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass

    def do_HEAD(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/download/app-debug.apk" and os.path.exists(APK_PATH):
            with open(APK_PATH, "rb") as f:
                data = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", 'attachment; filename="app-debug.apk"')
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        body = HTML_PAGE.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        if self.path != "/api/proxy-fetch":
            self.send_response(404)
            self.end_headers()
            return
        try:
            length = int(self.headers.get("Content-Length", "0"))
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            target_url = (payload.get("url") or "").strip()
            if not target_url.startswith("https://"):
                raise ValueError("Endpoint must use HTTPS (https://)")

            req = urllib.request.Request(target_url, headers={
                "Accept": "application/json",
                "User-Agent": "AppNotify/1.1 (Android; BatteryOptimized)"
            })
            auth_mode = payload.get("authMode", "none")
            secret = (payload.get("authSecret") or "").strip()
            if auth_mode == "bearer" and secret:
                token = secret[7:].strip() if secret.lower().startswith("bearer ") else secret
                req.add_header("Authorization", f"Bearer {token}")
            elif auth_mode == "api_key" and secret:
                header_name = (payload.get("authHeader") or "X-API-Key").strip() or "X-API-Key"
                req.add_header(header_name, secret)

            etag = (payload.get("etag") or "").strip()
            if etag:
                req.add_header("If-None-Match", etag)

            try:
                with urllib.request.urlopen(req, timeout=10) as resp:
                    raw = resp.read().decode("utf-8")
                    parsed_json = json.loads(raw.strip())
                    formatted = json.dumps(parsed_json, indent=2)
                    resp_etag = resp.headers.get("ETag", "")
                    out = {
                        "status": resp.status,
                        "formatted": formatted,
                        "etag": resp_etag,
                        "statusTag": f"HTTP {resp.status}" + (" · ETag cached" if resp_etag else " · OK")
                    }
            except urllib.error.HTTPError as he:
                if he.code == 304:
                    out = {"status": 304, "formatted": "", "etag": etag, "statusTag": "HTTP 304 Not Modified · Cached"}
                elif he.code in (401, 403):
                    raise ValueError(f"HTTP {he.code} Unauthorized — Verify your API Key or Bearer Token.")
                else:
                    raise ValueError(f"HTTP {he.code} — Endpoint returned an error status.")

            resp_bytes = json.dumps(out).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(resp_bytes)))
            self.end_headers()
            self.wfile.write(resp_bytes)
        except Exception as e:
            err_bytes = json.dumps({"error": str(e)}).encode("utf-8")
            self.send_response(400)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(err_bytes)))
            self.end_headers()
            self.wfile.write(err_bytes)

if __name__ == "__main__":
    http.server.ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
