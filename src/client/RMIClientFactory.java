package client;

import java.rmi.Naming;

import service.IUrlaubsService;

// Kleine Hilfsklasse fuer den Zugriff auf den RMI-Service.
// So muss die spaetere ConsoleUI die RMI-Adresse nicht selbst kennen.
public class RMIClientFactory {

    private static final String RMI_URL =
            "rmi://localhost:1099/urlaubs_service";

    public static IUrlaubsService holeUrlaubsService() throws Exception {
        return (IUrlaubsService) Naming.lookup(RMI_URL);
    }
}
