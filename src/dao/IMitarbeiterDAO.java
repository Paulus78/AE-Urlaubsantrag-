package dao;

import java.sql.SQLException;

import model.Mitarbeiter;

// Schnittstelle fuer Datenbankaktionen zur Tabelle mitarbeiter.
// Dadurch muss der Service spaeter nicht wissen, wie JDBC genau funktioniert.
public interface IMitarbeiterDAO {

    // Sucht einen Mitarbeiter anhand seiner ID.
    Mitarbeiter findeNachId(int mitarbeiterId) throws SQLException;

    // Speichert den neu berechneten Resturlaub eines Mitarbeiters.
    void resturlaubAktualisieren(int mitarbeiterId, int neuerResturlaub)
            throws SQLException;
}
