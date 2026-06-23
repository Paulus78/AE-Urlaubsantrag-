# AE-Projekt: Coding-Regeln aus den ANE2-Folien

Diese Datei ist als verbindliche Coding-Guideline für die Umsetzung des Anwendungsentwicklungs-Projekts gedacht. Sie ist bewusst umfassend formuliert und soll bei der Arbeit mit Codex/IDE als Projektregeldatei genutzt werden.

> Grundidee: Nicht alles in eine Klasse schreiben. Präsentation, fachliche Verarbeitung und Datenhaltung müssen klar getrennt bleiben. Java-Klassen, Collections, Exceptions, Interfaces und JDBC sollen so eingesetzt werden, wie es in den Folien vorgesehen ist.


---

## 0. Verwendung zusammen mit eurer eigenen Projektspezifikation

Dieses Dokument ist **keine fachliche Projektspezifikation**. Es beschreibt vor allem, **wie** der Code umgesetzt werden soll: Architekturregeln, Java-Regeln, Exceptions, Collections, JDBC und saubere Schichtentrennung.

Für euer konkretes Projekt soll zusätzlich eine eigene Datei verwendet werden, z. B.:

```text
PROJECT_SPEC.md
```

Diese projektspezifische Datei beschreibt, **was** konkret gebaut werden soll. Dort gehören eure eigenen Entscheidungen hinein:

- euer konkretes Drei-Schichtenmodell
- konkrete Packages und Klassen
- konkrete Service-/DAO-Interfaces
- konkrete Domain-Objekte
- konkrete Workflows/Use Cases
- konkretes Datenbankmodell
- konkrete Statuswerte, Validierungsregeln und Nebenbedingungen
- offene Annahmen und Entscheidungen

### 0.1 Reihenfolge für Codex / KI-Coding

Vor jeder Code-Änderung soll Codex die Dateien in dieser Reihenfolge berücksichtigen:

1. `PROJECT_SPEC.md` – fachliche und projektspezifische Vorgaben
2. `AE_Coding_Regeln_ANE2.md` – allgemeine Coding- und Architekturregeln aus den ANE2-Folien
3. vorhandener Quellcode – bestehende Struktur beibehalten, sofern sie nicht gegen die Regeln verstößt

### 0.2 Rollen der beiden Markdown-Dateien

| Datei | Zweck | Inhalt | Stabilität |
|---|---|---|---|
| `AE_Coding_Regeln_ANE2.md` | Allgemeine Coding-Regeln | Professor-Regeln, Java-Stil, Schichtenprinzip, Exceptions, Collections, JDBC | eher stabil |
| `PROJECT_SPEC.md` | Konkrete Projektvorgaben | euer Drei-Schichtenmodell, Klassen, Datenmodell, Workflows, fachliche Regeln | wird während des Projekts laufend angepasst |

### 0.3 Konfliktregel

Wenn `PROJECT_SPEC.md` und diese Coding-Regeln scheinbar widersprechen:

- Codex soll **nicht einfach frei entscheiden**.
- Fachliche Details aus `PROJECT_SPEC.md` haben Vorrang, solange sie nicht gegen die Professor-/Architekturregeln verstoßen.
- Die allgemeinen Regeln aus dieser Datei haben Vorrang bei Codequalität, Schichtentrennung, Java-Umsetzung, Collections, Exceptions und JDBC.
- Bei echten Konflikten soll Codex eine kurze Rückfrage stellen oder den Konflikt als TODO markieren.

Beispiel:

```text
PROJECT_SPEC.md sagt: Es gibt einen UrlaubsService mit Methode stelleAntrag(...).
AE_Coding_Regeln_ANE2.md sagt: Fachlogik gehört in die Anwendungsschicht.
=> Umsetzung: UrlaubsService liegt in der Anwendungsschicht und enthält die fachliche Logik.
```

### 0.4 Empfehlung für ein zusätzliches AGENTS.md

Falls ihr mit Codex arbeitet, kann zusätzlich eine kurze `AGENTS.md` im Projektroot sinnvoll sein:

```md
# AGENTS.md

Bitte vor jeder Codeänderung lesen:

1. PROJECT_SPEC.md
2. AE_Coding_Regeln_ANE2.md

Setze die fachlichen Vorgaben aus PROJECT_SPEC.md um und halte dabei strikt die Coding- und Architekturregeln aus AE_Coding_Regeln_ANE2.md ein.
Keine Monolith-Klassen erzeugen. JDBC nur in der Datenhaltung. Fachlogik nur in der Anwendungsschicht.
```

---

## 1. Architektur-Regeln: Drei-Schichten-Modell

### 1.1 Verbindliche Schichten

Das Projekt soll logisch in drei Schichten umgesetzt werden:

1. **Präsentationsschicht**  
   Verantwortlich für Benutzerinteraktion, Ein- und Ausgabe, Menüs, Konsolen- oder GUI-Ausgabe, Anzeige von Ergebnissen und Fehlermeldungen.

2. **Anwendungsschicht / Verarbeitungsschicht / Business Layer**  
   Verantwortlich für die komplette fachliche Verarbeitung, Validierungen, Entscheidungen, Regeln und Prozesslogik.

3. **Datenhaltungsschicht / Data Management Layer / Storage Layer**  
   Verantwortlich für dauerhaftes Speichern, Lesen, Ändern und Löschen von Daten, z. B. über MariaDB/JDBC.

### 1.2 Strikte Abhängigkeitsregel

Eine höhere Schicht darf nur die direkt darunterliegende Schicht benutzen.

Erlaubt:

```text
Präsentation -> Anwendungsschicht -> Datenhaltungsschicht -> Datenbank
```

Nicht erlaubt:

```text
Präsentation -> Datenbank
Präsentation -> JDBC
Präsentation -> DAO-Implementierung direkt, wenn ein Service vorgesehen ist
Datenhaltung -> Präsentation
Datenhaltung -> Benutzerabfragen
Anwendungsschicht -> System.out als fachliche Ausgabe-Logik
```

### 1.3 Aufgaben der Präsentationsschicht

Die Präsentationsschicht darf:

- Benutzereingaben entgegennehmen.
- Eingaben grob auf Format prüfen, z. B. ob ein Menüpunkt eine Zahl ist.
- Methoden der Anwendungsschicht aufrufen.
- Ergebnisse anzeigen.
- Fehlermeldungen ausgeben, wenn Exceptions aus unteren Schichten zurückkommen.
- Den Prozess aus der Aufgabenstellung mit Ereignissen und Schritten abbilden.

Die Präsentationsschicht darf nicht:

- SQL-Anweisungen enthalten.
- JDBC-Klassen wie `Connection`, `Statement` oder `ResultSet` verwenden.
- Direkt auf Tabellen zugreifen.
- Fachliche Regeln entscheiden, die eigentlich zur Anwendungsschicht gehören.
- Daten dauerhaft speichern.

