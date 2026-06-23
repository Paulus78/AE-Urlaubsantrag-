package service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.IUrlaubsantragDAO;
import dao.UrlaubsantragDAO;
import dto.EntscheidungDTO;
import dto.ErgebnisDTO;
import dto.UrlaubsantragDTO;
import dto.UrlaubsantragEingabeDTO;
import model.Urlaubsantrag;

// Einfache RMI-Implementierung des UrlaubsService.
// Die Fachlogik kommt in diese Klasse, nicht in UI oder DAO.
public class UrlaubsService extends UnicastRemoteObject implements IUrlaubsService {

    private IUrlaubsantragDAO urlaubsantragDAO;

    public UrlaubsService() throws RemoteException {
        super();
        urlaubsantragDAO = new UrlaubsantragDAO();
    }

    // TODO: Die komplette Logik zum Beantragen bauen wir im naechsten Schritt.
    @Override
    public ErgebnisDTO urlaubBeantragen(UrlaubsantragEingabeDTO eingabe)
            throws RemoteException {
        return new ErgebnisDTO(false, null,
                "Urlaub beantragen ist noch nicht umgesetzt.", null);
    }

    // TODO: Die Entscheidungslogik bauen wir spaeter nach der Spezifikation.
    @Override
    public ErgebnisDTO vorgesetztenentscheidungVerarbeiten(
            EntscheidungDTO entscheidung) throws RemoteException {
        return new ErgebnisDTO(false, null,
                "Vorgesetztenentscheidung ist noch nicht umgesetzt.", null);
    }

    // Holt offene Antraege aus der DAO-Schicht und wandelt sie in DTOs um.
    @Override
    public List<UrlaubsantragDTO> offeneAntraegeAnzeigen()
            throws RemoteException {
        List<UrlaubsantragDTO> dtoListe = new ArrayList<>();

        try {
            List<Urlaubsantrag> antraege =
                    urlaubsantragDAO.findeOffeneAntraege();

            for (Urlaubsantrag antrag : antraege) {
                dtoListe.add(erstelleDTO(antrag));
            }
        } catch (SQLException exception) {
            throw new RemoteException(
                    "Offene Antraege konnten nicht geladen werden.", exception);
        }

        return dtoListe;
    }

    // Baut aus dem Datenobjekt ein DTO fuer die UI/RMI-Uebertragung.
    private UrlaubsantragDTO erstelleDTO(Urlaubsantrag antrag) {
        return new UrlaubsantragDTO(
                antrag.getAntragId(),
                antrag.getStarttag(),
                antrag.getEndtag(),
                antrag.getStatus(),
                antrag.getAntragstellerId(),
                antrag.getVertretungId(),
                antrag.getGenehmigerId());
    }
}
