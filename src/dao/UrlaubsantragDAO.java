package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import model.Urlaubsantrag;

// Datenbankzugriffe fuer Urlaubsantraege.
// Die Entscheidung ueber Genehmigung oder Ablehnung trifft der Service.
public class UrlaubsantragDAO implements IUrlaubsantragDAO {

    // Speichert einen Antrag und gibt die neue Antrag-ID zurueck.
    @Override
    public int speichern(Urlaubsantrag antrag) throws SQLException {
        // TODO: In der aktuellen DB heisst die Spalte Angestellter_ID.
        // In der Spezifikation heisst sie fachlich antragsteller_id.
        String sql = "INSERT INTO urlaubsantrag "
                + "(Starttag, Endtag, Status, Angestellter_ID, Vertretung_ID, "
                + "Genehmiger_ID) VALUES (?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement stat = null;
        ResultSet res = null;
        int neueId = 0;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stat.setInt(1, antrag.getStarttag());
            stat.setInt(2, antrag.getEndtag());
            stat.setString(3, antrag.getStatus());
            stat.setInt(4, antrag.getAntragstellerId());
            stat.setInt(5, antrag.getVertretungId());
            setIntegerOderNull(stat, 6, antrag.getGenehmigerId());
            stat.executeUpdate();
            res = stat.getGeneratedKeys();

            if (res.next()) {
                neueId = res.getInt(1);
            }
        } catch (SQLException exception) {
            throw new SQLException("Urlaubsantrag konnte nicht gespeichert werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return neueId;
    }

    // Sucht einen Antrag ueber seine ID.
    @Override
    public Urlaubsantrag findeNachId(int antragId) throws SQLException {
        String sql = "SELECT Antrag_ID, Starttag, Endtag, Status, "
                + "Angestellter_ID, Vertretung_ID, Genehmiger_ID "
                + "FROM urlaubsantrag WHERE Antrag_ID = ?";

        Connection con = null;
        PreparedStatement stat = null;
        ResultSet res = null;
        Urlaubsantrag antrag = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql);
            stat.setInt(1, antragId);
            res = stat.executeQuery();

            if (res.next()) {
                antrag = erstelleUrlaubsantrag(res);
            }
        } catch (SQLException exception) {
            throw new SQLException("Urlaubsantrag konnte nicht gelesen werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return antrag;
    }

    // Liefert alle Antraege mit dem Status offen.
    @Override
    public List<Urlaubsantrag> findeOffeneAntraege() throws SQLException {
        String sql = "SELECT Antrag_ID, Starttag, Endtag, Status, "
                + "Angestellter_ID, Vertretung_ID, Genehmiger_ID "
                + "FROM urlaubsantrag WHERE Status = ?";

        Connection con = null;
        PreparedStatement stat = null;
        ResultSet res = null;
        List<Urlaubsantrag> antraege = new ArrayList<>();

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql);
            stat.setString(1, Urlaubsantrag.STATUS_OFFEN);
            res = stat.executeQuery();

            while (res.next()) {
                antraege.add(erstelleUrlaubsantrag(res));
            }
        } catch (SQLException exception) {
            throw new SQLException("Offene Urlaubsantraege konnten nicht gelesen werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return antraege;
    }

    // Aktualisiert nur den Status eines vorhandenen Antrags.
    @Override
    public void statusAktualisieren(int antragId, String status)
            throws SQLException {
        String sql = "UPDATE urlaubsantrag SET Status = ? "
                + "WHERE Antrag_ID = ?";

        Connection con = null;
        PreparedStatement stat = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.prepareStatement(sql);
            stat.setString(1, status);
            stat.setInt(2, antragId);
            stat.executeUpdate();
        } catch (SQLException exception) {
            throw new SQLException("Status konnte nicht aktualisiert werden.",
                    exception);
        } finally {
            schliessen(null, stat, con);
        }
    }

    // Baut aus einer Ergebniszeile ein Urlaubsantrag-Objekt.
    private Urlaubsantrag erstelleUrlaubsantrag(ResultSet res)
            throws SQLException {
        Integer genehmigerId = null;
        int geleseneGenehmigerId = res.getInt("Genehmiger_ID");

        if (!res.wasNull()) {
            genehmigerId = geleseneGenehmigerId;
        }

        return new Urlaubsantrag(
                res.getInt("Antrag_ID"),
                res.getInt("Starttag"),
                res.getInt("Endtag"),
                res.getString("Status"),
                res.getInt("Angestellter_ID"),
                res.getInt("Vertretung_ID"),
                genehmigerId);
    }

    // Setzt eine Integer-Zahl oder NULL in ein PreparedStatement.
    private void setIntegerOderNull(PreparedStatement stat, int position,
            Integer wert) throws SQLException {
        if (wert == null) {
            stat.setNull(position, Types.INTEGER);
        } else {
            stat.setInt(position, wert);
        }
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