Beispiel für erlaubte Struktur:

```java
public class ConsoleUi {
    private final UrlaubsService urlaubsService;

    public ConsoleUi(UrlaubsService urlaubsService) {
        this.urlaubsService = urlaubsService;
    }

    public void starte() {
        // Eingaben lesen
        // Service-Methode aufrufen
        // Ergebnis anzeigen
    }
}
```

### 1.4 Aufgaben der Anwendungsschicht

Die Anwendungsschicht enthält die fachlichen Funktionen, die der Prozess benötigt.

Sie darf:

- Fachliche Anforderungen umsetzen.
- Geschäftsregeln prüfen.
- Entscheidungen treffen.
- Daten aus der Präsentation entgegennehmen und fachlich verarbeiten.
- Datenhaltung über DAO-/Repository-Schnittstellen nutzen.
- Exceptions werfen, wenn fachliche Regeln verletzt werden.
- Ergebnisse an die Präsentationsschicht zurückgeben.

Sie darf nicht:

- Konsolenmenüs oder GUI-Code enthalten.
- Direkt mit dem Benutzer interagieren.
- SQL direkt in Fachmethoden mischen, wenn eine Datenhaltungsschicht vorgesehen ist.
- `ResultSet` oder andere JDBC-Details an die Präsentationsschicht weiterreichen.

Beispiel:

```java
public class UrlaubsService {
    private final UrlaubsantragDao urlaubsantragDao;
    private final MitarbeiterDao mitarbeiterDao;

    public UrlaubsService(UrlaubsantragDao urlaubsantragDao, MitarbeiterDao mitarbeiterDao) {
        this.urlaubsantragDao = urlaubsantragDao;
        this.mitarbeiterDao = mitarbeiterDao;
    }

    public Ergebnis stelleAntrag(Urlaubsantrag antrag) throws FachlicheException {
        // fachliche Prüfung
        // DAO-Aufruf
        // Ergebnis zurückgeben
        return new Ergebnis("offen");
    }
}
```

### 1.5 Aufgaben der Datenhaltungsschicht

Die Datenhaltungsschicht stellt nach oben Funktionen bereit, um Daten zu verwalten:

- Lesen
- Schreiben
- Ändern
- Löschen

Sie darf:

- JDBC verwenden.
- SQL-Anweisungen ausführen.
- Datenbankverbindungen öffnen.
- Ergebnisse aus `ResultSet` auslesen.
- Daten aus Tabellen in Java-Objekte umwandeln.
- Java-Objekte in Tabellenzeilen übersetzen.

Sie darf nicht:

- Benutzerinteraktion enthalten.
- Fachliche Entscheidungen treffen, die in die Anwendungsschicht gehören.
- UI-Ausgaben erzeugen.
- `ResultSet` nach oben durchreichen.

Beispiel:

```java
public interface MitarbeiterDao {
    Mitarbeiter findeNachId(int id) throws DatenzugriffException;
    void speichere(Mitarbeiter mitarbeiter) throws DatenzugriffException;
}
```

```java
public class JdbcMitarbeiterDao implements MitarbeiterDao {
    private final Connection connection;

    public JdbcMitarbeiterDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Mitarbeiter findeNachId(int id) throws DatenzugriffException {
        // SQL ausführen
        // ResultSet auswerten
        // Mitarbeiter-Objekt zurückgeben
        return null;
    }
}
```

### 1.6 Schnittstellen zwischen den Schichten

Für die Schichten sollen klare Schnittstellen definiert werden.

Empfohlene Umsetzung:

```text
presentation
  -> nutzt Service-Interfaces oder Service-Klassen

application/service
  -> nutzt DAO-Interfaces

data/dao
  -> implementiert DAO-Interfaces mit JDBC
```

Bei verteilter Umsetzung kann die Verbindung zwischen Präsentationsschicht und Anwendungsschicht per RMI realisiert werden. Wenn RMI im Projekt nicht praktisch umgesetzt wird, soll die Idee trotzdem durch klar definierte Service-Schnittstellen abgebildet werden.

### 1.7 Warum diese Trennung wichtig ist

Die Schichtentrennung ist nicht nur „schön“, sondern soll konkret Folgendes ermöglichen:

- Wiederverwendung von Code, z. B. eine Datenverwaltung für mehrere Anwendungen.
- Schnellere Fehlersuche.
- Einfachere Erweiterungen.
- Entkopplung der Schichten.
- Änderungen in einer Schicht betreffen seltener andere Schichten.
- Skalierbarkeit durch mögliche Verteilung von Komponenten.
- Höhere Sicherheit durch einheitlichen Zugriff auf Daten.

### 1.8 Änderungen an Schnittstellen

Änderungen an Schnittstellen sind besonders kritisch, weil sie mehrere Schichten betreffen können.

Beispiel: Ein neues Pflichtfeld `kundennummer` muss eingeführt werden.

Dann müssen typischerweise angepasst werden:

- Präsentation: Eingabe erfassen.
- Anwendungsschicht: Fachlich prüfen und verarbeiten.
- Datenhaltung: Spalte speichern/lesen.
- Datenbank: Tabelle ändern.
- DTO/Domain-Klasse: Attribut ergänzen.

Regel: Änderungen an Schnittstellen immer bewusst und vollständig durch alle betroffenen Schichten ziehen.

### 1.9 Anti-Monolith-Regel

Nicht alles in eine Klasse schreiben.

Nicht erlaubt:

```java
public class Main {
    public static void main(String[] args) {
        // Menü anzeigen
        // Eingaben lesen
        // fachliche Prüfung
        // SQL-Statements
        // ResultSet auswerten
        // Ausgabe
    }
}
```

Das ist genau die Vermischung von Präsentation, Fachlogik und Datenhaltung, die vermieden werden soll.

---

## 2. Empfohlene Projektstruktur

Eine sinnvolle Struktur für das Projekt:

```text
src/
  main/
    java/
      de/gruppe/projekt/
        presentation/
          ConsoleUi.java
          Main.java

        application/
          service/
            UrlaubsService.java
            MitarbeiterService.java
          dto/
            ErgebnisDto.java

        domain/
          model/
            Mitarbeiter.java
            Urlaubsantrag.java
            UrlaubskalenderEintrag.java
          exception/
            FachlicheException.java
            UngueltigerParameterException.java

        persistence/
          dao/
            MitarbeiterDao.java
            UrlaubsantragDao.java
          jdbc/
            JdbcMitarbeiterDao.java
            JdbcUrlaubsantragDao.java
            DatabaseConnectionFactory.java
          exception/
            DatenzugriffException.java

        util/
          DateUtils.java
          InputValidator.java
```

Die Namen dürfen an euer Projekt angepasst werden. Wichtig ist die klare Trennung der Verantwortlichkeiten.

