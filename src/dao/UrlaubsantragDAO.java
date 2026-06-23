package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Urlaubsantrag;

// JDBC-Umsetzung fuer die Tabelle urlaubsantrag.
// Diese Klasse liest und schreibt Antraege, entscheidet aber nichts fachlich.
public class UrlaubsantragDAO implements IUrlaubsantragDAO {

    // Speichert einen neuen Urlaubsantrag und gibt die neue Antrag-ID zurueck.
    @Override
    public int speichern(Urlaubsantrag antrag) throws SQLException {
        // TODO: In der aktuellen DB heisst die Spalte Angestellter_ID.
        // In der Spezifikation heisst sie fachlich antragsteller_id.
        String sql = "INSERT INTO urlaubsantrag "
                + "(Starttag, Endtag, Status, Angestellter_ID, Vertretung_ID, "
                + "Genehmiger_ID) VALUES ("
                + antrag.getStarttag() + ", "
                + antrag.getEndtag() + ", "
                + sqlText(antrag.getStatus()) + ", "
                + antrag.getAntragstellerId() + ", "
                + antrag.getVertretungId() + ", "
                + sqlInteger(antrag.getGenehmigerId()) + ")";

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
            throw new SQLException("Urlaubsantrag konnte nicht gespeichert werden.",
                    exception);
        } finally {
            schliessen(res, stat, con);
        }

        return neueId;
    }

    // Sucht einen Urlaubsantrag anhand seiner ID.
    @Override
    public Urlaubsantrag findeNachId(int antragId) throws SQLException {
        String sql = "SELECT Antrag_ID, Starttag, Endtag, Status, "
                + "Angestellter_ID, Vertretung_ID, Genehmiger_ID "
                + "FROM urlaubsantrag WHERE Antrag_ID = " + antragId;

        Connection con = null;
        Statement stat = null;
        ResultSet res = null;
        Urlaubsantrag antrag = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            res = stat.executeQuery(sql);

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

    // Liefert alle offenen Urlaubsantraege aus der Datenbank.
    @Override
    public List<Urlaubsantrag> findeOffeneAntraege() throws SQLException {
        String sql = "SELECT Antrag_ID, Starttag, Endtag, Status, "
                + "Angestellter_ID, Vertretung_ID, Genehmiger_ID "
                + "FROM urlaubsantrag WHERE Status = "
                + sqlText(Urlaubsantrag.STATUS_OFFEN);

        Connection con = null;
        Statement stat = null;
        ResultSet res = null;
        List<Urlaubsantrag> antraege = new ArrayList<>();

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            res = stat.executeQuery(sql);

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
        String sql = "UPDATE urlaubsantrag SET Status = " + sqlText(status)
                + " WHERE Antrag_ID = " + antragId;

        Connection con = null;
        Statement stat = null;

        try {
            con = DatabaseConnection.getConnection();
            stat = con.createStatement();
            stat.executeUpdate(sql);
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

    // Setzt Hochkommas um einen Text fuer einfache SQL-Strings.
    private String sqlText(String wert) {
        if (wert == null) {
            return "NULL";
        }

        return "'" + wert + "'";
    }

    // Gibt NULL oder die Zahl als Text fuer SQL zurueck.
    private String sqlInteger(Integer wert) {
        if (wert == null) {
            return "NULL";
        }

        return String.valueOf(wert);
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
