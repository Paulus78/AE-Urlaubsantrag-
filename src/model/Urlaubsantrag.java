package model;

import java.io.Serializable;

// Datenklasse fuer einen Urlaubsantrag.
// Hier steht keine Prueflogik, die kommt spaeter in den UrlaubsService.
public class Urlaubsantrag implements Serializable {

    // Erlaubte Statuswerte laut Spezifikation.
    public static final String STATUS_OFFEN = "offen";
    public static final String STATUS_GENEHMIGT = "genehmigt";
    public static final String STATUS_ABGELEHNT = "abgelehnt";

    // Attribute entsprechen den Spalten der Tabelle urlaubsantrag.
    private int antragId;
    private int starttag;
    private int endtag;
    private String status;
    private int antragstellerId;
    private int vertretungId;
    private Integer genehmigerId;

    // Leerer Konstruktor fuer einfache Objekterzeugung und spaeter RMI.
    public Urlaubsantrag() {
    }

    // Konstruktor mit allen Werten, z.B. wenn ein Datensatz aus der DB gelesen wird.
    public Urlaubsantrag(int antragId, int starttag, int endtag, String status,
            int antragstellerId, int vertretungId, Integer genehmigerId) {
        this.antragId = antragId;
        this.starttag = starttag;
        this.endtag = endtag;
        this.status = status;
        this.antragstellerId = antragstellerId;
        this.vertretungId = vertretungId;
        this.genehmigerId = genehmigerId;
    }

    // Getter und Setter halten die Attribute privat, aber nutzbar.
    public int getAntragId() {
        return antragId;
    }

    public void setAntragId(int antragId) {
        this.antragId = antragId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAntragstellerId() {
        return antragstellerId;
    }

    public void setAntragstellerId(int antragstellerId) {
        this.antragstellerId = antragstellerId;
    }

    public int getVertretungId() {
        return vertretungId;
    }

    public void setVertretungId(int vertretungId) {
        this.vertretungId = vertretungId;
    }

    public Integer getGenehmigerId() {
        return genehmigerId;
    }

    public void setGenehmigerId(Integer genehmigerId) {
        this.genehmigerId = genehmigerId;
    }
}
