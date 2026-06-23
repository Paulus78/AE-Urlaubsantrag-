package dto;

import java.io.Serializable;

// DTO fuer die Eingaben beim Urlaub beantragen.
// Die UI packt die Eingaben hier hinein und schickt sie spaeter an den Service.
public class UrlaubsantragEingabeDTO implements Serializable {

    // Genau diese vier Werte gibt der Benutzer fuer einen Antrag ein.
    private int antragstellerId;
    private int vertretungId;
    private int starttag;
    private int endtag;

    // Leerer Konstruktor fuer RMI und einfache Objekterzeugung.
    public UrlaubsantragEingabeDTO() {
    }

    // Konstruktor, wenn alle Eingabewerte direkt bekannt sind.
    public UrlaubsantragEingabeDTO(int antragstellerId, int vertretungId,
            int starttag, int endtag) {
        this.antragstellerId = antragstellerId;
        this.vertretungId = vertretungId;
        this.starttag = starttag;
        this.endtag = endtag;
    }

    // Getter und Setter fuer die Weitergabe der Eingabewerte.
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
