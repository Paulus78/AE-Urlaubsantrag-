-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Erstellungszeit: 14. Jun 2026 um 11:42
-- Server-Version: 12.3.2-MariaDB
-- PHP-Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `ae urlaubsanträge`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mitarbeiter`
--

CREATE TABLE `mitarbeiter` (
  `Mitarbeiter_ID` int(20) NOT NULL AUTO_INCREMENT,
  `Vorname` varchar(30) NOT NULL,
  `Nachname` varchar(30) NOT NULL,
  `Resturlaub` int(3) NOT NULL,
  `Vorgesetzter_ID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Daten für Tabelle `mitarbeiter`
--

INSERT INTO `mitarbeiter` (`Mitarbeiter_ID`, `Vorname`, `Nachname`, `Resturlaub`, `Vorgesetzter_ID`) VALUES
(1, 'Max', 'Chef', 30, NULL),
(2, 'Anna', 'Meyer', 25, 1),
(3, 'Tom', 'Schmidt', 28, 1),
(4, 'Lisa', 'Bauer', 20, 1),
(5, 'Tim', 'Weber', 22, 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `urlaubsantrag`
--

CREATE TABLE `urlaubsantrag` (
  `Antrag_ID` int(20) NOT NULL AUTO_INCREMENT,
  `Starttag` int(10) NOT NULL,
  `Endtag` int(10) NOT NULL,
  `Status` varchar(20) NOT NULL,
  `Angestellter_ID` int(20) NOT NULL,
  `Vertretung_ID` int(20) NOT NULL,
  `Genehmiger_ID` int(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `urlaubskalender`
--

CREATE TABLE `urlaubskalender` (
  `Kalender_ID` int(20) NOT NULL AUTO_INCREMENT,
  `Mitarbeiter_ID` int(20) NOT NULL,
  `Starttag` int(20) NOT NULL,
  `Endtag` int(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `mitarbeiter`
--
ALTER TABLE `mitarbeiter`
  ADD PRIMARY KEY (`Mitarbeiter_ID`);

--
-- Indizes für die Tabelle `urlaubsantrag`
--
ALTER TABLE `urlaubsantrag`
  ADD PRIMARY KEY (`Antrag_ID`),
  ADD KEY `idx_urlaubsantrag_genehmiger` (`Genehmiger_ID`),
  ADD KEY `idx_urlaubsantrag_vertretung` (`Vertretung_ID`),
  ADD KEY `idx_urlaubsantrag_antragsteller` (`Angestellter_ID`);

--
-- Indizes für die Tabelle `urlaubskalender`
--
ALTER TABLE `urlaubskalender`
  ADD PRIMARY KEY (`Kalender_ID`),
  ADD KEY `idx_urlaubsKalender_mitarbeiter` (`Mitarbeiter_ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
