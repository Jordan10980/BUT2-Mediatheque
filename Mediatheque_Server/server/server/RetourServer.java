package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RetourServer implements Runnable{
    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:mysql://localhost:3306/mediatheque";
    private static final String user = "root";
    private static final String password = ""; // your database password here

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1002)) {
            System.out.println("En attente de connexion client");

            // getting database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection con = DriverManager.getConnection(url, user, password)){

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connexion établie (Retour).");

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    String inputData = in.readUTF();

                    // Separate the subscriber ID and the DVD ID.
                    String[] inputDataParts = inputData.split(";");
                    int abonneId = Integer.parseInt(inputDataParts[0]);
                    int dvdId = Integer.parseInt(inputDataParts[1]);

                    // checking if DVD is borrowed by the subscriber
                    PreparedStatement ps = con.prepareStatement("SELECT empruntePar FROM dvds WHERE numero = ?");
                    ps.setInt(1, dvdId);
                    ResultSet rs = ps.executeQuery();

                    String serverResponse = "";
                    if(rs.next()){
                        int empruntePar = rs.getInt("empruntePar");
                        if(empruntePar != abonneId){
                            serverResponse = "DVD is not borrowed by this subscriber.";
                        }
                        else{

                            // Question pour vérifier si le dvd est dégradé
                            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                            out.writeUTF("Le DVD est-il dégradé? Répondez par Y pour Oui, N pour Non.");
                            // Réponse cliente
                            DataInputStream degradationResponse = new DataInputStream(clientSocket.getInputStream());
                            String isDegraded = degradationResponse.readUTF();

                            if ("Y".equalsIgnoreCase(isDegraded)) {
                                // If the DVD is degraded, ban the user for 1 month.
                                PreparedStatement banPs = con.prepareStatement("UPDATE abonnes SET bannedUntil = ? WHERE numero = ?");
                                LocalDate bannedUntil = LocalDate.now().plusMonths(1);
                                banPs.setDate(1, Date.valueOf(bannedUntil));
                                banPs.setInt(2, abonneId);
                                banPs.executeUpdate();

                                // Mettre le dvd en dégradé
                                PreparedStatement dvdDegrade = con.prepareStatement("UPDATE dvds SET estDegrade = 1 WHERE numero = ?");
                                dvdDegrade.setInt(1, dvdId);
                                dvdDegrade.executeUpdate();

                                // Mettre le dvd en réparation
                                PreparedStatement enReparation = con.prepareStatement("UPDATE dvds SET enReparation = 1 WHERE numero = ?");
                                enReparation.setInt(1, dvdId);
                                enReparation.executeUpdate();
                                serverResponse = "Le DVD est dégradé, vous êtes banni pendant un mois.";
                            }

                            //Check si le retard est > 2 semaines
                            PreparedStatement ps2 = con.prepareStatement("SELECT dateRenduExcepte FROM dvds WHERE numero = ?");
                            ps2.setInt(1, dvdId);
                            ResultSet rs2 = ps2.executeQuery();

                            if(rs2.next()){
                                Date dateRenduExcepte = rs2.getDate("dateRenduExcepte");
                                LocalDate localDateRenduExcepte = dateRenduExcepte.toLocalDate();

                                LocalDate currentDate = LocalDate.now();
                                long weeksBetween = ChronoUnit.WEEKS.between(localDateRenduExcepte, currentDate);

                                if(weeksBetween > 2){
                                    // If the difference is more than 2 weeks, ban the user
                                    PreparedStatement banPs = con.prepareStatement("UPDATE abonnes SET bannedUntil = ? WHERE id = ?");
                                    LocalDate bannedUntil = LocalDate.now().plusMonths(1);
                                    banPs.setDate(1, Date.valueOf(bannedUntil));
                                    banPs.setInt(2, abonneId);
                                    int banRowsAffected = banPs.executeUpdate();
                                    if(banRowsAffected > 0){
                                        serverResponse = "Vous avez plus de deux semaines de retard, vous êtes banni pendant 1 mois.";
                                    }
                                }
                            }


                            // returning the DVD, and also cancel the reservation if any
                            PreparedStatement updatePs = con.prepareStatement("UPDATE dvds SET empruntePar = 0, reservePar = 0, ReservationTime = '1000-01-01 00:00:00', dateEmprunt = '0000-00-00', dateRenduExcepte = '0000-00-00', dateRenduReel = '0000-00-00' WHERE numero = ?");
                            updatePs.setInt(1, dvdId);
                            int rowsAffected = updatePs.executeUpdate();


                            //Check si le dvd est degrade
                            if(rowsAffected > 0){
                                serverResponse = "Return successful.";
                            }
                            else{
                                serverResponse = "Return failed.";
                            }
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
