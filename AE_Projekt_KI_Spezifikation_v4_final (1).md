# KI-Spezifikation für das AE-Projekt: Urlaubsantrag mit Vertretung

Version: 0.4 – vollständig finalisiert  
Stand: 2026-06-14  
Zielgruppe: Codex / KI-Assistenten / Entwicklerteam  
Umsetzungsstil: einfach, verständlich, passend für Wirtschaftsinformatik im 2. Semester

---

## 1. Zweck dieser Spezifikation

Diese Spezifikation ist die verbindliche Arbeitsgrundlage für Codex und andere KI-Assistenten im Projekt „Urlaubsantrag mit Vertretung“.

Codex darf das vorhandene Prozessmodell, Datenmodell und Schichtenmodell nicht eigenständig verändern.

Wenn etwas unklar ist, muss Codex zuerst eine Rückfrage stellen.

---

## 2. Grundprinzip für die Umsetzung

Die Anwendung soll bewusst einfach und verständlich bleiben.

Wichtig:

- Kein unnötig komplexer Code.
- Keine Enterprise-Architektur.
- Keine Frameworks wie Spring, Hibernate oder JPA.
- Keine unnötigen Design Patterns.
- Keine unnötige Abstraktion.
- Lieber klare Klassen, einfache Methoden und gut nachvollziehbarer Ablauf.
- Der Code soll so wirken, als könnte ihn ein Wirtschaftsinformatik-Student im 2. Semester erklären.

Trotzdem müssen die fachlichen Regeln, MariaDB, JDBC, RMI und das Schichtenmodell sauber eingehalten werden.

---

## 3. Verbindliche fachliche Entscheidungen

Folgende Entscheidungen sind final festgelegt:

| Nr. | Frage | Entscheidung |
|---|---|---|
| 1 | Was bedeutet „Vertretung vorhanden“? | `Vertretung_ID` ist gesetzt und die Vertretung hat im Zeitraum keinen überschneidenden Kalendereintrag. |
| 2 | Wie werden IDs erzeugt? | MariaDB nutzt `AUTO_INCREMENT`. |
| 3 | Wann wird `Genehmiger_ID` gesetzt? | Schon beim offenen Antrag. `Genehmiger_ID = Vorgesetzter_ID` des Antragstellers. Wenn kein Vorgesetzter vorhanden ist: `NULL`. |
| 4 | Status-Schreibweise | `offen`, `genehmigt`, `abgelehnt`. |
| 5 | Zählen Wochenenden als Urlaubstage? | Nein. |
| 6 | Darf Antragsteller seine eigene Vertretung sein? | Nein. |
| 7 | Darf Mitarbeiter ohne Vorgesetzten Urlaub beantragen? | Ja, aber nur mit verfügbarer Vertretung, weil Anträge mit Vertretung automatisch genehmigt werden. |
| 8 | Soll RMI umgesetzt werden? | Ja, aber einfach und anfängerfreundlich. |

---

## 4. Verbindliches Prozessmodell

Das Prozessmodell ist verbindlich:

```text
Start
  ↓
Urlaub beantragen
  ↓
Vertretung vorhanden?
  ├─ Ja  → Antrag genehmigen → Urlaub eintragen → Urlaubstage anpassen → Ende
  └─ Nein → Urlaubsantrag prüfen → Antrag genehmigt?
                         ├─ Ja  → Urlaub eintragen → Urlaubstage anpassen → Ende
                         └─ Nein → Antrag ablehnen → Ende
```

---

## 5. Prozesslogik im Detail

### 5.1 Urlaub beantragen

Die `ConsoleUI` liest folgende Eingaben:

- Antragsteller-ID
- Vertretung-ID
- Starttag
- Endtag

Diese Eingaben werden in ein `UrlaubsantragEingabeDTO` geschrieben und über RMI an den `IUrlaubsService` übergeben.

Die `ConsoleUI` entscheidet nicht selbst, ob der Antrag genehmigt oder abgelehnt wird.

---

### 5.2 Vertretung vorhanden?

Eine Vertretung gilt als vorhanden/verfügbar, wenn beide Bedingungen erfüllt sind:

