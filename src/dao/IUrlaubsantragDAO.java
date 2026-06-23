package dao;

import java.sql.SQLException;
import java.util.List;

import model.Urlaubsantrag;

// Schnittstelle fuer alle Datenbankaktionen rund um Urlaubsantraege.
// Die eigentliche JDBC-Umsetzung kommt spaeter in eine eigene DAO-Klasse.
public interface IUrlaubsantragDAO {

    // Speichert einen Antrag und gibt die neue ID aus der Datenbank zurueck.
    int speichern(Urlaubsantrag antrag) throws SQLException;

    // Sucht genau einen Antrag ueber seine ID.
    Urlaubsantrag findeNachId(int antragId) throws SQLException;

    // Liefert alle Antraege, die noch den Status "offen" haben.
    List<Urlaubsantrag> findeOffeneAntraege() throws SQLException;

    // Aendert nur den Status eines vorhandenen Antrags.
    void statusAktualisieren(int antragId, String status) throws SQLException;
}
