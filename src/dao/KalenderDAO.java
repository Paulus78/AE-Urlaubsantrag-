package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Kalendereintrag;

// JDBC-Umsetzung fuer die Tabelle urlaubskalender.
// Hier wird nur mit Kalenderdaten gearbeitet, nicht fachlich entschieden.
public class KalenderDAO implements IKalenderDAO {

    // Speichert einen genehmigten Urlaub im Kalender.
    @Override
    public int speichern(Kalendereintrag eintrag) throws SQLException {
        String sql = "INSERT INTO urlaubskalender "
                + "(mitarbeiter_id, starttag, endtag) VALUES ("
                + eintrag.getMitarbeiterId() + ", "
                + eintrag.getStarttag() + ", "
                + eintrag.getEndtag() + ")";

        Connection con = null;
        Statement stat = null;
        ResultSet res = null;
        int neueId = 0;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            stat.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            res = stat.getGeneratedKeys();

            if (res.next()) {
                neueId = res.getInt(1);
            }
        } catch (SQLException exception) {
            throw new SQLException("Kalendereintrag konnte nicht gespeichert werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return neueId;
    }

    // Prueft, ob im Kalender schon ein Urlaub im gleichen Zeitraum liegt.
    @Override
    public boolean hatUeberschneidung(int mitarbeiterId, int starttag, int endtag)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM urlaubskalender "
                + "WHERE mitarbeiter_id = " + mitarbeiterId
                + " AND starttag <= " + endtag
                + " AND endtag >= " + starttag;

        Connection con = null;
        Statement stat = null;
        ResultSet res = null;
        boolean hatUeberschneidung = false;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            res = stat.executeQuery(sql);

            if (res.next()) {
                hatUeberschneidung = res.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new SQLException("Ueberschneidung konnte nicht geprueft werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return hatUeberschneidung;
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