---

## 3. Klassen, Objekte und Attribute

### 3.1 Klassen modellieren fachliche Entitäten

Klassen sollen reale oder fachlich relevante Sachverhalte abstrahieren, z. B.:

- `Kunde`
- `Mitarbeiter`
- `Auto`
- `Urlaubsantrag`
- `Konto`
- `Transaktion`

Eine Klasse kann enthalten:

- Attribute
- Methoden
- Beziehungen zu anderen Klassen

### 3.2 Attribute müssen typisiert sein

Jedes Attribut braucht einen klaren Java-Typ.

Beispiele:

```java
private int id;
private String name;
private LocalDate startdatum;
private Mitarbeiter genehmiger;
private List<Urlaubsantrag> antraege;
```

### 3.3 Attribute grundsätzlich `private`

Attribute sollen grundsätzlich nicht direkt von außen verändert werden.

Regel:

```java
private String name;
```

Nicht:

```java
public String name;
```

Grund: Geheimnisprinzip. Der Zustand eines Objekts soll geschützt sein. Zugriff und Änderung erfolgen kontrolliert über Methoden.

### 3.4 Getter und Setter gezielt einsetzen

Getter und Setter sind erlaubt, aber nicht gedankenlos für jedes Attribut automatisch erzeugen.

Regeln:

- Getter sind sinnvoll, wenn andere Klassen den Wert lesen müssen.
- Setter sind sinnvoll, wenn der Wert nachträglich geändert werden darf.
- Wenn ein Wert nach Erzeugung unveränderlich bleiben soll, keinen Setter anbieten.
- Setter dürfen Validierung enthalten.

Beispiel:

```java
public void setLeistung(int leistung) throws UngueltigerParameterException {
    if (leistung < 0) {
        throw new UngueltigerParameterException("Leistung darf nicht negativ sein.");
    }
    this.leistung = leistung;
}
```

### 3.5 `protected` nur bewusst verwenden

`protected` erlaubt Zugriff aus Unterklassen. Es ist sinnvoll bei Vererbungshierarchien, wenn Unterklassen direkt auf gemeinsame Attribute zugreifen sollen.

Beispiel:

```java
public abstract class Fahrzeug {
    protected double gewicht;
}
```

Regel: `protected` nicht als Ersatz für sauberes Kapseln missbrauchen.

### 3.6 `public` ist die öffentliche Schnittstelle

`public` Methoden bilden die nutzbare Schnittstelle einer Klasse.

Regel:

- Public nur für Methoden/Klassen, die wirklich von außen genutzt werden sollen.
- Attribute nicht public machen.
- Hilfsmethoden eher `private` halten.

### 3.7 Statische Attribute

`static` Attribute gehören zur Klasse, nicht zu einem einzelnen Objekt.

Geeignet für:

- Zähler über alle Objekte einer Klasse.
- Gemeinsame Klassenwerte.
- Konstanten in Kombination mit `final`.

Beispiel:

```java
private static int anzahlErzeugterAutos = 0;
```

Regeln:

- Ein statisches Attribut existiert genau einmal pro Klasse.
- Es ist nicht objektindividuell.
- Es ist nicht automatisch konstant.
- Veränderliche statische Attribute sparsam einsetzen.

### 3.8 Konstanten mit `static final`

Konstanten sollen mit `final` deklariert und direkt initialisiert werden.

Beispiel:

```java
private static final double HORSEPOWERS_TO_KILOWATT = 0.735;
```

Regeln:

- Konstanten müssen initialisiert werden.
- Sie ändern sich zur Laufzeit nicht mehr.
- Meistens als `static final`, weil der Wert für alle Objekte gleich ist.
- Namen von Konstanten am besten in Großbuchstaben mit Unterstrichen schreiben.

---

## 4. Konstruktoren und Objekt-Erzeugung

### 4.1 Keine ungültigen Objekte erzeugen

Wenn ein Objekt mit ungültigen Werten erzeugt werden würde, soll keine halb kaputte Instanz entstehen.

Nicht so:

```java
public Auto(int leistung) {
    if (leistung < 0) {
        System.out.println("Falscher Parameter");
    }
    this.leistung = leistung; // Objekt kann trotzdem ungültig sein
}
```

Besser:

```java
public Auto(int leistung) throws UngueltigerParameterException {
    if (leistung < 0) {
        throw new UngueltigerParameterException("Leistung darf nicht negativ sein.");
    }
    this.leistung = leistung;
}
```

### 4.2 Konstruktoren validieren Pflichtwerte

Konstruktoren sollen prüfen:

- Sind Pflichtwerte vorhanden?
- Sind Zahlenbereiche gültig?
- Sind Zustände fachlich sinnvoll?
- Sind Beziehungen vollständig, wenn sie verpflichtend sind?

Beispiel:

```java
public Urlaubsantrag(Mitarbeiter antragsteller, Mitarbeiter vertretung, LocalDate start, LocalDate ende)
        throws UngueltigerParameterException {
    if (antragsteller == null) {
        throw new UngueltigerParameterException("Antragsteller fehlt.");
    }
    if (vertretung == null) {
        throw new UngueltigerParameterException("Vertretung fehlt.");
    }
    if (start == null || ende == null || ende.isBefore(start)) {
        throw new UngueltigerParameterException("Zeitraum ist ungültig.");
    }

    this.antragsteller = antragsteller;
    this.vertretung = vertretung;
    this.start = start;
    this.ende = ende;
}
```

### 4.3 Konstruktoren in Vererbungshierarchien mit `super(...)`

Wenn Klassen erben, soll gemeinsamer Initialisierungscode nicht mehrfach geschrieben werden.

Regel:

- Oberklasse initialisiert ihre eigenen Attribute.
- Unterklasse ruft `super(...)` auf.
- Unterklasse initialisiert nur ihre zusätzlichen Attribute.

Beispiel:

```java
public abstract class Fortbewegungsmittel {
    protected double gewicht;

    public Fortbewegungsmittel(double gewicht) {
        this.gewicht = gewicht;
    }
}

public abstract class Landfahrzeug extends Fortbewegungsmittel {
    protected int anzahlAchsen;

    public Landfahrzeug(double gewicht, int anzahlAchsen) {
        super(gewicht);
        this.anzahlAchsen = anzahlAchsen;
    }
}

public class Pkw extends Landfahrzeug {
    private int anzahlPlaetze;

    public Pkw(double gewicht, int anzahlAchsen, int anzahlPlaetze) {
        super(gewicht, anzahlAchsen);
        this.anzahlPlaetze = anzahlPlaetze;
    }
}
```

---

## 5. Methoden-Regeln

### 5.1 Methodensignatur bewusst definieren

Eine Methodensignatur besteht aus:

- Sichtbarkeit / Access Modifier
- Rückgabetyp
- Methodenname
- formalen Parametern mit Typ und Name

