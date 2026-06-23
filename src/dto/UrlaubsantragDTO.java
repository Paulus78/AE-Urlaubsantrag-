package dto;

import java.io.Serializable;

// DTO fuer die Anzeige oder Uebertragung eines Urlaubsantrags.
// Es enthaelt die wichtigsten Antragsdaten, aber keine Fachlogik.
public class UrlaubsantragDTO implements Serializable {

    // Diese Werte koennen spaeter z.B. in der ConsoleUI angezeigt werden.
    private int antragId;
    private int starttag;
    private int endtag;
    private String status;
    private int antragstellerId;
    private int vertretungId;
    private Integer genehmigerId;

    // Leerer Konstruktor fuer RMI.
    public UrlaubsantragDTO() {
    }

    // Konstruktor, wenn alle Antragsdaten direkt gesetzt werden sollen.
    public UrlaubsantragDTO(int antragId, int starttag, int endtag,
            String status, int antragstellerId, int vertretungId,
            Integer genehmigerId) {
        this.antragId = antragId;
        this.starttag = starttag;
        this.endtag = endtag;
        this.status = status;
        this.antragstellerId = antragstellerId;
        this.vertretungId = vertretungId;
        this.genehmigerId = genehmigerId;
    }

    // Getter und Setter fuer die einzelnen Antragsdaten.
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
