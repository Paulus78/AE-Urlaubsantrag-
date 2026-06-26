package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Kalendereintrag;

// Datenbankzugriffe fuer den Urlaubskalender.
// Hier wird nichts genehmigt, sondern nur gespeichert oder gelesen.
public class KalenderDAO implements IKalenderDAO {

    // Speichert einen Urlaub im Kalender und gibt die neue ID zurueck.
    @Override
    public int speichern(Kalendereintrag eintrag) throws SQLException {
        String sql = "INSERT INTO urlaubskalender "
                + "(mitarbeiter_id, starttag, endtag) VALUES (?, ?, ?)";

        Connection con = null;
        PreparedStatement stat = null;
        ResultSet res = null;
        int neueId = 0;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stat.setInt(1, eintrag.getMitarbeiterId());
            stat.setInt(2, eintrag.getStarttag());
            stat.setInt(3, eintrag.getEndtag());
            stat.executeUpdate();
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

    // Prueft, ob sich ein Zeitraum mit einem Kalendereintrag ueberschneidet.
    @Override
    public boolean hatUeberschneidung(int mitarbeiterId, int starttag, int endtag)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM urlaubskalender "
                + "WHERE mitarbeiter_id = ? "
                + "AND starttag <= ? "
                + "AND endtag >= ?";

        Connection con = null;
        PreparedStatement stat = null;
        ResultSet res = null;
        boolean hatUeberschneidung = false;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql);
            stat.setInt(1, mitarbeiterId);
            stat.setInt(2, endtag);
            stat.setInt(3, starttag);
            res = stat.executeQuery();

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

    // JDBC-Objekte wieder schliessen, damit keine Verbindung offen bleibt.
    private void schliessen(ResultSet res, PreparedStatement stat, Connection con)
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
