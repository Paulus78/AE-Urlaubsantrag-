package service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.IKalenderDAO;
import dao.IMitarbeiterDAO;
import dao.IUrlaubsantragDAO;
import dao.KalenderDAO;
import dao.MitarbeiterDAO;
import dao.UrlaubsantragDAO;
import dto.EntscheidungDTO;
import dto.ErgebnisDTO;
import dto.UrlaubsantragDTO;
import dto.UrlaubsantragEingabeDTO;
import model.Kalendereintrag;
import model.Mitarbeiter;
import model.Urlaubsantrag;

// Einfache RMI-Implementierung des UrlaubsService.
// Die Fachlogik kommt in diese Klasse, nicht in UI oder DAO.
public class UrlaubsService extends UnicastRemoteObject implements IUrlaubsService {

    private IUrlaubsantragDAO urlaubsantragDAO;
    private IMitarbeiterDAO mitarbeiterDAO;
    private IKalenderDAO kalenderDAO;

    public UrlaubsService() throws RemoteException {
        super();
        urlaubsantragDAO = new UrlaubsantragDAO();
        mitarbeiterDAO = new MitarbeiterDAO();
        kalenderDAO = new KalenderDAO();
    }

    // Beantragt Urlaub und entscheidet nach den Regeln aus der Spezifikation.
    @Override
    public ErgebnisDTO urlaubBeantragen(UrlaubsantragEingabeDTO eingabe)
            throws RemoteException {
        try {
            ErgebnisDTO pruefErgebnis = pruefeEingabe(eingabe);

            if (pruefErgebnis != null) {
                return pruefErgebnis;
            }

            Mitarbeiter antragsteller =
                    mitarbeiterDAO.findeNachId(eingabe.getAntragstellerId());
            Mitarbeiter vertretung =
                    mitarbeiterDAO.findeNachId(eingabe.getVertretungId());

            pruefErgebnis = pruefeMitarbeiter(eingabe, antragsteller, vertretung);

            if (pruefErgebnis != null) {
                return pruefErgebnis;
            }

            int urlaubstage = berechneUrlaubstage(
                    eingabe.getStarttag(), eingabe.getEndtag());

            if (urlaubstage <= 0) {
                return new ErgebnisDTO(false, null,
                        "Im Zeitraum liegen keine Urlaubstage.", null);
            }

            if (antragsteller.getResturlaub() < urlaubstage) {
                return new ErgebnisDTO(false, null,
                        "Der Resturlaub reicht fuer diesen Antrag nicht aus.",
                        null);
            }

            boolean antragstellerHatUrlaub = kalenderDAO.hatUeberschneidung(
                    eingabe.getAntragstellerId(),
                    eingabe.getStarttag(),
                    eingabe.getEndtag());

            if (antragstellerHatUrlaub) {
                return new ErgebnisDTO(false, null,
                        "Der Antragsteller hat in diesem Zeitraum schon Urlaub.",
                        null);
            }

            boolean vertretungHatUrlaub = kalenderDAO.hatUeberschneidung(
                    eingabe.getVertretungId(),
                    eingabe.getStarttag(),
                    eingabe.getEndtag());

            if (!vertretungHatUrlaub) {
                return speichereGenehmigtenAntrag(
                        eingabe, antragsteller, urlaubstage);
            }

            return speichereOffenenOderAbgelehntenAntrag(eingabe, antragsteller);
        } catch (SQLException exception) {
            throw new RemoteException(
                    "Urlaubsantrag konnte nicht verarbeitet werden.", exception);
        }
    }

