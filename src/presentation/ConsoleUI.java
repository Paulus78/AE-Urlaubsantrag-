package presentation;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

import dto.EntscheidungDTO;
import dto.ErgebnisDTO;
import dto.UrlaubsantragDTO;
import dto.UrlaubsantragEingabeDTO;
import service.IUrlaubsService;

// Einfache Konsolenoberflaeche fuer die Urlaubsverwaltung.
// Die UI liest nur Eingaben, ruft den Service auf und zeigt Ergebnisse an.
public class ConsoleUI {

    private IUrlaubsService service;
    private Scanner scanner;

    public ConsoleUI(IUrlaubsService service) {
        this.service = service;
        scanner = new Scanner(System.in);
    }

    // Startet das einfache Menue.
    public void starten() {
        int auswahl = -1;

        while (auswahl != 0) {
            zeigeMenue();
            auswahl = leseInt("Auswahl: ");

            if (auswahl == 1) {
                urlaubBeantragen();
            } else if (auswahl == 2) {
                offeneAntraegeAnzeigen();
            } else if (auswahl == 3) {
                antragEntscheiden();
            } else if (auswahl == 0) {
                System.out.println("Programm wird beendet.");
            } else {
                System.out.println("Ungueltige Auswahl.");
            }
        }
    }

    private void zeigeMenue() {
        System.out.println();
        System.out.println("=== Urlaubsverwaltung ===");
        System.out.println("1 - Urlaub beantragen");
        System.out.println("2 - Offene Antraege anzeigen");
        System.out.println("3 - Antrag entscheiden");
        System.out.println("0 - Beenden");
    }

    // Liest die Daten fuer einen neuen Urlaubsantrag ein.
    private void urlaubBeantragen() {
        int antragstellerId = leseInt("Antragsteller-ID: ");
        int vertretungId = leseInt("Vertretung-ID: ");
        int starttag = leseInt("Starttag: ");
        int endtag = leseInt("Endtag: ");

        UrlaubsantragEingabeDTO eingabe = new UrlaubsantragEingabeDTO(
                antragstellerId, vertretungId, starttag, endtag);

        try {
            ErgebnisDTO ergebnis = service.urlaubBeantragen(eingabe);
            zeigeErgebnis(ergebnis);
        } catch (RemoteException exception) {
            System.out.println("Fehler beim Beantragen: "
                    + exception.getMessage());
        }
    }

    // Holt offene Antraege vom Service und zeigt sie in der Konsole an.
    private void offeneAntraegeAnzeigen() {
        try {
            List<UrlaubsantragDTO> antraege = service.offeneAntraegeAnzeigen();

            if (antraege.isEmpty()) {
                System.out.println("Es gibt keine offenen Antraege.");
                return;
            }

            System.out.println();
            System.out.println("Offene Antraege:");

            for (UrlaubsantragDTO antrag : antraege) {
                zeigeAntrag(antrag);
            }
        } catch (RemoteException exception) {
            System.out.println("Offene Antraege konnten nicht geladen werden: "
                    + exception.getMessage());
        }
    }

    // Liest eine Entscheidung ein und schickt sie an den Service.
    private void antragEntscheiden() {
        try {
            List<UrlaubsantragDTO> offeneAntraege =
                    service.offeneAntraegeAnzeigen();

            if (offeneAntraege.isEmpty()) {
                System.out.println("Es gibt keine offenen Antraege zum Entscheiden.");
                return;
            }

            System.out.println();
            System.out.println("Diese offenen Antraege koennen entschieden werden:");

            for (UrlaubsantragDTO antrag : offeneAntraege) {
                zeigeAntrag(antrag);
            }

            int antragId = leseInt("Antrag-ID: ");

            if (!istOffenerAntragInListe(offeneAntraege, antragId)) {
                System.out.println("Diese Antrag-ID ist nicht in der Liste "
                        + "der offenen Antraege.");
                return;
            }

            String antwort = leseText("Genehmigen? (j/n): ");
            boolean genehmigt = antwort.equalsIgnoreCase("j");

            EntscheidungDTO entscheidung =
                    new EntscheidungDTO(antragId, genehmigt);

            ErgebnisDTO ergebnis =
                    service.vorgesetztenentscheidungVerarbeiten(entscheidung);
            zeigeErgebnis(ergebnis);
        } catch (RemoteException exception) {
            System.out.println("Entscheidung konnte nicht verarbeitet werden: "
                    + exception.getMessage());
        }
    }

    // Prueft, ob die eingegebene ID wirklich zu einem offenen Antrag gehoert.
    private boolean istOffenerAntragInListe(List<UrlaubsantragDTO> antraege,
            int antragId) {
        for (UrlaubsantragDTO antrag : antraege) {
            if (antrag.getAntragId() == antragId) {
                return true;
            }
        }

        return false;
    }

    // Liest eine ganze Zahl und fragt bei falscher Eingabe erneut.
    private int leseInt(String text) {
        while (true) {
            System.out.print(text);

            if (scanner.hasNextInt()) {
                int zahl = scanner.nextInt();
                scanner.nextLine();
                return zahl;
            }

            System.out.println("Bitte eine ganze Zahl eingeben.");
            scanner.nextLine();
        }
    }

    private String leseText(String text) {
        System.out.print(text);
        return scanner.nextLine();
    }

    private void zeigeErgebnis(ErgebnisDTO ergebnis) {
        System.out.println();
        System.out.println("Erfolgreich: " + ergebnis.isErfolgreich());
        System.out.println("Status: " + ergebnis.getStatus());
        System.out.println("Meldung: " + ergebnis.getMeldung());

        if (ergebnis.getAntragId() != null) {
            System.out.println("Antrag-ID: " + ergebnis.getAntragId());
        }
    }

    private void zeigeAntrag(UrlaubsantragDTO antrag) {
        System.out.println("-------------------------");
        System.out.println("Antrag-ID: " + antrag.getAntragId());
        System.out.println("Zeitraum: Tag " + antrag.getStarttag()
                + " bis Tag " + antrag.getEndtag());
        System.out.println("Status: " + antrag.getStatus());
        System.out.println("Antragsteller-ID: " + antrag.getAntragstellerId());
        System.out.println("Vertretung-ID: " + antrag.getVertretungId());
        System.out.println("Genehmiger-ID: " + antrag.getGenehmigerId());
    }
}
