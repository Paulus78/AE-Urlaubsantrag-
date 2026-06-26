package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Mitarbeiter;

// Datenbankzugriffe fuer Mitarbeiter.
// Fachliche Entscheidungen gehoeren nicht hier rein, sondern in den Service.
public class MitarbeiterDAO implements IMitarbeiterDAO {

    // Sucht einen Mitarbeiter ueber seine ID.
    @Override
    public Mitarbeiter findeNachId(int mitarbeiterId) throws SQLException {
        String sql = "SELECT mitarbeiter_id, vorname, nachname, resturlaub, "
                + "vorgesetzter_id FROM mitarbeiter WHERE mitarbeiter_id = ?";

        Connection con = null;
        PreparedStatement stat = null;
        ResultSet res = null;
        Mitarbeiter mitarbeiter = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql);
            stat.setInt(1, mitarbeiterId);
            res = stat.executeQuery();

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

    // Schreibt den neuen Resturlaub in die Datenbank.
    @Override
    public void resturlaubAktualisieren(int mitarbeiterId, int neuerResturlaub)
            throws SQLException {
        String sql = "UPDATE mitarbeiter SET resturlaub = ? "
                + "WHERE mitarbeiter_id = ?";

        Connection con = null;
        PreparedStatement stat = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql);
            stat.setInt(1, neuerResturlaub);
            stat.setInt(2, mitarbeiterId);
            stat.executeUpdate();
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
