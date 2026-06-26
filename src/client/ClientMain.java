package client;

import presentation.ConsoleUI;
import service.IUrlaubsService;

// Startklasse fuer den Client.
// Der Client holt den Service per RMI und startet dann die ConsoleUI.
public class ClientMain {

    public static void main(String[] args) {
        try {
            IUrlaubsService service = RMIClientFactory.holeUrlaubsService();
            ConsoleUI ui = new ConsoleUI(service);
            ui.starten();
        } catch (Exception exception) {
            System.out.println("RMI-Client konnte den Service nicht nutzen.");
            exception.printStackTrace();
        }
    }
}
