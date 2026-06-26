package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import dto.EntscheidungDTO;
import dto.ErgebnisDTO;
import dto.UrlaubsantragDTO;
import dto.UrlaubsantragEingabeDTO;

// Remote-Interface fuer den UrlaubsService.
// Das ist der gemeinsame Vertrag zwischen Client und Server.
public interface IUrlaubsService extends Remote {

    // Wird aufgerufen, wenn ein Mitarbeiter Urlaub beantragt.
    ErgebnisDTO urlaubBeantragen(UrlaubsantragEingabeDTO eingabe)
            throws RemoteException;

    // Wird aufgerufen, wenn ein offener Antrag genehmigt oder abgelehnt wird.
    ErgebnisDTO vorgesetztenentscheidungVerarbeiten(EntscheidungDTO entscheidung)
            throws RemoteException;

    // Liefert die offenen Antraege fuer die spaetere Anzeige in der UI.
    List<UrlaubsantragDTO> offeneAntraegeAnzeigen() throws RemoteException;
}
