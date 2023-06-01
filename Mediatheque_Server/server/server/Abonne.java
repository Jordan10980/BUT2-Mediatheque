package server;

import java.time.LocalDate;

public class Abonne {
    private int numero;
    private String nom;
    private LocalDate dateNaissance;

    public Abonne(int numero, String nom, LocalDate dateNaissance) {
        this.numero = numero;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
    }

    public int getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    // Add setters if necessary
}