1. Es wurde eine `Vertretung_ID` angegeben.
2. Die Vertretung hat im Zeitraum keinen überschneidenden Kalendereintrag in `URLAUBSKALENDER`.

Zusätzlich gilt:

```text
Antragsteller_ID != Vertretung_ID
```

Der Antragsteller darf also nicht seine eigene Vertretung sein.

---

### 5.3 Wenn Vertretung vorhanden ist

Wenn die Vertretung verfügbar ist:

1. Antrag wird automatisch genehmigt.
2. `Status = genehmigt`.
3. `Genehmiger_ID` wird auf den direkten Vorgesetzten des Antragstellers gesetzt.
4. Wenn der Antragsteller keinen Vorgesetzten hat, bleibt `Genehmiger_ID = NULL`.
5. Urlaub wird in `URLAUBSKALENDER` eingetragen.
6. `Resturlaub` des Antragstellers wird reduziert.
7. Prozess endet.

Wichtig:

Anträge mit verfügbarer Vertretung werden immer automatisch genehmigt.

---

### 5.4 Wenn keine Vertretung vorhanden ist

Wenn die Vertretung nicht verfügbar ist:

1. Der Urlaubsantrag wird geprüft.
2. Wenn der Antragsteller einen direkten Vorgesetzten hat:
   - Antrag wird mit `Status = offen` gespeichert.
   - `Genehmiger_ID = Vorgesetzter_ID` des Antragstellers.
   - Der Vorgesetzte entscheidet später.
3. Wenn der Antragsteller keinen direkten Vorgesetzten hat:
   - Der Antrag kann nicht sinnvoll durch einen Vorgesetzten entschieden werden.
   - In diesem Fall wird der Antrag abgelehnt oder nicht angenommen.

Empfohlene einfache Umsetzung:

```text
Kein Vorgesetzter und keine verfügbare Vertretung → ErgebnisDTO mit Fehler/Ablehnung zurückgeben.
```

Falls der Professor ausdrücklich verlangt, dass auch dieser Fall als Datensatz gespeichert wird, sollte der Antrag mit `Status = abgelehnt` gespeichert werden.

---

### 5.5 Vorgesetztenentscheidung

Offene Anträge können in der `ConsoleUI` angezeigt werden.

Der Nutzer kann für einen offenen Antrag eine Entscheidung eingeben:

- genehmigen
- ablehnen

Diese Entscheidung wird als `EntscheidungDTO` an den `UrlaubsService` übergeben.

Der `UrlaubsService` prüft:

1. Existiert der Antrag?
2. Hat der Antrag den Status `offen`?
3. Ist die gespeicherte `Genehmiger_ID` wirklich der direkte Vorgesetzte des Antragstellers?

Wenn genehmigt:

1. `Status = genehmigt`
2. Kalendereintrag erstellen
3. Resturlaub reduzieren

Wenn abgelehnt:

1. `Status = abgelehnt`
2. Kein Kalendereintrag
3. Resturlaub bleibt unverändert

---

## 6. Verbindliches Datenmodell

Das Datenmodell besteht aus genau drei Tabellen:

1. `MITARBEITER`
2. `URLAUBSANTRAG`
3. `URLAUBSKALENDER`

Codex darf keine weiteren Tabellen ergänzen.

---

## 7. Tabelle: MITARBEITER

### 7.1 Attribute

| Datentyp | Attribut | Schlüssel | Hinweis |
|---|---|---|---|
| `int` | `Mitarbeiter_ID` | PK | `AUTO_INCREMENT` |
| `string` | `Vorname` |  | Vorname |
| `string` | `Nachname` |  | Nachname |
| `int` | `Resturlaub` |  | verbleibende Urlaubstage |
| `int` | `Vorgesetzter_ID` | FK | darf `NULL` sein |

### 7.2 Beziehung: Mitarbeiter hat Vorgesetzten

- Ein Mitarbeiter hat `0:1` Vorgesetzten.
- Ein Vorgesetzter kann `0:n` Mitarbeiter haben.
- Umsetzung über `Vorgesetzter_ID`.
- `Vorgesetzter_ID` darf `NULL` sein.

---

## 8. Tabelle: URLAUBSANTRAG

### 8.1 Attribute