    // Verarbeitet die Entscheidung fuer einen offenen Urlaubsantrag.
    @Override
    public ErgebnisDTO vorgesetztenentscheidungVerarbeiten(
            EntscheidungDTO entscheidung) throws RemoteException {
        try {
            if (entscheidung == null) {
                return new ErgebnisDTO(false, null,
                        "Es wurde keine Entscheidung uebergeben.", null);
            }

            if (entscheidung.getAntragId() <= 0) {
                return new ErgebnisDTO(false, null,
                        "Die Antrag-ID ist ungueltig.", null);
            }

            Urlaubsantrag antrag =
                    urlaubsantragDAO.findeNachId(entscheidung.getAntragId());

            ErgebnisDTO pruefErgebnis = pruefeOffenenAntrag(antrag);

            if (pruefErgebnis != null) {
                return pruefErgebnis;
            }

            Mitarbeiter antragsteller =
                    mitarbeiterDAO.findeNachId(antrag.getAntragstellerId());

            pruefErgebnis = pruefeGenehmiger(antrag, antragsteller);

            if (pruefErgebnis != null) {
                return pruefErgebnis;
            }

            if (entscheidung.isGenehmigt()) {
                return genehmigeOffenenAntrag(antrag, antragsteller);
            }

            return lehneOffenenAntragAb(antrag);
        } catch (SQLException exception) {
            throw new RemoteException(
                    "Vorgesetztenentscheidung konnte nicht verarbeitet werden.",
                    exception);
        }
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

    // Prueft einfache Eingabefehler, bevor die Datenbank benutzt wird.
    private ErgebnisDTO pruefeEingabe(UrlaubsantragEingabeDTO eingabe) {
        if (eingabe == null) {
            return new ErgebnisDTO(false, null,
                    "Es wurden keine Eingabedaten uebergeben.", null);
        }

        if (eingabe.getAntragstellerId() <= 0) {
            return new ErgebnisDTO(false, null,
                    "Die Antragsteller-ID ist ungueltig.", null);
        }

        if (eingabe.getVertretungId() <= 0) {
            return new ErgebnisDTO(false, null,
                    "Die Vertretung-ID ist ungueltig.", null);
        }

        if (eingabe.getStarttag() <= 0 || eingabe.getEndtag() <= 0) {
            return new ErgebnisDTO(false, null,
                    "Starttag und Endtag muessen groesser als 0 sein.", null);
        }

        if (eingabe.getEndtag() < eingabe.getStarttag()) {
            return new ErgebnisDTO(false, null,
                    "Der Endtag darf nicht vor dem Starttag liegen.", null);
        }

        return null;
    }

    // Prueft, ob die beteiligten Mitarbeiter vorhanden und sinnvoll sind.
    private ErgebnisDTO pruefeMitarbeiter(UrlaubsantragEingabeDTO eingabe,
            Mitarbeiter antragsteller, Mitarbeiter vertretung) {
        if (antragsteller == null) {
            return new ErgebnisDTO(false, null,
                    "Der Antragsteller wurde nicht gefunden.", null);
        }

        if (vertretung == null) {
            return new ErgebnisDTO(false, null,
                    "Die Vertretung wurde nicht gefunden.", null);
        }

        if (eingabe.getAntragstellerId() == eingabe.getVertretungId()) {
            return new ErgebnisDTO(false, null,
                    "Der Antragsteller darf nicht seine eigene Vertretung sein.",
                    null);
        }

        return null;
    }

    // Prueft, ob der Antrag existiert und wirklich noch offen ist.
    private ErgebnisDTO pruefeOffenenAntrag(Urlaubsantrag antrag) {
        if (antrag == null) {
            return new ErgebnisDTO(false, null,
                    "Der Urlaubsantrag wurde nicht gefunden.", null);
        }

        if (!Urlaubsantrag.STATUS_OFFEN.equals(antrag.getStatus())) {
            return new ErgebnisDTO(false, antrag.getStatus(),
                    "Nur offene Antraege koennen entschieden werden.",
                    antrag.getAntragId());
        }

        return null;
    }

    // Prueft, ob der gespeicherte Genehmiger zum Vorgesetzten passt.
    private ErgebnisDTO pruefeGenehmiger(Urlaubsantrag antrag,
            Mitarbeiter antragsteller) {
        if (antragsteller == null) {
            return new ErgebnisDTO(false, null,
                    "Der Antragsteller wurde nicht gefunden.",
                    antrag.getAntragId());
        }

        Integer vorgesetzterId = antragsteller.getVorgesetzterId();
        Integer genehmigerId = antrag.getGenehmigerId();

        if (vorgesetzterId == null || genehmigerId == null) {
            return new ErgebnisDTO(false, null,
                    "Der Antrag hat keinen gueltigen Vorgesetzten.",
                    antrag.getAntragId());
        }

        if (!vorgesetzterId.equals(genehmigerId)) {
            return new ErgebnisDTO(false, null,
                    "Der Genehmiger ist nicht der direkte Vorgesetzte.",
                    antrag.getAntragId());
        }

        return null;
    }

    // Speichert einen Antrag, der wegen freier Vertretung direkt genehmigt wird.
    private ErgebnisDTO speichereGenehmigtenAntrag(
            UrlaubsantragEingabeDTO eingabe, Mitarbeiter antragsteller,
            int urlaubstage) throws SQLException {
        Urlaubsantrag antrag = new Urlaubsantrag(
                0,
                eingabe.getStarttag(),
                eingabe.getEndtag(),
                Urlaubsantrag.STATUS_GENEHMIGT,
                eingabe.getAntragstellerId(),
                eingabe.getVertretungId(),
                antragsteller.getVorgesetzterId());

        int antragId = urlaubsantragDAO.speichern(antrag);

        Kalendereintrag eintrag = new Kalendereintrag(
                0,
                eingabe.getAntragstellerId(),
                eingabe.getStarttag(),
                eingabe.getEndtag());

        kalenderDAO.speichern(eintrag);

        int neuerResturlaub = antragsteller.getResturlaub() - urlaubstage;
        mitarbeiterDAO.resturlaubAktualisieren(
                eingabe.getAntragstellerId(), neuerResturlaub);

        return new ErgebnisDTO(true, Urlaubsantrag.STATUS_GENEHMIGT,
                "Der Antrag wurde automatisch genehmigt.", antragId);
    }

    // Wenn die Vertretung nicht frei ist, wird der Antrag offen gespeichert
    // oder ohne Vorgesetzten direkt abgelehnt.
    private ErgebnisDTO speichereOffenenOderAbgelehntenAntrag(
            UrlaubsantragEingabeDTO eingabe, Mitarbeiter antragsteller)
            throws SQLException {
        Integer genehmigerId = antragsteller.getVorgesetzterId();

        if (genehmigerId == null) {
            return new ErgebnisDTO(false, Urlaubsantrag.STATUS_ABGELEHNT,
                    "Ohne freie Vertretung und ohne Vorgesetzten kann der Antrag "
                            + "nicht genehmigt werden.",
                    null);
        }

        Urlaubsantrag antrag = new Urlaubsantrag(
                0,
                eingabe.getStarttag(),
                eingabe.getEndtag(),
                Urlaubsantrag.STATUS_OFFEN,
                eingabe.getAntragstellerId(),
                eingabe.getVertretungId(),
                genehmigerId);

        int antragId = urlaubsantragDAO.speichern(antrag);

        return new ErgebnisDTO(true, Urlaubsantrag.STATUS_OFFEN,
                "Der Antrag wurde gespeichert und wartet auf Entscheidung.",
                antragId);
    }

    // Genehmigt einen offenen Antrag und traegt den Urlaub ein.
    private ErgebnisDTO genehmigeOffenenAntrag(Urlaubsantrag antrag,
            Mitarbeiter antragsteller) throws SQLException {
        int urlaubstage = berechneUrlaubstage(
                antrag.getStarttag(), antrag.getEndtag());

        if (antragsteller.getResturlaub() < urlaubstage) {
            return new ErgebnisDTO(false, null,
                    "Der Resturlaub reicht fuer die Genehmigung nicht aus.",
                    antrag.getAntragId());
        }

        boolean hatSchonUrlaub = kalenderDAO.hatUeberschneidung(
                antrag.getAntragstellerId(),
                antrag.getStarttag(),
                antrag.getEndtag());

        if (hatSchonUrlaub) {
            return new ErgebnisDTO(false, null,
                    "Der Antragsteller hat in diesem Zeitraum schon Urlaub.",
                    antrag.getAntragId());
        }

        urlaubsantragDAO.statusAktualisieren(
                antrag.getAntragId(), Urlaubsantrag.STATUS_GENEHMIGT);

        Kalendereintrag eintrag = new Kalendereintrag(
                0,
                antrag.getAntragstellerId(),
                antrag.getStarttag(),
                antrag.getEndtag());

        kalenderDAO.speichern(eintrag);

        int neuerResturlaub = antragsteller.getResturlaub() - urlaubstage;
        mitarbeiterDAO.resturlaubAktualisieren(
                antrag.getAntragstellerId(), neuerResturlaub);

        return new ErgebnisDTO(true, Urlaubsantrag.STATUS_GENEHMIGT,
                "Der Antrag wurde genehmigt.", antrag.getAntragId());
    }

    // Lehnt einen offenen Antrag ab. Kalender und Resturlaub bleiben unveraendert.
    private ErgebnisDTO lehneOffenenAntragAb(Urlaubsantrag antrag)
            throws SQLException {
        urlaubsantragDAO.statusAktualisieren(
                antrag.getAntragId(), Urlaubsantrag.STATUS_ABGELEHNT);

        return new ErgebnisDTO(true, Urlaubsantrag.STATUS_ABGELEHNT,
                "Der Antrag wurde abgelehnt.", antrag.getAntragId());
    }

    // Tag 1 ist Montag. Samstag und Sonntag zaehlen nicht als Urlaubstage.
    private int berechneUrlaubstage(int starttag, int endtag) {
        int urlaubstage = 0;

        for (int tag = starttag; tag <= endtag; tag++) {
            if (!istWochenende(tag)) {
                urlaubstage++;
            }
        }

        return urlaubstage;
    }

    private boolean istWochenende(int tag) {
        int wochentag = tag % 7;
        return wochentag == 6 || wochentag == 0;
    }
}
