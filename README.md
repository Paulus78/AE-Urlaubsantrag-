# AE Urlaubsantrag

Einfaches Java-Projekt zur Urlaubsverwaltung ohne Maven.

## Voraussetzungen

- Java ist installiert.
- MariaDB laeuft lokal auf Port `3306`.
- Die Datenbank `ae_urlaubsantraege` ist importiert.
- Die Datei `lib/mariadb-java-client-3.5.8.jar` ist vorhanden.

Die Datenbankverbindung steht in:

```text
src/dao/DatabaseConnection.java
```

Aktuell verwendet das Projekt:

```text
Benutzer: root
Passwort: leer
```

## Programm starten

### 1. Projekt kompilieren

Im Projektordner ausfuehren:

```powershell
$javaFiles = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib\mariadb-java-client-3.5.8.jar" -d bin $javaFiles
```

### 2. RMI-Server starten

In einem Terminal ausfuehren und offen lassen:

```powershell
java -cp "bin;lib\mariadb-java-client-3.5.8.jar" server.RMIServer
```

Der Server startet dabei automatisch die RMI-Registry auf Port `1099`.
Die Registry muss also nicht extra manuell gestartet werden.

### 3. Client starten

In einem zweiten Terminal ausfuehren:

```powershell
java -cp "bin;lib\mariadb-java-client-3.5.8.jar" client.ClientMain
```

Danach erscheint das Konsolenmenue der Urlaubsverwaltung.

## Reihenfolge

```text
1. MariaDB starten
2. Projekt kompilieren
3. RMIServer starten
4. ClientMain starten
```

## Datenbank kurz testen

Optional kann die Verbindung mit `DbTest` getestet werden:

```powershell
java -cp "bin;lib\mariadb-java-client-3.5.8.jar" DbTest
```

Der Test fuehrt nur diese lesende Abfrage aus:

```sql
SELECT COUNT(*) FROM mitarbeiter;
```

Dabei werden keine Tabellen oder Daten veraendert.

## Testdaten einspielen

Fuer einfache Tests gibt es diese Datei:

```text
testdaten.sql
```

In phpMyAdmin:

```text
1. Datenbank ae_urlaubsantraege auswaehlen
2. Importieren anklicken
3. testdaten.sql auswaehlen
4. Import starten
```

Die Datei legt diese Test-Mitarbeiter an:

```text
ID 6: Paul Test
ID 7: Mara Vertretung
ID 8: Nils Kollege
```

Ausserdem ist Mitarbeiter 2 an Tag 8 bis 12 im Kalender eingetragen.
Damit kann man testen, dass eine nicht verfuegbare Vertretung zu einem
offenen Antrag fuehrt.

## Programm testen

Eine ausfuehrliche Anleitung mit festen Eingaben und erwarteten Ergebnissen
steht in:

```text
TEST_ANLEITUNG.md
```

Vor jedem Test:

```text
1. MariaDB starten
2. Testdaten einspielen
3. Projekt kompilieren
4. RMIServer starten
5. ClientMain starten
```

### Test 1: Verbindung zur Datenbank

```powershell
java -cp "bin;lib\mariadb-java-client-3.5.8.jar" DbTest
```

Erwartung:

```text
Verbindung erfolgreich.
Anzahl Mitarbeiter: ...
```

### Test 2: Urlaub wird automatisch genehmigt

Im Menue eingeben:

```text
Auswahl: 1
Antragsteller-ID: 6
Vertretung-ID: 7
Starttag: 1
Endtag: 5
```

Erwartung:

```text
Erfolgreich: true
Status: genehmigt
```

Warum?

Mitarbeiter 7 hat in diesem Zeitraum keinen Urlaub. Die Vertretung ist also
verfuegbar und der Antrag wird automatisch genehmigt.

### Test 3: Urlaub bleibt offen

Im Menue eingeben:

```text
Auswahl: 1
Antragsteller-ID: 6
Vertretung-ID: 2
Starttag: 8
Endtag: 12
```

Erwartung:

```text
Erfolgreich: true
Status: offen
```

Warum?

Mitarbeiter 2 ist laut Testdaten an Tag 8 bis 12 schon im Urlaub.
Die Vertretung ist also nicht verfuegbar. Weil Mitarbeiter 6 einen
Vorgesetzten hat, wird der Antrag offen gespeichert.

### Test 4: Offene Antraege anzeigen

Im Menue eingeben:

```text
Auswahl: 2
```

Erwartung:

Der offene Antrag aus Test 3 wird angezeigt.

### Test 5: Offenen Antrag genehmigen

Im Menue eingeben:

```text
Auswahl: 3
Antrag-ID: ID aus der Anzeige
Genehmigen? (j/n): j
```

Erwartung:

```text
Erfolgreich: true
Status: genehmigt
```

Danach wird ein Kalendereintrag erstellt und der Resturlaub reduziert.

### Test 6: Offenen Antrag ablehnen

Dafuer zuerst nochmal einen offenen Antrag wie in Test 3 anlegen.
Dann im Menue eingeben:

```text
Auswahl: 3
Antrag-ID: ID aus der Anzeige
Genehmigen? (j/n): n
```

Erwartung:

```text
Erfolgreich: true
Status: abgelehnt
```

Dabei wird kein Kalendereintrag erstellt und der Resturlaub bleibt gleich.

## Projektstruktur

```text
src/
  client/        Startpunkt fuer den Client
  server/        Startpunkt fuer den RMI-Server
  presentation/  ConsoleUI
  service/       Fachlogik und RMI-Service
  dao/           Datenbankzugriff mit JDBC
  dto/           Datenuebertragung
  model/         einfache Datenobjekte
```