Beispiel:

```java
public void setFirstName(String firstName) {
    this.firstName = firstName;
}
```

### 5.2 Methoden gehören in die richtige Schicht

Beispiele:

| Methode | Richtige Schicht |
|---|---|
| `zeigeMenue()` | Präsentation |
| `stelleUrlaubsantrag(...)` | Anwendungsschicht |
| `pruefeResturlaub(...)` | Anwendungsschicht |
| `findeMitarbeiterNachId(...)` | Datenhaltung |
| `speichereUrlaubsantrag(...)` | Datenhaltung |
| `executeQuery(...)` | Datenhaltung |

### 5.3 `private` Hilfsmethoden verwenden

Komplexere öffentliche Methoden sollen interne Teilschritte an private Hilfsmethoden auslagern.

Beispiel:

```java
public Ergebnis genehmigeAntrag(int antragId, int genehmigerId) throws FachlicheException {
    Urlaubsantrag antrag = ladeAntrag(antragId);
    pruefeGenehmiger(antrag, genehmigerId);
    antrag.genehmigen();
    speichereAntrag(antrag);
    return new Ergebnis("genehmigt");
}

private void pruefeGenehmiger(Urlaubsantrag antrag, int genehmigerId) throws FachlicheException {
    // fachliche Prüfung
}
```

### 5.4 Statische Methoden

`static` Methoden gehören zur Klasse, nicht zu einem Objekt.

Regeln:

- Aufruf über `Klassenname.methodenName(...)`.
- Kein Zugriff auf nicht-statische Attribute eines konkreten Objekts.
- Sinnvoll für echte Hilfsfunktionen ohne Objektzustand.
- Nicht verwenden, um die Objektorientierung zu umgehen.

Beispiel:

```java
public final class DateUtils {
    private DateUtils() {
    }

    public static boolean istGueltigerZeitraum(LocalDate start, LocalDate ende) {
        return start != null && ende != null && !ende.isBefore(start);
    }
}
```

### 5.5 Finale Methoden

`final` Methoden können in Unterklassen nicht überschrieben werden.

Sinnvoll für:

- Sicherheitskritische Methoden.
- Methoden, deren Bedeutung nicht verändert werden darf.
- Zentrale Berechnungen, die konsistent bleiben müssen.

Beispiel:

```java
public final boolean authorisiereZugriff(int pin) {
    // darf in Unterklassen nicht verändert werden
    return pin > 0;
}
```

### 5.6 Abstrakte Methoden

Abstrakte Methoden:

- Haben keinen Methodenrumpf.
- Enden mit Semikolon.
- Müssen in konkreten Unterklassen implementiert werden.
- Bewirken, dass die Klasse abstrakt ist.

Beispiel:

```java
public abstract class Person {
    public abstract int getId();
}
```

---

## 6. Abstrakte Klassen, finale Klassen und Vererbung

### 6.1 Abstrakte Klassen sinnvoll einsetzen

Abstrakte Klassen sind sinnvoll, wenn eine Oberklasse gemeinsame Attribute oder Methoden bündelt, aber keine eigenen Objekte davon existieren sollen.

Beispiel:

```java
public abstract class Person {
    private int id;
    private String name;
}

public class Mitarbeiter extends Person {
}

public class Kunde extends Person {
}
```

Regeln:

- Keine Objekte von abstrakten Klassen erzeugen.
- Gemeinsame Logik in abstrakten Oberklassen bündeln.
- Konkrete Klassen für tatsächlich erzeugbare Objekte verwenden.

### 6.2 Vererbung nur verwenden, wenn sie fachlich passt

Vererbung ist nicht automatisch immer sinnvoll.

Verwenden, wenn gilt:

```text
Unterklasse IST eine spezielle Form der Oberklasse.
```

Beispiel:

```text
Pkw ist ein Straßenfahrzeug.
Mitarbeiter ist eine Person.
```

Nicht verwenden, wenn nur eine zufällige technische Ähnlichkeit besteht.

### 6.3 Finale Klassen

`final` Klassen können nicht weiter abgeleitet werden.

Sinnvoll, wenn:

- die Bedeutung einer Klasse nicht verändert werden soll,
- Vererbung fachlich unsinnig ist,
- Manipulation durch Überschreiben verhindert werden soll.

Beispiel:

```java
public final class Rolle {
    // keine Unterklassen erlaubt
}
```

---

## 7. Interfaces als Coding-Regel

### 7.1 Interfaces definieren nutzbare Funktionen

Ein Interface legt fest, **wie** eine Funktionalität genutzt wird, aber nicht, **wie** sie intern umgesetzt ist.

Für das Projekt heißt das:

- Präsentation soll gegen Service-Schnittstellen arbeiten können.
- Anwendungsschicht soll gegen DAO-Schnittstellen arbeiten können.
- Implementierungen können später ausgetauscht werden.

Beispiel:

```java
public interface UrlaubsantragDao {
    void speichere(Urlaubsantrag antrag) throws DatenzugriffException;
    Urlaubsantrag findeNachId(int id) throws DatenzugriffException;
}
```

```java
public class JdbcUrlaubsantragDao implements UrlaubsantragDao {
    @Override
    public void speichere(Urlaubsantrag antrag) throws DatenzugriffException {
        // JDBC-Code
    }

    @Override
    public Urlaubsantrag findeNachId(int id) throws DatenzugriffException {
        // JDBC-Code
        return null;
    }
}
```

### 7.2 Interface-Regeln in Java

Regeln:

- Methoden in Interfaces sind abstrakt; `abstract` kann geschrieben werden, ist aber nicht zwingend nötig.
- Attribute in Interfaces dürfen nur Konstanten sein.
- Interface-Konstanten sind `public static final`.
- Von einem Interface kann kein Objekt erzeugt werden.
- Eine Klasse implementiert Interfaces mit `implements`.
- Mehrere Interfaces werden mit Komma getrennt.
- Eine konkrete Klasse muss alle Interface-Methoden implementieren.
- Wenn sie nicht alle Methoden implementiert, muss die Klasse abstrakt bleiben.

Beispiel:

```java
public interface Bremsbar {
    void bremsen(double bremskraft);
}

public interface Blinkbar {
    void blinkenLinks();
    void blinkenRechts();
    void blinkenAus();
}

public class Auto implements Bremsbar, Blinkbar {
    @Override
    public void bremsen(double bremskraft) {
        // Umsetzung
    }

    @Override
    public void blinkenLinks() {
        // Umsetzung
    }

    @Override
    public void blinkenRechts() {
        // Umsetzung
    }

    @Override
    public void blinkenAus() {
        // Umsetzung
    }
}
```

### 7.3 Gegen Interface-Typen programmieren

Wenn eine Klasse ein Interface implementiert, kann sie überall verwendet werden, wo dieses Interface erwartet wird.

Regel:

