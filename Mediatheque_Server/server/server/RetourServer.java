package server;

import javax.mail.MessagingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class RetourServer implements Runnable {
    private static final String url = "jdbc:mysql://localhost:3306/mediatheque";
    private static final String user = "root";
    private static final String password = "";

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1002)) {
            System.out.println("En attente de connexion client");

            // Connexion à la bd
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, user, password)) {

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connexion établie (Retour).");

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    String inputData = in.readUTF();


                    String[] inputDataParts = inputData.split(";");
                    int abonneId = Integer.parseInt(inputDataParts[0]);
                    int dvdId = Integer.parseInt(inputDataParts[1]);

                    // Vérifier si le dvd est emprunté par un abonné
                    PreparedStatement ps = con.prepareStatement("SELECT empruntePar FROM dvds WHERE numero = ?");
                    ps.setInt(1, dvdId);
                    ResultSet rs = ps.executeQuery();

                    String serverResponse = "";
                    if (rs.next()) {
                        int empruntePar = rs.getInt("empruntePar");
                        if (empruntePar != abonneId) {
                            serverResponse = "Le DVD n'est pas emprunté par cet abonné.";
                        } else {
                            // Question pour vérifier si le DVD est dégradé
                            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                            out.writeUTF("Le DVD est-il dégradé ? Répondez par Y pour Oui, N pour Non.");

                            // Réponse du client
                            DataInputStream degradationResponse = new DataInputStream(clientSocket.getInputStream());
                            String isDegraded = degradationResponse.readUTF();

                            if ("Y".equalsIgnoreCase(isDegraded)) {
                                // Si le DVD est dégradé ban l'utilisateur pendant 1 mois
                                PreparedStatement banPs = con.prepareStatement("UPDATE abonnes SET bannedUntil = ? WHERE numero = ?");
                                LocalDate bannedUntil = LocalDate.now().plusMonths(1);
                                banPs.setDate(1, Date.valueOf(bannedUntil));
                                banPs.setInt(2, abonneId);
                                banPs.executeUpdate();

                                // Mettre le DVD en dégradé
                                PreparedStatement dvdDegrade = con.prepareStatement("UPDATE dvds SET estDegrade = 1 WHERE numero = ?");
                                dvdDegrade.setInt(1, dvdId);
                                dvdDegrade.executeUpdate();

                                // Mettre le DVD en réparation
                                PreparedStatement enReparation = con.prepareStatement("UPDATE dvds SET enReparation = 1 WHERE numero = ?");
                                enReparation.setInt(1, dvdId);
                                enReparation.executeUpdate();

                                serverResponse = "Le DVD est dégradé, vous êtes banni pendant un mois.";
                            } else {
                                // Vérifier si le retard est > 2 semaines
                                PreparedStatement ps2 = con.prepareStatement("SELECT dateRenduExcepte FROM dvds WHERE numero = ?");
                                ps2.setInt(1, dvdId);
                                ResultSet rs2 = ps2.executeQuery();

                                if (rs2.next()) {
                                    Date dateRenduExcepte = rs2.getDate("dateRenduExcepte");
                                    LocalDate localDateRenduExcepte = dateRenduExcepte.toLocalDate();

                                    LocalDate currentDate = LocalDate.now();
                                    long weeksBetween = ChronoUnit.WEEKS.between(localDateRenduExcepte, currentDate);

                                    if (weeksBetween > 2) {
                                        // Si la différence est > à 2 semaines, ban l'utilisateur
                                        PreparedStatement banPs = con.prepareStatement("UPDATE abonnes SET bannedUntil = ? WHERE numero = ?");
                                        LocalDate bannedUntil = LocalDate.now().plusMonths(1);
                                        banPs.setDate(1, Date.valueOf(bannedUntil));
                                        banPs.setInt(2, abonneId);
                                        int banRowsAffected = banPs.executeUpdate();
                                        if (banRowsAffected > 0) {
                                            serverResponse = "Vous avez plus de deux semaines de retard, vous êtes banni pendant 1 mois.";
                                        }
                                    }
                                }

                                // Retourner le DVD et annuler la  réservation
                                PreparedStatement updatePs = con.prepareStatement("UPDATE dvds SET empruntePar = 0, reservePar = 0, ReservationTime = '1000-01-01 00:00:00', dateEmprunt = '0000-00-00', dateRenduExcepte = '0000-00-00', dateRenduReel = '0000-00-00' WHERE numero = ?");
                                updatePs.setInt(1, dvdId);
                                int rowsAffected = updatePs.executeUpdate();

                                // Vérifier si le DVD est dégradé
                                if (rowsAffected > 0) {
                                    serverResponse = "Retour réussi.";

                                    // Vérifie si la case EnAttente est vide ou pas
                                    PreparedStatement ps3 = con.prepareStatement("SELECT EnAttente FROM dvds WHERE numero = ?");
                                    ps3.setInt(1, dvdId);
                                    ResultSet rs3 = ps3.executeQuery();

                                    if (rs3.next()) {
                                        String enAttente = rs3.getString("EnAttente");

                                        if (enAttente != null) {
                                            // Envoi de l'e-mail
                                            String emailSubject = "DVD disponible";
                                            String emailBody = "Le DVD que vous avez réservé est maintenant disponible.";

                                            // Configuration des informations SMTP pour Gmail
                                            String host = "smtp.gmail.com";
                                            int port = 587;
                                            String username = "testapi1098@gmail.com";
                                            String password = "ejaafgaydybxmayv";

                                            // Configuration des propriétés pour la connexion SMTP
                                            Properties props = new Properties();
                                            props.put("mail.smtp.auth", "true");
                                            props.put("mail.smtp.starttls.enable", "true");
                                            props.put("mail.smtp.host", host);
                                            props.put("mail.smtp.port", port);

                                            // Création d'une session avec l'authentification
                                            Session session = Session.getInstance(props, new Authenticator() {
                                                @Override
                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                    return new PasswordAuthentication(username, password);
                                                }
                                            });

                                            try {
                                                Message message = new MimeMessage(session);

                                                message.setFrom(new InternetAddress(username));
                                                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(enAttente));

                                                message.setSubject(emailSubject);
                                                message.setText(emailBody);

                                                // Envoi du message
                                                Transport.send(message);

                                                System.out.println("L'e-mail a été envoyé avec succès.");
                                            } catch (MessagingException e) {
                                                System.out.println("Une erreur s'est produite lors de l'envoi de l'e-mail : " + e.getMessage());
                                            }
                                        }

                                    }
                                } else {
                                    serverResponse = "Échec du retour.";
                                }
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
