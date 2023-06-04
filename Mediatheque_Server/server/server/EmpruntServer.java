package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;

public class EmpruntServer implements Runnable{
    // URL, nom d'utilisateur et mot de passe JDBC du serveur MySQL
    private static final String url = "jdbc:mysql://localhost:3306/mediatheque?zeroDateTimeBehavior=convertToNull";
    private static final String user = "root";
    private static final String password = "";

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1001)) {
            System.out.println("En attente de connexion client");

            // Obtenir la connexion à la base de données
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection con = DriverManager.getConnection(url, user, password)){

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connexion établie (Emprunt).");

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    String inputData = in.readUTF();

                    // Séparer l'ID de l'abonné et l'ID du DVD.
                    String[] inputDataParts = inputData.split(";");
                    int abonneId = Integer.parseInt(inputDataParts[0]);
                    int dvdId = Integer.parseInt(inputDataParts[1]);

                    // Vérifier si la date de bannissement est supérieure à la date actuelle
                    PreparedStatement checkBanStatement = con.prepareStatement("SELECT bannedUntil FROM abonnes WHERE numero = ?");
                    checkBanStatement.setInt(1, abonneId);
                    ResultSet checkBanRs = checkBanStatement.executeQuery();

                    // Vérifier si le DVD est en réparation
                    PreparedStatement dvdReparation = con.prepareStatement("SELECT enReparation FROM dvds WHERE numero = ?");
                    dvdReparation.setInt(1, dvdId);
                    ResultSet dvdReparationRs = dvdReparation.executeQuery();

                    // Vérifier si le DVD est déjà emprunté
                    PreparedStatement ps = con.prepareStatement("SELECT empruntePar FROM dvds WHERE numero = ?");
                    ps.setInt(1, dvdId);
                    ResultSet rs = ps.executeQuery();

                    String serverResponse = "";
                    boolean isBanned = false;
                    boolean isRepaired = false;
                    boolean isBorrowed = false;

                    if(rs.next()){
                        int empruntePar = rs.getInt("empruntePar");
                        if(empruntePar != 0){
                            serverResponse = "Le DVD est déjà emprunté.";
                            isBorrowed = true;
                        }
                    }

                    if(checkBanRs.next()) {
                        Date banDate = checkBanRs.getDate("bannedUntil");
                        if(banDate != null && banDate.toLocalDate().isAfter(LocalDate.now())) {
                            serverResponse = "Vous êtes banni jusqu'à " + banDate + ", vous ne pouvez pas réserver de DVD.";
                            isBanned = true;
                        }
                    }

                    if (dvdReparationRs.next()) {
                        int enReparation = dvdReparationRs.getInt("enReparation");
                        if (enReparation == 1) {
                            serverResponse = "Le dvd "+dvdId+" est en réparation" ;
                            isRepaired = true;
                        }
                    }

                    if(!isBorrowed && !isBanned && !isRepaired){
                        // Emprunter le DVD
                        PreparedStatement updatePs = con.prepareStatement("UPDATE dvds SET empruntePar = ?, reservePar = 0 WHERE numero = ?");
                        updatePs.setInt(1, abonneId);
                        updatePs.setInt(2, dvdId);

                        // Mettre à jour la date d'emprunt du DVD
                        PreparedStatement updateDateEmprunt = con.prepareStatement("UPDATE dvds SET dateEmprunt = ? WHERE numero = ?");
                        Date currentDate = Date.valueOf(LocalDate.now());
                        updateDateEmprunt.setDate(1, currentDate);
                        updateDateEmprunt.setInt(2, dvdId);

                        // Ajouter la date d'échéance (2 semaines)
                        PreparedStatement insertDateExcepte = con.prepareStatement("UPDATE dvds SET dateRenduExcepte = ? WHERE numero = ?");
                        LocalDate datePlusTwoWeeks = LocalDate.now().plusWeeks(2);
                        Date sqlDatePlusTwoWeeks = Date.valueOf(datePlusTwoWeeks); // Conversion de la date en java.sql.Date
                        insertDateExcepte.setDate(1, sqlDatePlusTwoWeeks);
                        insertDateExcepte.setInt(2, dvdId);

                        int rowsAffected = updatePs.executeUpdate();
                        int rowsAffectedDate = updateDateEmprunt.executeUpdate();
                        int rowsAffectedDateRenduExcepte = insertDateExcepte.executeUpdate();

                        if(rowsAffected > 0 && rowsAffectedDate > 0 && rowsAffectedDateRenduExcepte > 0){
                            serverResponse = "Emprunt réussi. Vous devrez le rendre pour : " + datePlusTwoWeeks;
                        }
                        else{
                            serverResponse = "Échec de l'emprunt.";
                        }
                    }

                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    out.writeUTF(serverResponse);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
