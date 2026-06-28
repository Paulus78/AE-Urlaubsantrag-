# Testanleitung fuer die Urlaubsverwaltung

Diese Anleitung nutzt die Daten aus `ae_urlaubsantr__ge (1).sql`.

Vorhandene Mitarbeiter:

```text
ID 1: Max Chef, kein Vorgesetzter
ID 2: Anna Meyer, Vorgesetzter 1
ID 3: Tom Schmidt, Vorgesetzter 1
ID 4: Lisa Bauer, Vorgesetzter 1
ID 5: Tim Weber, Vorgesetzter 1
```

Wichtig:

Im SQL-Dump sind am Anfang noch keine Urlaubsantraege und keine
Kalendereintraege vorhanden. Deshalb ist jede Vertretung zuerst frei.
Ein Antrag wird dadurch meistens direkt genehmigt.

## Vorbereitung

### 1. Datenbank starten

MariaDB muss laufen und die Datenbank `ae_urlaubsantraege` muss vorhanden sein.

### 2. Projekt kompilieren

Im Projektordner ausfuehren:

```powershell
$javaFiles = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib\mariadb-java-client-3.5.8.jar" -d bin $javaFiles
```

### 3. Server starten

In einem Terminal starten und offen lassen:

```powershell
java -cp "bin;lib\mariadb-java-client-3.5.8.jar" server.RMIServer
```

### 4. Client starten

In einem zweiten Terminal starten:

```powershell
java -cp "bin;lib\mariadb-java-client-3.5.8.jar" client.ClientMain
```

Danach erscheint:

```text
=== Urlaubsverwaltung ===
1 - Urlaub beantragen
2 - Offene Antraege anzeigen
3 - Antrag entscheiden
0 - Beenden
```

## Test 1: Urlaub beantragen mit freier Vertretung

Eingabe im Menue:

```text
Auswahl: 1
Antragsteller-ID: 2
Vertretung-ID: 3
Starttag: 1
Endtag: 5
```

Erwartung:

```text
Erfolgreich: true
Status: genehmigt
Meldung: Der Antrag wurde automatisch genehmigt.
```

Warum?

Tom Schmidt `ID 3` hat noch keinen Kalendereintrag in diesem Zeitraum.
Die Vertretung ist also frei. Deshalb wird der Antrag direkt genehmigt.

## Test 2: Offene Antraege anzeigen, wenn es keine offenen Antraege gibt

Eingabe im Menue:

```text
Auswahl: 2
```

Erwartung:

```text
Es gibt keine offenen Antraege.
```

Warum?

Der Antrag aus Test 1 wurde direkt genehmigt. Genehmigte Antraege erscheinen
nicht in der Liste der offenen Antraege.

## Test 3: Vertretung blockieren, damit ein offener Antrag entsteht

Damit ein offener Antrag entstehen kann, muss die Vertretung im gewuenschten
Zeitraum schon Urlaub haben.

In phpMyAdmin unter SQL ausfuehren:

```sql
INSERT INTO urlaubskalender
    (Mitarbeiter_ID, Starttag, Endtag)
VALUES
    (3, 8, 12);
```

Damit ist Tom Schmidt `ID 3` an Tag 8 bis 12 nicht verfuegbar.

Danach im Programm:

```text
Auswahl: 1
Antragsteller-ID: 2
Vertretung-ID: 3
Starttag: 8
Endtag: 12
```

Erwartung:

```text
Erfolgreich: true
Status: offen
Meldung: Der Antrag wurde gespeichert und wartet auf Entscheidung.
```

Warum?

Anna Meyer `ID 2` beantragt Urlaub. Ihre Vertretung Tom Schmidt `ID 3` ist
in diesem Zeitraum blockiert. Anna hat aber einen Vorgesetzten, naemlich
Max Chef `ID 1`. Deshalb wird der Antrag offen gespeichert.

Die angezeigte Antrag-ID fuer Test 5 merken.

## Test 4: Offene Antraege anzeigen

Eingabe im Menue:

```text
Auswahl: 2
```

Erwartung:

Der offene Antrag aus Test 3 wird angezeigt, zum Beispiel:

```text
Antrag-ID: ...
Zeitraum: Tag 8 bis Tag 12
Status: offen
Antragsteller-ID: 2
Vertretung-ID: 3
Genehmiger-ID: 1
```

## Test 5: Offenen Antrag genehmigen

Eingabe im Menue:

```text
Auswahl: 3
Antrag-ID: ID aus Test 4
Genehmigen? (j/n): j
```

Erwartung:

```text
Erfolgreich: true
Status: genehmigt
Meldung: Der Antrag wurde genehmigt.
```

Warum?

Der offene Antrag wird durch die Entscheidung genehmigt. Danach wird ein
Kalendereintrag fuer Anna Meyer erstellt und ihr Resturlaub wird reduziert.

## Test 6: Offenen Antrag ablehnen

Zuerst wieder einen offenen Antrag erzeugen. Dafuer kann man einen anderen
Zeitraum nehmen und die Vertretung vorher blockieren:

```sql
INSERT INTO urlaubskalender
    (Mitarbeiter_ID, Starttag, Endtag)
VALUES
    (4, 15, 19);
```

Danach im Programm:

```text
Auswahl: 1
Antragsteller-ID: 3
Vertretung-ID: 4
Starttag: 15
Endtag: 19
```

Erwartung:

```text
Status: offen
```

Dann ablehnen:

```text
Auswahl: 3
Antrag-ID: ID aus der Anzeige
Genehmigen? (j/n): n
```

Erwartung:

```text
Erfolgreich: true
Status: abgelehnt
Meldung: Der Antrag wurde abgelehnt.
```

Warum?

Bei Ablehnung wird kein Kalendereintrag fuer den Antragsteller erstellt und
der Resturlaub bleibt unveraendert.

## Test 7: Fehlerfall eigene Vertretung

Eingabe im Menue:

```text
Auswahl: 1
Antragsteller-ID: 2
Vertretung-ID: 2
Starttag: 20
Endtag: 22
```

Erwartung:

```text
Erfolgreich: false
Meldung: Der Antragsteller darf nicht seine eigene Vertretung sein.
```

## Test 8: Fehlerfall zu wenig Resturlaub

Eingabe im Menue:

```text
Auswahl: 1
Antragsteller-ID: 4
Vertretung-ID: 5
Starttag: 1
Endtag: 40
```

Erwartung:

```text
Erfolgreich: false
Meldung: Der Resturlaub reicht fuer diesen Antrag nicht aus.
```

Warum?

Lisa Bauer `ID 4` hat laut Startdaten 20 Tage Resturlaub. Im Zeitraum
Tag 1 bis 40 liegen mehr Urlaubstage als sie Resturlaub hat.

## Hinweise

Wenn Tests mehrfach durchgefuehrt werden, koennen sich Ergebnisse aendern,
weil neue Antraege, Kalendereintraege und Resturlaub gespeichert werden.

Fuer einen frischen Teststand kann die Datenbank neu aus
`ae_urlaubsantr__ge (1).sql` importiert werden.
