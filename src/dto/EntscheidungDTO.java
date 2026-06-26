package dto;

import java.io.Serializable;

// DTO fuer die Entscheidung eines Vorgesetzten.
// Es enthaelt nur, welcher Antrag entschieden wird und ob er genehmigt wurde.
public class EntscheidungDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int antragId;
    private boolean genehmigt;

    // Leerer Konstruktor fuer RMI.
    public EntscheidungDTO() {
    }

    // Konstruktor fuer eine fertige Entscheidung.
    public EntscheidungDTO(int antragId, boolean genehmigt) {
        this.antragId = antragId;
        this.genehmigt = genehmigt;
    }

    // Getter und Setter fuer die beiden Entscheidungswerte.
    public int getAntragId() {
        return antragId;
    }

    public void setAntragId(int antragId) {
        this.antragId = antragId;
    }

    public boolean isGenehmigt() {
        return genehmigt;
    }

    public void setGenehmigt(boolean genehmigt) {
        this.genehmigt = genehmigt;
    }
}
