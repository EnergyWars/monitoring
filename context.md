# context.md – Monitoring

Beschreibt nur den **aktuellen Stand** (keine Historie). Bei Widersprüchen hat
`FEATURES.md` Vorrang. Nach jeder Funktions-Änderung pflegen.

## Aufbau jedes Projektes

- **FEATURES.md**: im Projektstamm, vollständige Feature-Beschreibung.
- **.gitignore**: enthält `dev/*`, `java_pid*`, `*.hprof`, `features`,
  `features/*` (aus der Vorlage übernommen — Feature-Notizen unter
  `features/*.md` sind bewusst nicht versioniert, siehe unten).
- **context.md**: diese Datei.
- **CLAUDE.md**: verweist auf die globalen Vorgaben in `~/.claude/CLAUDE.md`.

## Stack

Kotlin 2.3.20, AGP 8.13.2, JVM 17, compileSdk 35, minSdk 26, targetSdk 35.
Jetpack Compose Material 3, Single-Activity, Compose Navigation, Hilt, Room
2.8.4, DataStore. OkHttp 4.12.0 (HTTP — bewusst die 4.x-Reihe statt 5.x, da
das `okhttp-android`-Artefakt von 5.x `compileSdk 37` voraussetzt, das
Projekt aber laut Vorlage bei `compileSdk 35` bleibt), kotlinx.serialization.json
1.11.0 (JSON-Parsing, bewusst statt `org.json` wegen Testbarkeit auf reinem
JVM), WorkManager 2.11.2 + androidx.hilt:hilt-work 1.2.0 (Hintergrund-Jobs).
Hausschrift Geist. `gradle build`/`assembleDebug`/`./gradlew …` nie ohne
explizite Erlaubnis ausführen.

Basis ist die Vorlage `/home/simon/IdeaProjects/base-project`
(Skill `base-project`): Theme/Tokens 1:1 übernommen, Design-Showcase und
Lorem-Ipsum-Beispielseiten der Vorlage entfernt und durch die echte App-UI
ersetzt. `verify-theme.sh` (Pfade auf `com.wafflehq.monitoring` angepasst)
ist grün.

## Implementierungsstatus

| Feature | Status | Dateien |
|---|---|---|
| Seiten-Liste (Home) | fertig | `ui/home/HomeScreen.kt`, `ui/home/HomeViewModel.kt` |
| Seite hinzufügen/bearbeiten/löschen + „Jetzt testen" | fertig | `ui/pageform/PageFormScreen.kt`, `ui/pageform/PageFormViewModel.kt` |
| Seiten-Art JSON-API (Pfad-Bedingung) | fertig | `data/monitoring/JsonPathEvaluator.kt`, `data/monitoring/PageType.kt`, `data/monitoring/CheckStatus.kt` |
| HTTP-Abruf | fertig | `data/monitoring/MonitorHttpClient.kt`, `data/monitoring/OkHttpMonitorClient.kt` |
| Kern-Prüflogik (Fetch → Auswerten → Persistieren → Benachrichtigen → Re-Notify) | fertig | `data/monitoring/MonitorCheckUseCase.kt` |
| Datenbank (Seiten + Ergebnisse) | fertig | `data/db/AppDatabase.kt`, `MonitoredPageEntity.kt`, `CheckResultEntity.kt`, `MonitoredPageDao.kt`, `CheckResultDao.kt` |
| Repositories | fertig | `data/monitoring/MonitoredPagesRepository.kt` (+Room-Impl), `CheckResultsRepository.kt` (+Room-Impl) |
| Hintergrund-Ausführung alle 30 Min. + 0-300s Zeit-Jitter (Alarm-Kette + WorkManager-Fallback + Boot-Receiver) | fertig | `background/AlarmScheduler.kt`, `background/MonitorAlarmReceiver.kt`, `background/BootReceiver.kt`, `background/MonitorCheckWorker.kt`, `background/Jitter.kt`, `MonitoringApp.kt` |
| Benachrichtigung + Bestätigungspflicht + stündliche Wiederholung | fertig | `background/NotificationHelper.kt`, `background/Notifier.kt`, `background/NotificationAckReceiver.kt` |
| Ergebnis-Verlauf je Seite | fertig | `ui/pagedetail/PageDetailScreen.kt`, `ui/pagedetail/PageDetailViewModel.kt` |
| Zuverlässigkeits-Einstellungen (Benachrichtigungen/Akku/Alarme/Xiaomi-Autostart) | fertig | `ui/settings/ReliabilitySettingsScreen.kt`, `ui/settings/ReliabilitySettingsViewModel.kt`, `background/ReliabilityStatus.kt` |
| Navigation | fertig | `ui/navigation/AppNavHost.kt`, `MainActivity.kt` (Deep-Link aus Notification via `EXTRA_OPEN_PAGE_ID`) |
| DI | fertig | `di/AppModule.kt`, `di/BindingsModule.kt` |
| Theme / Komponenten / Settings-Grundgerüst / Feature-Liste | aus Vorlage übernommen, unverändert nutzbar | `ui/theme/*`, `ui/components/*`, `ui/settings/SettingsScreen.kt`, `ui/settings/DisplaySettings*.kt`, `ui/features/*`, `data/features/*` |
| Tests (JsonPathEvaluator, MonitorCheckUseCase, Renotify-/Alarm-Logik, PageFormViewModel, HomeViewModel) | fertig, `./gradlew testDebugUnitTest` grün (28/28) | `app/src/test/java/com/wafflehq/monitoring/**` |

