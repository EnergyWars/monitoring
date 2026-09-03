# Features – Monitoring

Monitoring überwacht beliebige Web-Seiten/APIs im Hintergrund und benachrichtigt,
sobald ein konfiguriertes Verhalten eintritt. Erste unterstützte Seiten-Art:
**JSON-API**.

## Überwachte Seiten verwalten

- Home-Screen listet alle überwachten Seiten als Karten: Name, Typ-Chip,
  Host-Name aus der URL, Status der letzten Prüfung (Noch nicht geprüft /
  Kein Treffer / Treffer gefunden / Fehler) samt Zeitpunkt, sowie ein Schalter
  zum Aktivieren/Deaktivieren einzelner Seiten.
- FAB „+" öffnet das Formular zum Hinzufügen einer neuen Seite.
- Tippen auf eine Karte öffnet den Verlauf der Seite; von dort per Stift-Symbol
  ins Bearbeiten-Formular.
- Im Formular: Löschen (mit Bestätigungs-Dialog), das den kompletten
  Prüf-Verlauf der Seite mit entfernt.
- Ist noch keine Seite angelegt, zeigt der Home-Screen einen Leerzustand mit
  Hinweistext.

## Seiten-Art „JSON-API"

- Konfiguration je Seite: Name, URL (GET-Request), JSON-Pfad-Ausdruck.
- Pfad-Syntax: durch „." getrennte Segmente; ein Segment mit Suffix „[*]"
  iteriert ein Array. Beispiel `availabilities[*].slots[*]` liefert alle
  `slots`-Elemente über alle `availabilities`-Einträge hinweg — leer, wenn
  jedes `slots`-Array leer ist.
- Bedingung „erfüllt", sobald der Pfad mindestens einen Wert liefert
  (Doctolib-Beispiel aus `beispiel.md` exakt abgedeckt).
- Button „Jetzt testen" im Formular führt die Prüfung sofort aus (ohne zu
  speichern) und zeigt Trefferanzahl, „Kein Treffer" oder eine Fehlermeldung.
- Die Typ-Auswahl ist als Dropdown-Struktur angelegt, aktuell aber auf
  „JSON-API" fixiert — weitere Seiten-Arten lassen sich ergänzen, ohne das
  Datenmodell zu ändern (`PageType`-Enum).

## Hintergrund-Prüfung alle 30 Minuten (mit Zeit-Jitter)

- Läuft unabhängig vom Gerätezustand (Doze, Akku-Sparmodus, Xiaomi/MIUI):
  - Primär eine selbst verlängernde `AlarmManager`-Kette
    (`setExactAndAllowWhileIdle`), die bei jedem Auslösen sofort den nächsten
    Alarm plant und danach die eigentliche Prüfung als WorkManager-Job
    anstößt.
  - Zusätzlich ein periodischer WorkManager-Fallback (30-Minuten-Takt) für
    den Fall, dass exakte Alarme vom System verweigert werden.
  - Nach einem Neustart des Geräts armiert ein Boot-Receiver die Alarm-Kette
    neu.
  - Bevor die eigentlichen HTTP-Prüfungen abgeschickt werden, wartet der
    Hintergrund-Job zusätzlich eine zufällige Zeit zwischen 0 und 300
    Sekunden — damit die Anfragen nicht in einem exakten, leicht als Bot
    erkennbaren 30-Minuten-Takt beim Zielserver ankommen.
- Pro Durchlauf werden alle aktivierten Seiten parallel abgefragt, Status und
  Zeitpunkt der letzten Prüfung je Seite gespeichert.

## Benachrichtigung mit Bestätigungspflicht

- Bei einem Treffer: Ergebnis wird persistiert (Zeitpunkt, Trefferanzahl,
  Ausschnitt der Treffer) und eine nicht wegwischbare Benachrichtigung
  erscheint (eine je Seite, mit Aktion „Bestätigen").
- Solange nicht bestätigt, wird beim nächsten Prüf-Durchlauf nach Ablauf
  einer Stunde erneut benachrichtigt — bis der Nutzer „Bestätigen" antippt
  (aus der Notification oder im Verlauf der Seite).

## Ergebnis-Verlauf je Seite

- Verlauf-Screen einer Seite listet alle bisherigen Treffer (Zeitpunkt,
  Trefferanzahl, Ausschnitt der Werte, Status offen/bestätigt).
- „Bestätigen" auf einem offenen Eintrag bestätigt alle offenen Treffer der
  Seite auf einmal (dieselbe Logik wie die Benachrichtigungs-Aktion).

## Zuverlässigkeits-Einstellungen

- Eigener Einstellungs-Screen „Zuverlässigkeit" (über Einstellungen
  erreichbar) mit Status und Freigabe-Button je Punkt:
  - Benachrichtigungen erlauben (Android 13+).
  - Akku-Optimierung für die App deaktivieren.
  - „Alarme & Erinnerungen" erlauben (Android 12+, nötig für exakte Alarme).
  - Autostart aktivieren — nur auf Xiaomi/Redmi/POCO/Black-Shark-Geräten
    sichtbar, öffnet die MIUI-Sicherheits-App-Einstellung.
- Ein Banner auf dem Home-Screen weist auf fehlende Freigaben hin, solange
  die Einrichtung unvollständig ist.

## Design / Theme

- Übernommen aus dem WaffleHQ-Basisprojekt (`/base-project`): sieben
  Hue-Rampen, Material-3-Farbrollen, Geist-Schrift, wiederverwendbare
  Komponenten (`ui/components/App*.kt`). Theme-Umschalter (System/Hell/Dunkel)
  unter Einstellungen → Anzeige.

## Feature-Liste

- Aus dem Basisprojekt übernommene Infrastruktur: Markdown-Dateien unter
  `features/*.md` werden im Build nach `assets/features/` gespiegelt und im
  Einstellungs-Bereich „Features" als abhakbare Liste mit Detail-Ansicht
  angezeigt.