| Datentyp | Attribut | Schlüssel | Hinweis |
|---|---|---|---|
| `int` | `Antrag_ID` | PK | `AUTO_INCREMENT` |
| `int` | `Starttag` |  | Beginn des Zeitraums |
| `int` | `Endtag` |  | Ende des Zeitraums |
| `string` | `Status` |  | `offen`, `genehmigt`, `abgelehnt` |
| `int` | `Antragsteller_ID` | FK | verweist auf `MITARBEITER` |
| `int` | `Vertretung_ID` | FK | verweist auf `MITARBEITER` |
| `int` | `Genehmiger_ID` | FK | verweist auf `MITARBEITER`, darf `NULL` sein |

### 8.2 Statuswerte

Verbindlich:

```text
offen
genehmigt
abgelehnt
```

In Java sollen Konstanten verwendet werden:

```java
public static final String STATUS_OFFEN = "offen";
public static final String STATUS_GENEHMIGT = "genehmigt";
public static final String STATUS_ABGELEHNT = "abgelehnt";
```

Keine eigene Status-Tabelle anlegen.

---

## 9. Tabelle: URLAUBSKALENDER

### 9.1 Attribute

| Datentyp | Attribut | Schlüssel | Hinweis |
|---|---|---|---|
| `int` | `Kalender_ID` | PK | `AUTO_INCREMENT` |
| `int` | `Mitarbeiter_ID` | FK | verweist auf `MITARBEITER` |
| `int` | `Starttag` |  | Beginn des Urlaubs |
| `int` | `Endtag` |  | Ende des Urlaubs |

### 9.2 Fachliche Bedeutung

`URLAUBSKALENDER` enthält nur genehmigte Urlaube.

Bei offenen oder abgelehnten Anträgen wird kein Kalendereintrag erstellt.

---

## 10. MariaDB-Schema

Dieses Schema bildet euer Datenmodell inklusive der finalen Entscheidungen ab.

```sql
CREATE TABLE mitarbeiter (
    mitarbeiter_id INT NOT NULL AUTO_INCREMENT,
    vorname VARCHAR(100) NOT NULL,
    nachname VARCHAR(100) NOT NULL,
    resturlaub INT NOT NULL,
    vorgesetzter_id INT NULL,
    PRIMARY KEY (mitarbeiter_id),
    FOREIGN KEY (vorgesetzter_id) REFERENCES mitarbeiter(mitarbeiter_id)
);

CREATE TABLE urlaubsantrag (
    antrag_id INT NOT NULL AUTO_INCREMENT,
    starttag INT NOT NULL,
    endtag INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    antragsteller_id INT NOT NULL,
    vertretung_id INT NOT NULL,
    genehmiger_id INT NULL,
    PRIMARY KEY (antrag_id),
    FOREIGN KEY (antragsteller_id) REFERENCES mitarbeiter(mitarbeiter_id),
    FOREIGN KEY (vertretung_id) REFERENCES mitarbeiter(mitarbeiter_id),
    FOREIGN KEY (genehmiger_id) REFERENCES mitarbeiter(mitarbeiter_id)
);

CREATE TABLE urlaubskalender (
    kalender_id INT NOT NULL AUTO_INCREMENT,
    mitarbeiter_id INT NOT NULL,
    starttag INT NOT NULL,
    endtag INT NOT NULL,
    PRIMARY KEY (kalender_id),
    FOREIGN KEY (mitarbeiter_id) REFERENCES mitarbeiter(mitarbeiter_id)
);
```

Wichtig:

- `Genehmiger_ID` ist `NULL` erlaubt, weil Mitarbeiter ohne Vorgesetzten existieren können.
- `Vertretung_ID` ist nicht `NULL`, weil eine Vertretung angegeben werden soll.
- `AUTO_INCREMENT` wird für alle Primärschlüssel verwendet.

---

## 11. Nebenbedingung: Genehmiger ist direkter Vorgesetzter

Diese Regel ist verbindlich:

```text
Genehmiger_ID darf nur auf den direkten Vorgesetzten des Antragstellers verweisen.
```

Das bedeutet:

```text
URLAUBSANTRAG.Genehmiger_ID == MITARBEITER.Vorgesetzter_ID des Antragstellers
```

