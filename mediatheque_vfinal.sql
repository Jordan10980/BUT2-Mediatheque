-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : dim. 04 juin 2023 à 22:33
-- Version du serveur : 10.4.28-MariaDB
-- Version de PHP : 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `mediatheque`
--

-- --------------------------------------------------------

--
-- Structure de la table `abonnes`
--

CREATE TABLE `abonnes` (
  `numero` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `dateNaissance` date NOT NULL,
  `bannedUntil` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `abonnes`
--

INSERT INTO `abonnes` (`numero`, `nom`, `dateNaissance`, `bannedUntil`) VALUES
(1, 'Martin Dupont', '1960-06-15', '0000-00-00'),
(2, 'Sophie Lefevre', '1962-07-24', '0000-00-00'),
(3, 'Paul Bernard', '1964-08-03', '0000-00-00'),
(4, 'Marie Dubois', '1966-09-12', '0000-00-00'),
(5, 'Jean Leroy', '1968-10-21', '0000-00-00'),
(6, 'Nicole Pelletier', '1970-11-30', '0000-00-00'),
(7, 'Francois Moreau', '1972-12-09', '0000-00-00'),
(8, 'Isabelle Girard', '1974-01-18', '0000-00-00'),
(9, 'Pierre Lemoine', '1976-02-27', '0000-00-00'),
(10, 'Nathalie Gauthier', '1978-03-08', '0000-00-00'),
(11, 'Andre Fontaine', '1980-04-17', '0000-00-00'),
(12, 'Danielle Rousseau', '1982-05-26', '0000-00-00'),
(13, 'Luc Marchand', '1984-06-05', '0000-00-00'),
(14, 'Claire Lacroix', '1986-07-14', '0000-00-00'),
(15, 'Philippe Fournier', '1988-08-23', '2023-06-21'),
(16, 'Dominique Lambert', '1990-10-02', '0000-00-00'),
(17, 'Serge Dufour', '1992-10-11', '0000-00-00'),
(18, 'Helene Gautier', '1994-11-20', '0000-00-00'),
(19, 'Louis Roy', '1996-11-29', '0000-00-00'),
(20, 'Denise Bertrand', '1998-12-08', '0000-00-00'),
(21, 'Michel Lemieux', '2000-12-17', '0000-00-00'),
(22, 'Sylvie Charpentier', '2000-12-26', '0000-00-00'),
(23, 'Patrick Lelievre', '2000-01-04', '0000-00-00'),
(24, 'Beatrice Mercier', '2000-02-13', '0000-00-00'),
(25, 'Jacques Leger', '2000-03-22', '0000-00-00');

-- --------------------------------------------------------

--
-- Structure de la table `dvds`
--

CREATE TABLE `dvds` (
  `numero` int(11) NOT NULL,
  `titre` varchar(100) NOT NULL,
  `empruntePar` int(11) NOT NULL,
  `reservePar` int(11) NOT NULL,
  `reservationTime` datetime DEFAULT '1000-01-01 00:00:00',
  `dateEmprunt` date NOT NULL,
  `dateRenduExcepte` date NOT NULL,
  `dateRenduReel` date NOT NULL,
  `estDegrade` tinyint(1) NOT NULL DEFAULT 0,
  `enReparation` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `dvds`
--

INSERT INTO `dvds` (`numero`, `titre`, `empruntePar`, `reservePar`, `reservationTime`, `dateEmprunt`, `dateRenduExcepte`, `dateRenduReel`, `estDegrade`, `enReparation`) VALUES
(1, 'Inception', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(2, 'Titanic', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(3, 'Pulp Fiction', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(4, 'The Shawshank Redemption', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(5, 'The Dark Knight', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(6, 'Fight Club', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(7, 'Forrest Gump', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(8, 'The Godfather', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(9, 'The Matrix', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(10, 'Gladiator', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(11, 'Interstellar', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(12, 'Saving Private Ryan', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(13, 'Schindler’s List', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(14, 'The Lord of the Rings: The Return of the King', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(15, 'The Lion King', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(16, 'The Silence of the Lambs', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(17, 'Jurassic Park', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(18, 'Braveheart', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(19, 'The Sixth Sense', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(20, 'Toy Story', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(21, 'The Truman Show', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(22, 'American Beauty', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(23, 'Good Will Hunting', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(24, 'The Green Mile', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0),
(25, 'Shrek', 0, 0, '1000-01-01 00:00:00', '0000-00-00', '0000-00-00', '0000-00-00', 0, 0);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `abonnes`
--
ALTER TABLE `abonnes`
  ADD PRIMARY KEY (`numero`);

--
-- Index pour la table `dvds`
--
ALTER TABLE `dvds`
  ADD PRIMARY KEY (`numero`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `abonnes`
--
ALTER TABLE `abonnes`
  MODIFY `numero` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT pour la table `dvds`
--
ALTER TABLE `dvds`
  MODIFY `numero` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
