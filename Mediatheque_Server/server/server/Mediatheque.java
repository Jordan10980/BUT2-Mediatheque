package server;

import server.Abonne;
import server.Document;

import java.util.HashMap;
import java.util.Map;

public class Mediatheque {
    private Map<Integer, Abonne> abonnes;
    private Map<Integer, Document> documents;

    public Mediatheque() {
        abonnes = new HashMap<>();
        documents = new HashMap<>();
    }

    public void ajouterAbonne(Abonne abonne) {
        abonnes.put(abonne.getNumero(), abonne);
    }

    public void ajouterDocument(Document document) {
        documents.put(document.numero(), document);
    }

    public String reserver(int numeroAbonne, int numeroDocument) {
        Abonne abonne = abonnes.get(numeroAbonne);
        Document document = documents.get(numeroDocument);

        if (abonne == null || document == null) {
            return "Abonné ou document introuvable.";
        }

        if (document.reservePar() != null) {
            return "Ce document est déjà réservé.";
        }

        if (document.empruntePar() != null) {
            return "Ce document est actuellement emprunté.";
        }

        document.reservation(abonne);
        return "Réservation effectuée avec succès.";
    }

    public String emprunter(int numeroAbonne, int numeroDocument) {
        Abonne abonne = abonnes.get(numeroAbonne);
        Document document = documents.get(numeroDocument);

        if (abonne == null || document == null) {
            return "Abonné ou document introuvable.";
        }

        if (document.empruntePar() != null) {
            return "Ce document est actuellement emprunté.";
        }

        document.emprunt(abonne);
        return "Emprunt effectué avec succès.";
    }

    public String retour(int numeroAbonne, int numeroDocument) {
        Abonne abonne = abonnes.get(numeroAbonne);
        Document document = documents.get(numeroDocument);

        if (abonne == null || document == null) {
            return "Abonné ou document introuvable.";
        }

        if (document.empruntePar() == null || !document.empruntePar().equals(abonne)) {
            return "Ce document n'a pas été emprunté par cet abonné.";
        }

        document.retour();
        return "Retour effectué avec succès.";
    }

}