Ausnahme:

Wenn der Antragsteller keinen Vorgesetzten hat, ist `Genehmiger_ID = NULL` erlaubt.

Diese Prüfung erfolgt im `UrlaubsService`.

---

## 12. Wochenenden zählen nicht als Urlaubstage

Wochenenden werden nicht vom Resturlaub abgezogen.

Da `Starttag` und `Endtag` im Datenmodell als `int` modelliert sind, braucht die Umsetzung eine einfache Regel, wie aus einer Zahl ein Wochentag abgeleitet wird.

Einfache empfohlene Regel für Anfänger-Code:

```text
Tag 1 = Montag
Tag 2 = Dienstag
Tag 3 = Mittwoch
Tag 4 = Donnerstag
Tag 5 = Freitag
Tag 6 = Samstag
Tag 7 = Sonntag
Tag 8 = Montag
...
```

Dann gilt:

```java
private boolean istWochenende(int tag) {
    int wochentag = tag % 7;
    return wochentag == 6 || wochentag == 0;
}
```

Berechnung der Urlaubstage:

```java
private int berechneUrlaubstage(int starttag, int endtag) {
    int urlaubstage = 0;

    for (int tag = starttag; tag <= endtag; tag++) {
        if (!istWochenende(tag)) {
            urlaubstage++;
        }
    }

    return urlaubstage;
}
```

Offene Mini-Rückfrage:

Diese Regel setzt voraus, dass `Starttag = 1` ein Montag ist. Falls das nicht gewünscht ist, muss eine andere einfache Regel festgelegt werden.

---

## 13. Verbindliches Schichtenmodell

Die Anwendung besteht aus drei Schichten:

```text
1. Präsentationsschicht
2. Logikschicht / Anwendungsschicht
3. Datenschicht / Datenzugriffsschicht
```

DTOs und Datenobjekte sind keine eigene Schicht.

---

## 14. Schicht 1: Präsentationsschicht

### 14.1 Klasse

```text
ConsoleUI
```

### 14.2 Aufgaben

Die `ConsoleUI` darf:

- Eingaben lesen
- Ergebnisse anzeigen
- offene Anträge anzeigen
- über RMI das Interface `IUrlaubsService` aufrufen

### 14.3 Nicht erlaubt

Die `ConsoleUI` darf nicht:

- SQL ausführen
- DAOs direkt nutzen
- direkt auf MariaDB zugreifen
- selbst entscheiden, ob ein Antrag genehmigt wird
- selbst Resturlaub ändern
- selbst Kalendereinträge schreiben

---

## 15. RMI zwischen Präsentationsschicht und Logikschicht

RMI ist wichtig und soll umgesetzt werden, aber einfach.

### 15.1 Ziel

Die Präsentationsschicht ruft die Logikschicht über RMI auf.

```text
ConsoleUI  --RMI-->  IUrlaubsService / UrlaubsService  -->  DAOs  --JDBC--> MariaDB
```

### 15.2 Einfache RMI-Struktur

Benötigte Bestandteile:

```text
IUrlaubsService       Remote-Interface
UrlaubsService        Implementierung, extends UnicastRemoteObject
UrlaubsServer         startet/bindet den RMI-Service
ConsoleUI             RMI-Client, ruft den Service auf
```

### 15.3 IUrlaubsService als Remote Interface

```java
public interface IUrlaubsService extends java.rmi.Remote {
    ErgebnisDTO urlaubBeantragen(UrlaubsantragEingabeDTO eingabe) throws java.rmi.RemoteException;
    ErgebnisDTO vorgesetztenentscheidungVerarbeiten(EntscheidungDTO entscheidung) throws java.rmi.RemoteException;
    List<UrlaubsantragDTO> offeneAntraegeAnzeigen() throws java.rmi.RemoteException;
}
```

Wichtig:

- Jede RMI-Methode wirft `RemoteException`.
- DTOs, die über RMI übertragen werden, müssen `Serializable` implementieren.

### 15.4 UrlaubsService als einfache RMI-Implementierung

```java
public class UrlaubsService extends java.rmi.server.UnicastRemoteObject implements IUrlaubsService {

    public UrlaubsService() throws java.rmi.RemoteException {
        super();
    }

    // Methoden aus IUrlaubsService hier implementieren
}
```

