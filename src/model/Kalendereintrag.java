package model;

import java.io.Serializable;

// Datenklasse fuer einen Eintrag im Urlaubskalender.
// Ein Eintrag bedeutet: Dieser Urlaub ist genehmigt und im Kalender gespeichert.
public class Kalendereintrag implements Serializable {

    // Attribute entsprechen den Spalten der Tabelle urlaubskalender.
    private int kalenderId;
    private int mitarbeiterId;
    private int starttag;
    private int endtag;

    // Leerer Konstruktor fuer einfache Objekterzeugung.
    public Kalendereintrag() {
    }

    // Vollstaendiger Konstruktor fuer einen kompletten Kalendereintrag.
    public Kalendereintrag(int kalenderId, int mitarbeiterId, int starttag,
            int endtag) {
        this.kalenderId = kalenderId;
        this.mitarbeiterId = mitarbeiterId;
        this.starttag = starttag;
        this.endtag = endtag;
    }

    // Getter und Setter fuer den Zugriff aus DAO und Service.
    public int getKalenderId() {
        return kalenderId;
    }

    public void setKalenderId(int kalenderId) {
        this.kalenderId = kalenderId;
    }

    public int getMitarbeiterId() {
        return mitarbeiterId;
    }

    public void setMitarbeiterId(int mitarbeiterId) {
        this.mitarbeiterId = mitarbeiterId;
    }

    public int getStarttag() {
        return starttag;
    }

    public void setStarttag(int starttag) {
        this.starttag = starttag;
    }

    public int getEndtag() {
        return endtag;
    }

    public void setEndtag(int endtag) {
        this.endtag = endtag;
    }
}