```java
UrlaubsantragDao dao = new JdbcUrlaubsantragDao(connection);
```

statt überall direkt:

```java
JdbcUrlaubsantragDao dao = new JdbcUrlaubsantragDao(connection);
```

Vorteil: Implementierung austauschbar, Schichten bleiben entkoppelt.

### 7.4 Interfaces nicht mit abstrakten Klassen verwechseln

Für die Umsetzung:

| Situation | Besser |
|---|---|
| Gemeinsame Attribute oder gemeinsame implementierte Methoden | abstrakte Klasse |
| Nur Verhalten/Fähigkeit definieren | Interface |
| Mehrere Fähigkeiten kombinieren | mehrere Interfaces |
| Objekt darf nicht direkt erzeugt werden, aber gemeinsame Basislogik existiert | abstrakte Klasse |

---

## 8. Collections und Generics

### 8.1 Arrays nur verwenden, wenn Größe wirklich fest ist

Arrays haben eine feste Größe und bieten wenig Komfort für Einfügen, Löschen und Suchen.

Für variable Mengen von Objekten sollen Collections aus `java.util` verwendet werden.

Nicht ideal:

```java
Kunde[] kunden = new Kunde[10000];
```

Besser:

```java
List<Kunde> kunden = new ArrayList<>();
```

### 8.2 Java Collection Framework verwenden

Collections sind Objekte, die andere Objekte enthalten und verwalten können.

Typische Funktionen:

- Hinzufügen
- Löschen
- Prüfen, ob ein Objekt enthalten ist
- Anzahl Elemente bestimmen
- Alle Elemente durchlaufen

### 8.3 Collection-Typ passend auswählen

| Anforderung | Passender Typ |
|---|---|
| Reihenfolge der Objekte ist wichtig | `List` |
| Schneller Zugriff über Position/Index | `ArrayList` |
| Häufiges Einfügen/Löschen | `LinkedList` |
| Keine Duplikate, Reihenfolge unwichtig | `HashSet` |
| Geordnete Menge ohne Duplikate | `TreeSet` |
| Zugriff über eindeutigen Schlüssel | `Map` |
| Schlüssel/Wert-Paare, schneller Zugriff | `HashMap` |
| Sortierte Schlüssel | `TreeMap` |

### 8.4 List-Regeln

`List` eignet sich für geordnete Sammlungen.

Eigenschaften:

- Reihenfolge bleibt erhalten.
- Zugriff über Index möglich.
- Duplikate sind erlaubt.

Beispiel:

```java
List<Mitarbeiter> mitarbeiter = new ArrayList<>();
mitarbeiter.add(new Mitarbeiter(1, "Meier"));
Mitarbeiter erster = mitarbeiter.get(0);
```

### 8.5 Set-Regeln

`Set` eignet sich für Mengen ohne Duplikate.

Eigenschaften:

- Keine doppelten Elemente.
- Keine feste Position wie bei Listen.
- Bei `HashSet`: schneller Zugriff zur Existenzprüfung.
- Bei `TreeSet`: sortierte Menge, Ordnung muss definiert sein.

Wenn eigene Objekte in Sets verwendet werden, müssen Gleichheit und Hashing sauber definiert werden:

```java
@Override
public boolean equals(Object obj) {
    // fachliche Gleichheit prüfen
    return super.equals(obj);
}

@Override
public int hashCode() {
    // passend zu equals implementieren
    return super.hashCode();
}
```

Wenn eine Sortierung gebraucht wird, `Comparable` implementieren:

```java
public class Mitarbeiter implements Comparable<Mitarbeiter> {
    @Override
    public int compareTo(Mitarbeiter anderer) {
        return Integer.compare(this.id, anderer.id);
    }
}
```

### 8.6 Map-Regeln

`Map` eignet sich für Schlüssel/Wert-Paare.

Eigenschaften:

- Ein Schlüssel verweist auf einen Wert.
- Schlüssel müssen eindeutig sein.
- Schlüssel sollen nicht undefiniert sein.
- Doppelte Werte sind möglich.
- Zugriff erfolgt über den Schlüssel, nicht über eine Position.

Beispiel:

```java
Map<Integer, Mitarbeiter> mitarbeiterNachId = new HashMap<>();
mitarbeiterNachId.put(4711, new Mitarbeiter(4711, "Meyer"));

Mitarbeiter mitarbeiter = mitarbeiterNachId.get(4711);
```

Wichtige Methoden:

```java
put(key, value)
get(key)
containsKey(key)
containsValue(value)
remove(key)
size()
values()
keySet()
```

### 8.7 Immer Generics verwenden

Keine rohen Collections verwenden.

Nicht:

```java
Vector autos = new Vector();
autos.add(new Auto());
Auto auto = (Auto) autos.get(0);
```

Besser:

```java
List<Auto> autos = new ArrayList<>();
autos.add(new Auto());
Auto auto = autos.get(0);
```

Regeln:

- Immer Typ-Parameter setzen, z. B. `List<Auto>` oder `Map<Integer, Kunde>`.
- Dadurch prüft der Compiler den Typ bereits beim Übersetzen.
- Keine unnötigen Casts.
- Warnungen wie „Unsafe type operation“ vermeiden.

### 8.8 Iteratoren typisieren

Wenn ein Iterator verwendet wird, dann ebenfalls typisiert.

```java
Iterator<Auto> iterator = autos.iterator();
while (iterator.hasNext()) {
    Auto auto = iterator.next();
}
```

Nicht:

```java
Iterator iterator = autos.iterator();
Auto auto = (Auto) iterator.next();
```

### 8.9 For-each-Schleife verwenden, wenn nur durchlaufen wird

Für einfaches Durchlaufen ist die erweiterte for-Schleife kompakt und typsicher.

```java
for (Auto auto : autos) {
    System.out.println(auto.getKennzeichen());
}
```

### 8.10 Iterator verwenden, wenn die Collection keine Indexposition hat

`Set` und `Map` haben keinen normalen Indexzugriff.

Für `Set`:

```java
for (Mitarbeiter mitarbeiter : mitarbeiterSet) {
    // Verarbeitung
}
```

Für `Map` über Schlüssel:

```java
for (Integer id : mitarbeiterNachId.keySet()) {
    Mitarbeiter mitarbeiter = mitarbeiterNachId.get(id);
}
```

Für `Map` über Werte:

```java
for (Mitarbeiter mitarbeiter : mitarbeiterNachId.values()) {
    // Verarbeitung
}
```

---

## 9. Exception-Regeln

### 9.1 Exceptions statt ungültiger Zustände

Wenn ein Fehlerfall auftritt, soll er durch eine Exception signalisiert werden.

Nicht:

- Spezialwert zurückgeben, ohne dass klar ist, wie der Aufrufer reagieren soll.
- Ungültige Attribute setzen und weiterlaufen lassen.
- Nur `System.out.println("Fehler")` ausgeben und trotzdem ein kaputtes Objekt erzeugen.

