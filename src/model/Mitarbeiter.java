package model;

import java.io.Serializable;

// Datenklasse fuer einen Mitarbeiter aus der Tabelle mitarbeiter.
// Serializable ist wichtig, weil Objekte spaeter ueber RMI verschickt werden koennen.
public class Mitarbeiter implements Serializable {

    // Attribute entsprechen den Spalten aus dem Datenmodell.
    private int mitarbeiterId;
    private String vorname;
    private String nachname;
    private int resturlaub;
    private Integer vorgesetzterId;

    // Leerer Konstruktor, damit Java/RMI Objekte einfach erzeugen kann.
    public Mitarbeiter() {
    }

    // Vollstaendiger Konstruktor, wenn alle Werte direkt bekannt sind.
    public Mitarbeiter(int mitarbeiterId, String vorname, String nachname,
            int resturlaub, Integer vorgesetzterId) {
        this.mitarbeiterId = mitarbeiterId;
        this.vorname = vorname;
        this.nachname = nachname;
        this.resturlaub = resturlaub;
        this.vorgesetzterId = vorgesetzterId;
    }

    // Getter und Setter fuer den Zugriff von anderen Klassen.
    public int getMitarbeiterId() {
        return mitarbeiterId;
    }

    public void setMitarbeiterId(int mitarbeiterId) {
        this.mitarbeiterId = mitarbeiterId;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public int getResturlaub() {
        return resturlaub;
    }

    public void setResturlaub(int resturlaub) {
        this.resturlaub = resturlaub;
    }

    public Integer getVorgesetzterId() {
        return vorgesetzterId;
    }

    public void setVorgesetzterId(Integer vorgesetzterId) {
        this.vorgesetzterId = vorgesetzterId;
    }
}
