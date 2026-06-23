# AE Urlaubsantrag

Einfaches Java-Projekt zur Urlaubsverwaltung ohne Maven.

## Projektstruktur

```text
.
|-- .vscode/
|   `-- settings.json
|-- lib/
|   `-- mariadb-java-client-3.5.8.jar
|-- src/
|   `-- DbTest.java
`-- ae_urlaubsantr__ge (1).sql
```

## Voraussetzungen

- Java 21
- Visual Studio Code mit der Erweiterung `Extension Pack for Java`
- Lokale MariaDB auf Port `3306`
- Datenbank `ae_urlaubsantraege`
- Tabelle `mitarbeiter`

## Datenbankverbindung

`DbTest.java` verwendet standardmaessig:

- Benutzer: `root`
- Passwort: leer

Abweichende Zugangsdaten koennen in PowerShell vor dem Start gesetzt werden:

```powershell
$env:DB_USER = "mein_benutzer"
$env:DB_PASSWORD = "mein_passwort"
```

Danach kann `DbTest.java` in Visual Studio Code ueber `Run Java` gestartet werden.

## Was der Test macht

Der Test verbindet sich mit der lokalen Datenbank und fuehrt nur diese lesende
Abfrage aus:

```sql
SELECT COUNT(*) FROM mitarbeiter;
```

Tabellen und Daten werden nicht veraendert.
