-- Testdaten fuer die Urlaubsverwaltung
-- Diese Datei legt nur einfache Beispieldaten an.
-- Es werden keine Tabellen geloescht oder neu erstellt.

USE ae_urlaubsantraege;

-- Zusaetzliche Mitarbeiter zum Testen.
-- INSERT IGNORE verhindert Fehler, wenn die IDs schon vorhanden sind.
INSERT IGNORE INTO mitarbeiter
    (Mitarbeiter_ID, Vorname, Nachname, Resturlaub, Vorgesetzter_ID)
VALUES
    (6, 'Paul', 'Test', 20, 1),
    (7, 'Mara', 'Vertretung', 20, 1),
    (8, 'Nils', 'Kollege', 20, 1);

-- Anna Meyer ist an Tag 8 bis 12 schon im Urlaub.
-- Dadurch kann man testen, dass ein Antrag offen bleibt,
-- wenn die Vertretung nicht verfuegbar ist.
INSERT INTO urlaubskalender
    (Mitarbeiter_ID, Starttag, Endtag)
SELECT 2, 8, 12
WHERE NOT EXISTS (
    SELECT 1
    FROM urlaubskalender
    WHERE Mitarbeiter_ID = 2
      AND Starttag = 8
      AND Endtag = 12
);
