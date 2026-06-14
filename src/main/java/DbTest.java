import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbTest {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/ae_urlaubsantraege";
    private static final String USER = getEnvironmentValue("DB_USER", "root");
    private static final String PASSWORD = getEnvironmentValue("DB_PASSWORD", "");

    public static void main(String[] args) {
        String sql = "SELECT COUNT(*) FROM mitarbeiter";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
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

    private static String getEnvironmentValue(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