### 15.5 UrlaubsServer

Der Server startet den Service und bindet ihn an die RMI-Registry.

Einfache Anfänger-Variante:

```java
public class UrlaubsServer {
    public static void main(String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            IUrlaubsService service = new UrlaubsService();
            java.rmi.Naming.rebind("rmi://localhost/UrlaubsService", service);
            System.out.println("UrlaubsService gestartet.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 15.6 ConsoleUI als RMI-Client

Die `ConsoleUI` holt sich den Service über RMI:

```java
IUrlaubsService service = (IUrlaubsService) java.rmi.Naming.lookup("rmi://localhost/UrlaubsService");
```

Danach ruft die UI nur Methoden des Service auf.

### 15.7 Wichtig für Einfachheit

Nicht einbauen:

- komplexe RMI-Security-Policies
- dynamische Host-Konfiguration
- mehrere Server
- komplexe Thread-Verwaltung
- eigene Netzwerkprotokolle

Nur einfache lokale RMI-Nutzung auf `localhost`.

---

## 16. Schicht 2: Logikschicht / Anwendungsschicht

### 16.1 Interface

```text
IUrlaubsService
```

Das Interface ist gleichzeitig die Schnittstelle zwischen UI und Logik sowie das RMI-Remote-Interface.

### 16.2 Implementierung

```text
UrlaubsService
```

### 16.3 Aufgaben

Der `UrlaubsService` ist zuständig für:

- Resturlaub prüfen
- Vertretung prüfen
- direkten Vorgesetzten prüfen
- Vorgesetztenentscheidung verarbeiten
- Antrag genehmigen
- Antrag ablehnen
- Kalender und Resturlaub koordinieren

### 16.4 Wichtigste Regel

Nur der `UrlaubsService` trifft fachliche Entscheidungen.

Nicht die UI.

Nicht die DAOs.

---

## 17. Schicht 3: Datenschicht / Datenzugriffsschicht

### 17.1 Interfaces und Implementierungen

```text
IUrlaubsantragDAO  → UrlaubsantragDAO
IMitarbeiterDAO    → MitarbeiterDAO
IKalenderDAO       → KalenderDAO
```

### 17.2 Aufgabe

Die DAO-Schicht kapselt alle Datenbankzugriffe mit JDBC.

### 17.3 Erlaubt

Die DAO-Schicht darf:

- `Connection` nutzen
- SQL ausführen
- `PreparedStatement` nutzen
- `ResultSet` auslesen
- Daten speichern
- Daten aktualisieren
- Daten aus MariaDB in Java-Objekte umwandeln

### 17.4 Nicht erlaubt

Die DAO-Schicht darf nicht:

- fachlich entscheiden, ob Urlaub genehmigt wird
- Konsolenausgaben als normale Programmlogik erzeugen
- direkt mit der UI kommunizieren

---

## 18. DAO-Interfaces

### 18.1 IUrlaubsantragDAO

Da `Antrag_ID` per `AUTO_INCREMENT` erzeugt wird, gibt `speichern(...)` die neue ID zurück.

```java
public interface IUrlaubsantragDAO {
    int speichern(Urlaubsantrag antrag);
    Urlaubsantrag findeNachId(int antragId);
    List<Urlaubsantrag> findeOffeneAntraege();
    void statusAktualisieren(int antragId, String status);
}
```

### 18.2 IMitarbeiterDAO

```java
public interface IMitarbeiterDAO {
    Mitarbeiter findeNachId(int mitarbeiterId);
    void resturlaubAktualisieren(int mitarbeiterId, int neuerResturlaub);
}
```

### 18.3 IKalenderDAO

Da `Kalender_ID` per `AUTO_INCREMENT` erzeugt wird, kann `speichern(...)` die ID zurückgeben.

```java
public interface IKalenderDAO {
    int speichern(Kalendereintrag eintrag);
    boolean hatUeberschneidung(int mitarbeiterId, int starttag, int endtag);
}
```

---

## 19. DTOs

DTOs dienen der Datenübertragung zwischen Schichten.

Sie enthalten keine Fachlogik.

Wegen RMI müssen DTOs `Serializable` implementieren.

Verbindliche DTOs:

```text
UrlaubsantragEingabeDTO
EntscheidungDTO
ErgebnisDTO
UrlaubsantragDTO
```

### 19.1 UrlaubsantragEingabeDTO

```java
public class UrlaubsantragEingabeDTO implements java.io.Serializable {
    private int antragstellerId;
    private int vertretungId;
    private int starttag;
    private int endtag;