Besser:

```java
throw new UngueltigerParameterException("Startdatum darf nicht nach Enddatum liegen.");
```

### 9.2 Checked vs. unchecked bewusst wählen

| Art | Basisklasse | Typischer Einsatz |
|---|---|---|
| Checked Exception | `Exception`, aber nicht `RuntimeException` | Fehler, die zur Laufzeit eventuell behandelbar sind, z. B. falsche Datei, falsche Eingabe, DB-Zugriff |
| Unchecked Exception | `RuntimeException` | Programmierfehler, die zur Laufzeit meist nicht sinnvoll behoben werden können |

Regeln:

- Checked Exceptions müssen behandelt oder weitergeleitet werden.
- Bei checked Exceptions muss `throws` in die Methodensignatur.
- Unchecked Exceptions müssen nicht deklariert werden.
- Nicht jede RuntimeException abfangen, nur damit das Programm „irgendwie weiterläuft“.

### 9.3 Eigene Exceptions definieren

Eigene Exceptions sollen sprechende Namen haben und von `Exception` oder `RuntimeException` erben.

Empfohlen: zwei Konstruktoren anbieten.

```java
public class UngueltigerParameterException extends Exception {
    public UngueltigerParameterException() {
        super("Ungültiger Parameterwert.");
    }

    public UngueltigerParameterException(String message) {
        super(message);
    }
}
```

Für Datenzugriffsfehler:

```java
public class DatenzugriffException extends Exception {
    public DatenzugriffException(String message) {
        super(message);
    }

    public DatenzugriffException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 9.4 Exceptions werfen

Beim Werfen einer Exception sind drei Schritte wichtig:

1. Bedingung formulieren.
2. Exception-Objekt erzeugen.
3. Exception mit `throw` werfen.

```java
if (leistung < 0) {
    throw new UngueltigerParameterException("Leistung darf nicht negativ sein.");
}
```

### 9.5 `throws` bei checked Exceptions

Wenn eine Methode eine checked Exception auslösen kann und sie nicht selbst behandelt, muss sie das deklarieren.

```java
public Auto(int leistung) throws UngueltigerParameterException {
    if (leistung < 0) {
        throw new UngueltigerParameterException("Leistung darf nicht negativ sein.");
    }
    this.leistung = leistung;
}
```

### 9.6 Exceptions fangen

Checked Exceptions müssen behandelt oder weitergeleitet werden.

```java
try {
    Auto auto = new Auto(-10);
} catch (UngueltigerParameterException e) {
    System.out.println(e.getMessage());
}
```

Regeln:

- Exceptions dort fangen, wo sinnvoll reagiert werden kann.
- Präsentation kann Fehlermeldungen anzeigen.
- Anwendungsschicht kann fachliche Fehler erzeugen oder weitergeben.
- Datenhaltung kann technische DB-Fehler in eigene Datenzugriff-Exceptions übersetzen.
- Nicht leere `catch`-Blöcke schreiben.

Nicht:

```java
catch (Exception e) {
}
```

### 9.7 `finally`

`finally` ist optional, wird aber immer ausgeführt, egal ob eine Exception aufgetreten ist oder nicht.

Geeignet für Aufräumarbeiten.

```java
try {
    // riskanter Code
} catch (Exception e) {
    // Fehler behandeln
} finally {
    // wird immer ausgeführt
}
```

---

## 10. JDBC- und Datenbank-Regeln

### 10.1 JDBC gehört ausschließlich in die Datenhaltungsschicht

JDBC-Klassen dürfen nur in DAO-/Persistence-Klassen verwendet werden.

JDBC-Klassen:

- `DriverManager`
- `Connection`
- `Statement`
- `ResultSet`

Nicht erlaubt:

```java
// In ConsoleUi oder Service nicht erlaubt
Connection connection = DriverManager.getConnection(...);
```

### 10.2 Package `java.sql`

Alle notwendigen JDBC-Klassen stammen aus `java.sql`.

Beispiel:

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
```

### 10.3 Aufgabe der JDBC-Klassen

| Klasse | Aufgabe |
|---|---|
| `DriverManager` | Verwaltet den passenden Datenbanktreiber und öffnet Verbindungen |
| `Connection` | Repräsentiert eine Verbindung zur Datenbank |
| `Statement` | Repräsentiert eine SQL-Anweisung |
| `ResultSet` | Repräsentiert das Ergebnis einer SQL-Abfrage als tabellenartige Struktur |

### 10.4 JDBC-Treiber

Regeln:

- Der JDBC-Treiber ist abhängig vom konkreten Datenbanksystem.
- Treiber werden üblicherweise als `.jar` bereitgestellt.
- Der konkrete URL-Name ist nicht Java-standardisiert und muss datenbankspezifisch geprüft werden.
- Für MariaDB kann ein Connector/J-Treiber nötig sein.
- Die JAR-Datei muss dem Projekt/der IDE als Bibliothek hinzugefügt werden.

### 10.5 Verbindungsaufbau

Die Verbindung zur Datenbank soll zentral erzeugt werden, nicht verstreut im Code.

Beispiel:

