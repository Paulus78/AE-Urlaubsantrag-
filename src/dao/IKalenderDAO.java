package dao;

import java.sql.SQLException;

import model.Kalendereintrag;

// Schnittstelle fuer den Urlaubskalender.
// Im Kalender stehen spaeter nur genehmigte Urlaube.
public interface IKalenderDAO {

    // Speichert einen neuen Kalendereintrag und gibt dessen ID zurueck.
    int speichern(Kalendereintrag eintrag) throws SQLException;

    // Prueft, ob ein Mitarbeiter im Zeitraum schon Urlaub eingetragen hat.
    boolean hatUeberschneidung(int mitarbeiterId, int starttag, int endtag)
            throws SQLException;
}