    // Konstruktoren, Getter, Setter
}
```

### 19.2 EntscheidungDTO

```java
public class EntscheidungDTO implements java.io.Serializable {
    private int antragId;
    private boolean genehmigt;

    // Konstruktoren, Getter, Setter
}
```

### 19.3 ErgebnisDTO

```java
public class ErgebnisDTO implements java.io.Serializable {
    private boolean erfolgreich;
    private String status;
    private String meldung;
    private Integer antragId;

    // Konstruktoren, Getter, Setter
}
```

### 19.4 UrlaubsantragDTO

```java
public class UrlaubsantragDTO implements java.io.Serializable {
    private int antragId;
    private int starttag;
    private int endtag;
    private String status;
    private int antragstellerId;
    private int vertretungId;
    private Integer genehmigerId;

    // Konstruktoren, Getter, Setter
}
```

---

## 20. Datenobjekte

Datenobjekte aus eurem Schichtenmodell:

```text
Mitarbeiter
Urlaubsantrag
Kalendereintrag
```

Wegen RMI und einfacher Wiederverwendbarkeit können auch diese Klassen `Serializable` implementieren.

### 20.1 Mitarbeiter

```java
public class Mitarbeiter implements java.io.Serializable {
    private int mitarbeiterId;
    private String vorname;
    private String nachname;
    private int resturlaub;
    private Integer vorgesetzterId;

    // Konstruktoren, Getter, Setter
}
```

### 20.2 Urlaubsantrag

```java
public class Urlaubsantrag implements java.io.Serializable {
    private int antragId;
    private int starttag;
    private int endtag;
    private String status;
    private int antragstellerId;
    private int vertretungId;
    private Integer genehmigerId;

    // Konstruktoren, Getter, Setter
}
```

### 20.3 Kalendereintrag

```java
public class Kalendereintrag implements java.io.Serializable {
    private int kalenderId;
    private int mitarbeiterId;
    private int starttag;
    private int endtag;

    // Konstruktoren, Getter, Setter
}
```

---

## 21. Fachlogik im UrlaubsService

### 21.1 Urlaub beantragen

Ablauf in `urlaubBeantragen(...)`:

```text
1. Eingabedaten prüfen.
2. Antragsteller laden.
3. Vertretung laden.
4. Prüfen: Antragsteller != Vertretung.
5. Urlaubstage ohne Wochenenden berechnen.
6. Resturlaub prüfen.
7. Direkten Vorgesetzten bestimmen.
8. Genehmiger_ID = Vorgesetzter_ID des Antragstellers, kann NULL sein.
9. Prüfen, ob Vertretung verfügbar ist.
10. Wenn Vertretung verfügbar:
    - Antrag mit Status genehmigt speichern.
    - Kalendereintrag speichern.
    - Resturlaub reduzieren.
11. Wenn Vertretung nicht verfügbar und Vorgesetzter vorhanden:
    - Antrag mit Status offen speichern.
12. Wenn Vertretung nicht verfügbar und kein Vorgesetzter vorhanden:
    - Antrag ablehnen oder Fehlermeldung zurückgeben.
```

### 21.2 Vorgesetztenentscheidung verarbeiten

Ablauf in `vorgesetztenentscheidungVerarbeiten(...)`:

```text
1. Antrag laden.
2. Prüfen, ob Antrag existiert.
3. Prüfen, ob Status offen ist.
4. Antragsteller laden.
5. Prüfen, ob Genehmiger_ID dem direkten Vorgesetzten entspricht.
6. Wenn Entscheidung genehmigt:
   - Status auf genehmigt setzen.
   - Kalendereintrag erstellen.
   - Resturlaub reduzieren.
7. Wenn Entscheidung abgelehnt:
   - Status auf abgelehnt setzen.
   - Kein Kalendereintrag.
   - Resturlaub bleibt gleich.
