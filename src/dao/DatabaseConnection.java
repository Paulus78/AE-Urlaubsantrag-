package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Zentrale Klasse fuer den Verbindungsaufbau zur MariaDB.
// So steht die Datenbank-Adresse nur an einer Stelle im Projekt.
public class DatabaseConnection {

    // URL der Datenbank, wie im JDBC-Skript gezeigt.
    private static final String DB_URL =
            "jdbc:mariadb://localhost:3306/ae_urlaubsantraege";

    // Name der Treiberklasse fuer MariaDB.
    private static final String DRIVER = "org.mariadb.jdbc.Driver";

    // Login-Daten wie im Unterrichtsbeispiel fuer eine lokale XAMPP/MariaDB.
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Gibt eine neue Verbindung zur Datenbank zurueck.
    // Die aufrufende Klasse muss die Verbindung danach wieder schliessen.
    public static Connection getConnection() throws SQLException {
        Connection con = null;

        try {
            Class.forName(DRIVER);
            con = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MariaDB-Treiber wurde nicht gefunden.", exception);
        }

        return con;
    }
}
