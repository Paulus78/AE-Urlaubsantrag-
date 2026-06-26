package dto;

import java.io.Serializable;

// DTO fuer Rueckmeldungen vom Service an die UI.
// So kann die UI spaeter anzeigen, ob etwas geklappt hat.
public class ErgebnisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // erfolgreich: true oder false, dazu Status, Meldung und optional Antrag-ID.
    private boolean erfolgreich;
    private String status;
    private String meldung;
    private Integer antragId;

    // Leerer Konstruktor fuer RMI.
    public ErgebnisDTO() {
    }

    // Konstruktor fuer eine komplette Rueckmeldung.
    public ErgebnisDTO(boolean erfolgreich, String status, String meldung,
            Integer antragId) {
        this.erfolgreich = erfolgreich;
        this.status = status;
        this.meldung = meldung;
        this.antragId = antragId;
    }

    // Getter und Setter fuer die Anzeige oder Weiterverarbeitung des Ergebnisses.
    public boolean isErfolgreich() {
        return erfolgreich;
    }

    public void setErfolgreich(boolean erfolgreich) {
        this.erfolgreich = erfolgreich;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMeldung() {
        return meldung;
    }

    public void setMeldung(String meldung) {
        this.meldung = meldung;
    }

    public Integer getAntragId() {
        return antragId;
    }

    public void setAntragId(Integer antragId) {
        this.antragId = antragId;
    }
}