```

---

## 22. Überschneidungsprüfung

Eine Überschneidung liegt vor, wenn gilt:

```text
start1 <= end2 UND end1 >= start2
```

Diese Regel wird genutzt für:

- Prüfung, ob der Antragsteller schon Urlaub im Zeitraum hat.
- Prüfung, ob die Vertretung im Zeitraum verfügbar ist.

Mögliche SQL-Bedingung:

```sql
SELECT COUNT(*)
FROM urlaubskalender
WHERE mitarbeiter_id = ?
  AND starttag <= ?
  AND endtag >= ?;
```

Parameter:

```text
mitarbeiter_id = gesuchter Mitarbeiter
zweites ? = gewünschter Endtag
drittes ? = gewünschter Starttag
```

Wenn `COUNT(*) > 0`, gibt es eine Überschneidung.

---

## 23. Konsistenz bei Genehmigung

Bei einer Genehmigung gehören diese Schritte zusammen:

1. Antrag auf `genehmigt` setzen.
2. Kalendereintrag erstellen.
3. Resturlaub reduzieren.

Diese Schritte sollten möglichst nicht teilweise erfolgreich sein.

Für eine einfache Anfängerumsetzung gibt es zwei Möglichkeiten:

### Variante A: Einfach halten

Jede DAO-Methode führt ihre eigene SQL-Anweisung aus.

Vorteil:

- Einfacher zu verstehen.

Nachteil:

- Wenn ein Schritt fehlschlägt, kann ein inkonsistenter Zwischenstand entstehen.

### Variante B: Einfache Transaktion

Der Service oder eine Hilfsmethode nutzt eine gemeinsame `Connection` und setzt:

```java
connection.setAutoCommit(false);
```

Nach allen Schritten:

```java
connection.commit();
```

Bei Fehler:

```java
connection.rollback();
```

Empfehlung:

> Wenn Transaktionen im Unterricht noch nicht behandelt wurden, Variante A nutzen und den Code einfach halten. Wenn Datenkonsistenz stärker bewertet wird, Variante B nutzen, aber sehr einfach kommentieren.

---

## 24. Projektstruktur

Einfache Projektstruktur passend zu Schichtenmodell, RMI und JDBC:

```text
src/
  client/
    ClientMain.java

  server/
    UrlaubsServer.java

  presentation/
    ConsoleUI.java

  service/
    IUrlaubsService.java
    UrlaubsService.java

  dao/
    IUrlaubsantragDAO.java
    IMitarbeiterDAO.java
    IKalenderDAO.java
    UrlaubsantragDAO.java
    MitarbeiterDAO.java
    KalenderDAO.java
    DatabaseConnection.java

  dto/
    UrlaubsantragEingabeDTO.java
    EntscheidungDTO.java
    ErgebnisDTO.java
    UrlaubsantragDTO.java

  model/
    Mitarbeiter.java
    Urlaubsantrag.java
    Kalendereintrag.java
