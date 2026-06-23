package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Mitarbeiter;

// JDBC-Umsetzung fuer die Tabelle mitarbeiter.
// Diese Klasse enthaelt nur Datenbankzugriffe, keine fachlichen Entscheidungen.
public class MitarbeiterDAO implements IMitarbeiterDAO {

    // Sucht einen Mitarbeiter anhand seiner ID in der Datenbank.
    @Override
    public Mitarbeiter findeNachId(int mitarbeiterId) throws SQLException {
        String sql = "SELECT mitarbeiter_id, vorname, nachname, resturlaub, "
                + "vorgesetzter_id FROM mitarbeiter WHERE mitarbeiter_id = "
                + mitarbeiterId;

        Connection con = null;
        Statement stat = null;
        ResultSet res = null;
        Mitarbeiter mitarbeiter = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            res = stat.executeQuery(sql);

            if (res.next()) {
                mitarbeiter = erstelleMitarbeiter(res);
            }
        } catch (SQLException exception) {
            throw new SQLException("Mitarbeiter konnte nicht gelesen werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return mitarbeiter;
    }

    // Aktualisiert den Resturlaub eines Mitarbeiters.
    // Diese Methode wird spaeter vom UrlaubsService aufgerufen.
    @Override
    public void resturlaubAktualisieren(int mitarbeiterId, int neuerResturlaub)
            throws SQLException {
        String sql = "UPDATE mitarbeiter SET resturlaub = " + neuerResturlaub
                + " WHERE mitarbeiter_id = " + mitarbeiterId;

        Connection con = null;
        Statement stat = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            stat.executeUpdate(sql);
        } catch (SQLException exception) {
            throw new SQLException("Resturlaub konnte nicht aktualisiert werden.",
                    exception);
        } finally {
            schliessen(null, stat, con);
        }
    }

    // Baut aus einer Ergebniszeile ein Mitarbeiter-Objekt.
    private Mitarbeiter erstelleMitarbeiter(ResultSet result) throws SQLException {
        Integer vorgesetzterId = null;
        int geleseneVorgesetzterId = result.getInt("vorgesetzter_id");

        if (!result.wasNull()) {
            vorgesetzterId = geleseneVorgesetzterId;
        }

        return new Mitarbeiter(
                result.getInt("mitarbeiter_id"),
                result.getString("vorname"),
                result.getString("nachname"),
                result.getInt("resturlaub"),
                vorgesetzterId);
    }

    // Schliesst die JDBC-Objekte in umgekehrter Reihenfolge.
    private void schliessen(ResultSet res, Statement stat, Connection con)
            throws SQLException {
        if (res != null) {
            res.close();
        }

        if (stat != null) {
            stat.close();
        }

        if (con != null) {
            con.close();
        }
    }
}