## Datenmodell

- `MonitoredPageEntity`: id, name, type (`PageType`, aktuell nur `JSON_API`),
  url, jsonPath, enabled, createdAt, lastCheckedAt, lastCheckStatus
  (`CheckStatus`), lastErrorMessage.
- `CheckResultEntity`: id, pageId (FK → `monitored_pages`, `CASCADE`),
  triggeredAt, matchedSummary, matchCount, acknowledged, lastNotifiedAt.
- Erststand (Version 1), keine Migration nötig.

## Hintergrund-Architektur

- `AlarmManager.setExactAndAllowWhileIdle` plant sich über
  `MonitorAlarmReceiver` bei jedem Auslösen selbst neu (unabhängig vom
  Check-Ergebnis) und enqueued `MonitorCheckWorker` als eindeutigen,
  expedited WorkManager-Job (`UNIQUE_WORK_NAME = "monitoring_check"`).
- Periodischer WorkManager-Fallback (`ExistingPeriodicWorkPolicy.KEEP`,
  gleicher Unique-Name) läuft parallel als Sicherheitsnetz, ohne doppelt zu
  feuern.
- `MonitoringApp` implementiert `Configuration.Provider` mit
  `HiltWorkerFactory`; die automatische WorkManager-Standardinitialisierung
  ist im Manifest deaktiviert (`androidx.startup`-Provider-Merge).
- `MonitorCheckUseCase.renotifyOutstanding()` übernimmt die stündliche
  Wiederholung anhand der reinen Funktion `needsRenotify(...)` — kein
  separater Alarm nötig.
- `MonitorCheckWorker.doWork()` wartet vor dem eigentlichen Prüfdurchlauf
  eine zufällige Zeit (`randomJitterMillis()`, 0–300 s), damit die
  ausgehenden Requests nicht exakt im 30-Minuten-Takt beim Zielserver
  ankommen.

## Offene Punkte

- `./gradlew testDebugUnitTest` läuft grün (28 Tests). Kein `assembleDebug`/
  Geräte-Test durchgeführt.
- Reliability-/Permission-Verhalten (Notification-Dialog, Xiaomi-Autostart-
  Intent, Battery-Optimization-Intent) ist nur auf echtem Gerät/Emulator
  final zu verifizieren.
- Launcher-Icon ist noch der Basisprojekt-Platzhalter (`mipmap-*`), das
  Notification-Small-Icon nutzt vorerst denselben Launcher-Icon-Adaptive-Satz
  statt eines eigenen einfarbigen Status-Icons.
