package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import service.IUrlaubsService;
import service.UrlaubsService;

// Startklasse fuer den RMI-Server.
// Diese Klasse muss laufen, bevor ein Client den UrlaubsService nutzen kann.
public class RMIServer {

    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "urlaubs_service";

    public static void main(String[] args) {
        try {
            // Die Registry ist wie ein kleines Telefonbuch fuer RMI-Objekte.
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // Das echte Remote-Objekt, das spaeter vom Client aufgerufen wird.
            IUrlaubsService service = new UrlaubsService();

            // Unter diesem Namen findet der Client den Service.
            registry.rebind(SERVICE_NAME, service);

            System.out.println("RMI-Registry wurde auf Port " + RMI_PORT
                    + " gestartet.");
            System.out.println("UrlaubsService wurde als '" + SERVICE_NAME
                    + "' registriert.");
        } catch (RemoteException exception) {
            System.out.println("RMI-Server konnte nicht gestartet werden.");
            exception.printStackTrace();
        }
    }
}
