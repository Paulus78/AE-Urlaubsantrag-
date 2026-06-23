import dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Kleine Testklasse, um die Verbindung zur lokalen MariaDB zu pruefen.
// Diese Klasse gehoert noch nicht zur eigentlichen Urlaubsverwaltung.
public class DbTest {

    public static void main(String[] args) {
        // Es wird nur gelesen. An Tabellen oder Daten wird hier nichts geaendert.
        String sql = "SELECT COUNT(*) FROM mitarbeiter";

        // try-with-resources schliesst Verbindung, Statement und Ergebnis automatisch.
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (result.next()) {
                System.out.println("Verbindung erfolgreich.");
                System.out.println("Anzahl Mitarbeiter: " + result.getInt(1));
            }
        } catch (SQLException exception) {
            System.err.println("Datenbanktest fehlgeschlagen: " + exception.getMessage());
        }
    }
}