```

Wichtig:

- `client` und `server` sind technische Startpunkte, keine zusätzlichen fachlichen Schichten.
- `ConsoleUI` gehört zur Präsentationsschicht.
- `UrlaubsService` gehört zur Logikschicht.
- DAOs gehören zur Datenzugriffsschicht.
- DTOs und Models sind Hilfsklassen, keine eigene Schicht.

---

## 25. Coding-Stil

Der Code soll einfach und anfängerfreundlich sein.

### 25.1 Gewünscht

- kurze Methoden
- klare Namen
- einfache Klassen
- normale `for`-Schleifen
- normale `if`-Abfragen
- einfache `try/catch`-Blöcke
- Getter und Setter
- verständliche Kommentare bei RMI und JDBC
- möglichst wenig Magie

### 25.2 Vermeiden

- Streams und Lambdas, wenn sie den Code schwerer verständlich machen
- Dependency Injection Frameworks
- Factories
- Builder Pattern
- komplexe generische Typen
- unnötige Interfaces außerhalb des Schichtenmodells
- Reflection
- Annotationen, außer absolut nötig
- Spring
- Hibernate
- JPA
- REST
- Webserver

---

## 26. Nicht-Ziele

Nicht einbauen:

- Login-System
- Rollen- und Rechtesystem
- Web-App
- REST-API
- E-Mail-Benachrichtigung
- Kalenderexport
- Feiertagslogik
- Abteilungen
- Projektteams
- mehrere Genehmigungsstufen
- Vertreterlisten
- Historisierung
- Audit-Logging
- ORM-Framework
- Spring Boot
- Hibernate
- JPA

---

## 27. Akzeptanzkriterien

### Szenario 1: Vertretung verfügbar

1. Mitarbeiter beantragt Urlaub.
2. Vertretung ist angegeben und hat keine Überschneidung im Kalender.
3. Antrag wird automatisch genehmigt.
4. `Genehmiger_ID` wird auf `Vorgesetzter_ID` gesetzt oder bleibt `NULL`, wenn kein Vorgesetzter existiert.
5. Kalendereintrag wird erstellt.
6. Resturlaub wird um die Urlaubstage ohne Wochenenden reduziert.

### Szenario 2: Vertretung nicht verfügbar, Vorgesetzter vorhanden

1. Mitarbeiter beantragt Urlaub.
2. Vertretung ist nicht verfügbar.
3. Antrag wird mit `Status = offen` gespeichert.
4. `Genehmiger_ID = Vorgesetzter_ID`.
5. Offener Antrag kann in der `ConsoleUI` angezeigt werden.
6. Vorgesetzter entscheidet später.

### Szenario 3: Offener Antrag wird genehmigt

1. Offener Antrag wird ausgewählt.
2. Entscheidung lautet genehmigen.
3. Status wird `genehmigt`.
4. Kalendereintrag wird erstellt.
5. Resturlaub wird reduziert.

### Szenario 4: Offener Antrag wird abgelehnt

1. Offener Antrag wird ausgewählt.
2. Entscheidung lautet ablehnen.
3. Status wird `abgelehnt`.
4. Kein Kalendereintrag wird erstellt.
5. Resturlaub bleibt gleich.

### Szenario 5: Mitarbeiter ohne Vorgesetzten mit verfügbarer Vertretung

1. Mitarbeiter ohne Vorgesetzten beantragt Urlaub.
2. Vertretung ist verfügbar.
3. Antrag wird automatisch genehmigt.
4. `Genehmiger_ID = NULL`.
5. Kalendereintrag wird erstellt.
6. Resturlaub wird reduziert.

### Szenario 6: Mitarbeiter ohne Vorgesetzten ohne verfügbare Vertretung

1. Mitarbeiter ohne Vorgesetzten beantragt Urlaub.
2. Vertretung ist nicht verfügbar.
3. Antrag kann nicht durch Vorgesetzten entschieden werden.
4. Ergebnis ist Ablehnung oder Fehlermeldung.

---

## 28. Finale Wochenend-Regel

Diese technische Regel ist final entschieden:

```text
Tag 1 = Montag
Tag 2 = Dienstag
Tag 3 = Mittwoch
Tag 4 = Donnerstag
Tag 5 = Freitag
Tag 6 = Samstag
Tag 7 = Sonntag
Tag 8 = Montag
...
```

Wochenenden sind also:

```text
Tag 6, 7, 13, 14, 20, 21, ...
```

Java-Regel:

```java
private boolean istWochenende(int tag) {
    int wochentag = tag % 7;
    return wochentag == 6 || wochentag == 0;
}
```

Diese Wochenendtage werden nicht vom `Resturlaub` abgezogen.

---

## 29. Arbeitsanweisung für Codex

Vor jeder Codeänderung prüfen:

1. Passt die Änderung zum Prozessmodell?
2. Passt die Änderung zum Datenmodell?
3. Passt die Änderung zum Schichtenmodell?
4. Bleibt die Fachlogik im `UrlaubsService`?
5. Bleibt SQL in der DAO-Schicht?
6. Ruft die `ConsoleUI` nur über RMI den Service auf?
7. Bleibt der Code einfach genug für 2. Semester Wirtschaftsinformatik?

Wenn eine dieser Fragen nicht eindeutig mit „Ja“ beantwortet werden kann, muss Codex zuerst eine Rückfrage stellen.