```java
public class DatabaseConnectionFactory {
    private static final String URL = "jdbc:mariadb://localhost:3306/projekt";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

Regeln:

- Verbindungsdaten nicht mehrfach im Projekt duplizieren.
- Datenbankzugriff nicht in `main()` zusammenbauen, wenn es eine Persistence-Schicht gibt.
- Verbindungsaufbau zentral kapseln.

### 10.6 SQL-Arten

DDL: Tabellen erzeugen, ändern, löschen.

```sql
CREATE TABLE ...;
ALTER TABLE ...;
DROP TABLE ...;
```

DML: Daten abfragen und verändern.

```sql
SELECT ... FROM ...;
INSERT INTO ... VALUES ...;
UPDATE ... SET ... WHERE ...;
DELETE FROM ... WHERE ...;
```

### 10.7 SQL-Datentypen aus den Folien

| SQL-Typ | Bedeutung |
|---|---|
| `CHAR(n)` | Zeichenkette fester Länge |
| `VARCHAR(n)` | Zeichenkette maximaler Länge |
| `SMALLINT` | 16-bit Vorzeichenzahl |
| `INTEGER` | 32-bit Vorzeichenzahl |
| `REAL` | Fließkommazahl mit sieben Stellen |
| `FLOAT` | Fließkommazahl mit 15 Stellen |
| `DECIMAL(n,m)` | Festkommazahl mit n Stellen, davon m Nachkommastellen |
| `DATE` | Datum; je nach DB auch `TIME`, `TIMESTAMP` |

### 10.8 ResultSet nur in der Datenhaltung auswerten

DAO-Klassen sollen `ResultSet` auswerten und Java-Objekte zurückgeben.

Nicht:

```java
public ResultSet findeAlle() {
    // ResultSet wird nach oben durchgereicht
}
```

Besser:

```java
public List<Mitarbeiter> findeAlle() throws DatenzugriffException {
    List<Mitarbeiter> result = new ArrayList<>();
    // ResultSet auswerten
    // Mitarbeiter-Objekte erzeugen
    return result;
}
```

### 10.9 SQL nicht mit Fachlogik vermischen

Nicht:

```java
public Ergebnis genehmigeAntrag(...) {
    // fachliche Prüfung
    // SQL direkt hier
    // Ausgabe direkt hier
}
```

Besser:

```text
Service prüft fachlich.
DAO speichert/lädt.
UI zeigt Ergebnis.
```

---

## 11. Objektmodell und relationales DB-Schema

### 11.1 Mapping-Regeln

| Klassendiagramm / UML | Relationales DB-Schema |
|---|---|
| Klasse | Tabelle / Entitätstyp |
| Attribut | Spalte |
| Objekt | Tabellenzeile, ganz oder teilweise |
| Assoziation / Aggregation / Komposition | Beziehung über Fremdschlüssel |
| 1:n-Beziehung | Zwei Tabellen, eine davon mit Fremdschlüssel |
| n:m-Beziehung | Dritte Tabelle für die Beziehung mit beiden Schlüsseln |
| Vererbung | Nicht direkt unterstützt; nur mit Zusatzaufwand annäherbar |
| Methoden | Nicht direkt in Tabellen abbildbar |

### 11.2 Primärschlüssel und Identität

Regeln:

- Jede Tabelle braucht einen Primärschlüssel.
- In der Datenbank wird Identität über Primärschlüssel abgebildet.
- In Java existiert zusätzlich die Objektreferenz.
- Wenn fachliche Gleichheit relevant ist, `equals()` und `hashCode()` passend implementieren.

### 11.3 Fremdschlüssel für Beziehungen

Beziehungen werden in relationalen Datenbanken über Fremdschlüssel abgebildet.

Beispiel 1:n:

```text
KUNDE(id, name)
KONTO(id, saldo, kunde_id)
```

`konto.kunde_id` verweist auf `kunde.id`.

Beispiel n:m:

```text
STUDIERENDER(id, name)
LEHRVERANSTALTUNG(id, titel)
STUDIERENDER_LEHRVERANSTALTUNG(studierender_id, lehrveranstaltung_id)
```

### 11.4 Navigation in Java explizit modellieren

In einer relationalen Datenbank können Beziehungen über Fremdschlüssel in beide Richtungen abgefragt werden.

In Java muss Navigierbarkeit explizit als Attribut modelliert werden.

Beispiel: Kunde kennt Konto.

```java
public class Kunde {
    private Konto konto;
}
```

Beispiel: Konto kennt Besitzer.

```java
public class Konto {
    private Kunde besitzer;
}
```

Regel: Nur die Navigationsrichtungen implementieren, die im Code wirklich gebraucht werden.

---

## 12. Umgang mit DTOs und Datenobjekten

Diese Folien nennen DTOs nicht explizit als eigenes Thema, aber für die Schichtenarchitektur ist eine klare Datenübergabe sinnvoll.

Regeln:

- Keine JDBC-Objekte über Schichtgrenzen geben.
- Keine UI-spezifischen Objekte in die Datenhaltung geben.
- Für Rückgaben an die Präsentation einfache Ergebnisobjekte oder DTOs nutzen.
- Domain-Objekte enthalten fachliche Daten und optional fachliches Verhalten.

Beispiel:

```java
public class ErgebnisDto {
    private final String status;
    private final String hinweis;

    public ErgebnisDto(String status, String hinweis) {
        this.status = status;
        this.hinweis = hinweis;
    }

    public String getStatus() {
        return status;
    }

