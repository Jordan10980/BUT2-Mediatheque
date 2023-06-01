package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;

public class EmpruntServer implements Runnable{
    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:mysql://localhost:3306/mediatheque?zeroDateTimeBehavior=convertToNull";
    private static final String user = "root";
    private static final String password = ""; // your database password here

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1001)) {
            System.out.println("En attente de connexion client");

            // getting database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection con = DriverManager.getConnection(url, user, password)){

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connexion établie (Emprunt).");

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    String inputData = in.readUTF();

                    // Separate the subscriber ID and the DVD ID.
                    String[] inputDataParts = inputData.split(";");
                    int abonneId = Integer.parseInt(inputDataParts[0]);
                    int dvdId = Integer.parseInt(inputDataParts[1]);

                    // Check si la date de banissement et supérieur à la date d'aujourd'hui
                    PreparedStatement checkBanStatement = con.prepareStatement("SELECT bannedUntil FROM abonnes WHERE numero = ?");
                    checkBanStatement.setInt(1, abonneId);
                    ResultSet checkBanRs = checkBanStatement.executeQuery();

                    // Check si le dvd est en réparation ou pas
                    PreparedStatement dvdReparation = con.prepareStatement("SELECT enReparation FROM dvds WHERE numero = ?");
                    dvdReparation.setInt(1, dvdId);
                    ResultSet dvdReparationRs = dvdReparation.executeQuery();

                    // checking if DVD is already borrowed
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
                            serverResponse = "DVD is already borrowed.";
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
                        // borrowing the DVD
                        PreparedStatement updatePs = con.prepareStatement("UPDATE dvds SET empruntePar = ?, reservePar = 0 WHERE numero = ?");
                        updatePs.setInt(1, abonneId);
                        updatePs.setInt(2, dvdId);

                        // Update la date d'emprunt du dvd
                        PreparedStatement updateDateEmprunt = con.prepareStatement("UPDATE dvds SET dateEmprunt = ? WHERE numero = ?");
                        Date currentDate = Date.valueOf(LocalDate.now());
                        updateDateEmprunt.setDate(1, currentDate);
                        updateDateEmprunt.setInt(2, dvdId);

                        // Ajout de la date excepté (2 semaines)
                        PreparedStatement insertDateExcepte = con.prepareStatement("UPDATE dvds SET dateRenduExcepte = ? WHERE numero = ?");
                        LocalDate datePlusTwoWeeks = LocalDate.now().plusWeeks(2);
                        Date sqlDatePlusTwoWeeks = Date.valueOf(datePlusTwoWeeks); // Conversion de la date en javasql Date
                        insertDateExcepte.setDate(1, sqlDatePlusTwoWeeks);
                        insertDateExcepte.setInt(2, dvdId);

                        int rowsAffected = updatePs.executeUpdate();
                        int rowsAffectedDate = updateDateEmprunt.executeUpdate();
                        int rowsAffectedDateRenduExcepte = insertDateExcepte.executeUpdate();

                        if(rowsAffected > 0 && rowsAffectedDate > 0 && rowsAffectedDateRenduExcepte > 0){
                            serverResponse = "Borrowing successful. Vous devrez le rendre pour : " + datePlusTwoWeeks;
                        }
                        else{
                            serverResponse = "Borrowing failed.";
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
