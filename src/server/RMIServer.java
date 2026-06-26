package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import service.IUrlaubsService;
import service.UrlaubsService;

// Startklasse fuer den RMI-Server.
// Diese Klasse muss laufen, bevor ein Client den UrlaubsService nutzen kann.
public class RMIServer {

    public static void main(String[] args) {
        try {
            // Die Registry ist wie ein kleines Telefonbuch fuer RMI-Objekte.
            LocateRegistry.createRegistry(1099);

            // Das echte Remote-Objekt, das spaeter vom Client aufgerufen wird.
            IUrlaubsService service = new UrlaubsService();

            // Unter diesem Namen findet der Client den Service.
            Naming.rebind("rmi://localhost:1099/UrlaubsService", service);

            System.out.println("RMI-Server gestartet.");
        } catch (Exception exception) {
            System.out.println("RMI-Server konnte nicht gestartet werden.");
            exception.printStackTrace();
        }
    }
}