    public String getHinweis() {
        return hinweis;
    }
}
```

---

## 13. Konkrete Codex-Regeln für die Codegenerierung

Wenn Codex Code erzeugt oder ändert, gelten diese Regeln:

### 13.1 Architektur

- Erzeuge keine Monolith-Klasse mit UI, Logik und SQL zusammen.
- Erzeuge mindestens getrennte Packages für Präsentation, Anwendung und Datenhaltung.
- SQL/JDBC darf nur in Persistence-/DAO-Klassen vorkommen.
- UI ruft Services auf, nicht DAOs direkt.
- Services enthalten Fachlogik und nutzen DAO-Interfaces.
- DAOs enthalten Datenbankzugriff und keine UI-Ausgabe.

### 13.2 Klassen und Attribute

- Attribute standardmäßig `private`.
- Keine öffentlichen veränderlichen Attribute.
- Getter/Setter nur, wenn nötig.
- Pflichtwerte im Konstruktor setzen.
- Ungültige Konstruktorparameter mit Exceptions ablehnen.
- Konstanten als `private static final`.
- Statische veränderliche Attribute nur bewusst einsetzen, z. B. Zähler.

### 13.3 Methoden

- Jede Methode soll eine klare Verantwortung haben.
- Sichtbarkeit so restriktiv wie sinnvoll wählen.
- Hilfsmethoden `private` machen.
- `static` nur für echte klassenbezogene Hilfsfunktionen ohne Objektzustand.
- `final` nutzen, wenn Überschreiben fachlich oder sicherheitstechnisch verhindert werden soll.

### 13.4 Interfaces

- Für Services und DAOs nach Möglichkeit Interfaces definieren.
- Implementierungen klar benennen, z. B. `JdbcMitarbeiterDao`.
- Variablen und Konstruktorparameter bevorzugt als Interface-Typ deklarieren.
- Klassen, die Interfaces implementieren, müssen alle Methoden implementieren oder abstrakt sein.

### 13.5 Collections

- Keine rohen Collections verwenden.
- Immer Generics verwenden.
- `List<T>` statt Array, wenn Anzahl dynamisch ist.
- `Map<K,V>` für eindeutige Schlüssel.
- `Set<T>` für eindeutige Mengen ohne Duplikate.
- For-each für einfaches Durchlaufen.
- Typisierte Iteratoren verwenden, wenn Iterator nötig ist.

### 13.6 Exceptions

- Keine ungültigen Objekte erzeugen.
- Keine Fehler nur per `System.out.println` behandeln.
- Eigene sprechende Exceptions verwenden.
- Checked Exceptions mit `throws` deklarieren oder behandeln.
- Keine leeren catch-Blöcke.
- `finally` für Code verwenden, der immer laufen muss.

### 13.7 JDBC

- JDBC-Code ausschließlich in DAO-/Persistence-Klassen.
- `ResultSet` nie aus der DAO-Schicht herausgeben.
- SQL-Anweisungen in DAO-Methoden kapseln.
- Verbindungsdaten zentral halten.
- Datenbanktreiber als Projektbibliothek einbinden.
- Tabellen mit Primärschlüsseln und Fremdschlüsseln modellieren.

---

## 14. Prüf-Checkliste vor Abgabe

### Architektur

- [ ] Es gibt eine erkennbare Präsentationsschicht.
- [ ] Es gibt eine erkennbare Anwendungsschicht.
- [ ] Es gibt eine erkennbare Datenhaltungsschicht.
- [ ] Präsentation enthält kein SQL.
- [ ] Präsentation enthält kein JDBC.
- [ ] Datenhaltung enthält keine UI-Ausgabe.
- [ ] Fachliche Regeln liegen in der Anwendungsschicht.
- [ ] Datenzugriff liegt in der Datenhaltungsschicht.
- [ ] Keine zentrale Monolith-Klasse mit allem zusammen.

### Klassen und Modifier

- [ ] Attribute sind typisiert.
- [ ] Attribute sind grundsätzlich `private`.
- [ ] Zugriff erfolgt kontrolliert über Methoden.
- [ ] `protected` wird nur bei sinnvoller Vererbung genutzt.
- [ ] `public` wird nur für echte Schnittstellenmethoden genutzt.
- [ ] Konstanten sind `static final`.
- [ ] Statische Attribute werden bewusst eingesetzt.
- [ ] Abstrakte Klassen werden nur eingesetzt, wenn keine direkten Objekte davon sinnvoll sind.
- [ ] Finale Klassen/Methoden werden eingesetzt, wenn Vererbung/Überschreiben verhindert werden soll.

### Konstruktoren und Validierung

- [ ] Konstruktoren setzen Pflichtwerte.
- [ ] Konstruktoren verhindern ungültige Objekte.
- [ ] Ungültige Parameter führen zu Exceptions.
- [ ] Unterklassen rufen passende Oberklassen-Konstruktoren mit `super(...)` auf.
- [ ] Redundanter Initialisierungscode wird vermieden.

### Interfaces

- [ ] Service-/DAO-Schnittstellen sind klar definiert.
- [ ] Implementierungen implementieren alle Interface-Methoden.
- [ ] Interfaces enthalten keine normalen Attribute.
- [ ] Interface-Konstanten sind Konstanten.
- [ ] Gegen Interface-Typen wird programmiert, wo Austauschbarkeit sinnvoll ist.

### Collections

- [ ] Variable Objektmengen nutzen Collections statt Arrays.
- [ ] Collections sind generisch typisiert.
- [ ] Es gibt keine Raw-Type-Warnungen.
- [ ] `List`, `Set` oder `Map` wurde passend zur Aufgabe gewählt.
- [ ] Keine unnötigen Casts beim Auslesen von Collections.
- [ ] Iteratoren sind typisiert.
- [ ] For-each wird für einfaches Durchlaufen genutzt.
- [ ] Bei Sets/Maps mit eigenen Objekten sind `equals()`/`hashCode()` bedacht.
- [ ] Bei sortierten Collections ist `compareTo(...)` oder eine Sortierlogik vorhanden.

### Exceptions

- [ ] Fachliche Fehler werden mit eigenen Exceptions signalisiert.
- [ ] Checked Exceptions werden behandelt oder weitergeleitet.
- [ ] Keine leeren catch-Blöcke.
- [ ] Fehlermeldungen sind aussagekräftig.
- [ ] `finally` wird genutzt, wenn Code immer ausgeführt werden muss.
- [ ] Programmierfehler werden nicht sinnlos verschluckt.

### Datenbank und JDBC

- [ ] JDBC wird nur in der Datenhaltung verwendet.
- [ ] `DriverManager`, `Connection`, `Statement`, `ResultSet` sind nicht in UI-/Service-Klassen.
- [ ] SQL ist in DAO-Klassen gekapselt.
- [ ] Tabellen besitzen Primärschlüssel.
- [ ] Beziehungen werden mit Fremdschlüsseln umgesetzt.
- [ ] n:m-Beziehungen haben eine eigene Verbindungstabelle.
- [ ] Java-Objekte werden sauber aus Tabellenzeilen aufgebaut.
- [ ] `ResultSet` wird nicht nach oben durchgereicht.
- [ ] Der JDBC-Treiber ist im Projekt eingebunden.

---

## 15. Kurzfassung als harte Regeln

1. **Keine Monolith-Klasse.** UI, Fachlogik und Datenbankzugriff trennen.
2. **Drei Schichten einhalten:** Präsentation, Anwendung, Datenhaltung.
3. **JDBC nur in Datenhaltung.** Nie in UI oder Service.
4. **SQL nur in DAO-/Persistence-Klassen.**
5. **Fachliche Regeln nur in der Anwendungsschicht.**
6. **Attribute grundsätzlich `private`.**
7. **Zugriff über Methoden, nicht direkt über Attribute.**
8. **Ungültige Objekte verhindern.** Konstruktoren validieren und werfen Exceptions.
9. **Eigene Exceptions für fachliche Fehler.**
10. **Checked Exceptions behandeln oder weiterleiten.**
11. **Keine leeren catch-Blöcke.**
12. **Collections statt Arrays bei dynamischen Mengen.**
13. **Immer Generics verwenden.** Keine Raw Types.
14. **Collection-Typ bewusst wählen:** `List`, `Set`, `Map`.
15. **Interfaces für Services/DAOs nutzen.** Gegen Schnittstellen programmieren.
16. **Keine `ResultSet`-Objekte außerhalb der Datenhaltung.**
17. **Primärschlüssel und Fremdschlüssel sauber modellieren.**
18. **n:m-Beziehungen über eigene Beziehungstabelle umsetzen.**
19. **Vererbung nur fachlich sinnvoll einsetzen.**
20. **`static`, `final`, `abstract`, `protected` bewusst und nicht zufällig verwenden.**

---

## 16. Quellenbasis aus den Folien

Diese Regeln wurden aus den bereitgestellten ANE2-Folien abgeleitet, insbesondere aus:

- `01_ANE2_Modifier_*`: Klassen, Attribute, Methoden, `public/private/protected`, `static`, `final`, `abstract`, Interfaces.
- `02_ANE2_Exceptions_*`: checked/unchecked Exceptions, try/catch/finally, eigene Exceptions, Konstruktorvalidierung.
- `03_ANE2_Collections_*`: Arrays vs. Collections, JCF, List/Set/Map, Iteratoren, Generics, Auswahl des Collection-Typs.
- `05_ANE2_Architektur_*`: Drei-Schichten-Modell, Präsentation, Anwendungsschicht, Datenhaltung, Nutzen und Projektumsetzung.
- `06_ANE2_JDBC_*`: Persistenz, ERM/RDBMS, SQL, JDBC, DriverManager, Connection, Statement, ResultSet, Mapping UML zu relationalem Schema.
