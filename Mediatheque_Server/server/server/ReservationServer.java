package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;
import java.security.MessageDigest;

public class ReservationServer implements Runnable {
    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:mysql://localhost:3306/mediatheque?zeroDateTimeBehavior=convertToNull";
    private static final String user = "root";
    private static final String password = ""; // your database password here

    private static final LocalDateTime DEFAULT_DATETIME = LocalDateTime.parse("1000-01-01T00:00:00");
    private ScheduledExecutorService executorService;

    private ConcurrentHashMap<Integer, Integer> waitingReservations;

    public ReservationServer() {
        this.executorService = Executors.newScheduledThreadPool(10); // Adjust the pool size according to your need
        this.waitingReservations = new ConcurrentHashMap<>();
    }

    // Music player
    private void playWaitingMusic(int duration, int dvdId) {
        executorService.execute(() -> {
            try {
                // Assuming the file is in the project root.
                File musicPath = new File(System.getProperty("user.dir") + "/server/server/attente.wav");

                if (musicPath.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInput);
                    clip.start();

                    // Adjusted sleep duration
                    Thread.sleep(duration * 1000);

                    clip.stop();
                } else {
                    System.out.println("Can't find file");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private int getMusicDurationInSeconds() {
        // TODO: Implement this based on your music file and library
        return 30; // placeholder return value
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(1000)) {
            System.out.println("En attente de connexion client");

            // getting database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, user, password)) {

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connexion établie (Reservation).");

                    // Create a new thread for each request
                    new Thread(() -> {
                        try {
                            handleRequest(clientSocket, con);
                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket clientSocket, Connection con) throws IOException, SQLException {
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        String inputData = in.readUTF();

        String[] inputDataParts = inputData.split(";");
        int abonneId = Integer.parseInt(inputDataParts[0]);
        int dvdId = Integer.parseInt(inputDataParts[1]);

        PreparedStatement ps = con.prepareStatement("SELECT reservePar, reservationTime FROM dvds WHERE numero = ?");
        ps.setInt(1, dvdId);
        ResultSet rs = ps.executeQuery();

        PreparedStatement checkBanStatement = con.prepareStatement("SELECT bannedUntil FROM abonnes WHERE numero = ?");
        checkBanStatement.setInt(1, abonneId);
        ResultSet checkBanRs = checkBanStatement.executeQuery();

        PreparedStatement dvdReparation = con.prepareStatement("SELECT enReparation FROM dvds WHERE numero = ?");
        dvdReparation.setInt(1, dvdId);
        ResultSet dvdReparationRs = dvdReparation.executeQuery();

        PreparedStatement checkPs = con.prepareStatement("SELECT empruntePar FROM dvds WHERE numero = ?");
        checkPs.setInt(1, dvdId);
        ResultSet checkRs = checkPs.executeQuery();


        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        String serverResponse = "";
        String serverResponseBanDate = "";
        boolean isBanned = false;
        boolean isRepaired = false;
        boolean isBorrowed = false;
        boolean wantAlert = false;

        if(checkBanRs.next()) {
            Date banDate = checkBanRs.getDate("bannedUntil");
            if(banDate != null && banDate.toLocalDate().isAfter(LocalDate.now())) {
                serverResponseBanDate = "Vous êtes banni jusqu'à " + banDate + ", vous ne pouvez pas réserver de DVD.";
                isBanned = true;
            }
        }

        if (dvdReparationRs.next()) {
            int enReparation = dvdReparationRs.getInt("enReparation");
            if (enReparation == 1) {
                isRepaired = true;
            }
        }

        if (checkRs.next() && checkRs.getInt("empruntePar") != 0) {
            isBorrowed = true;
            System.out.println("Malheureusement, le DVD a été emprunté. Vous avez quand même pu recevoir ce concert celeste gratuitement !");
            out.writeUTF("Voulez-vous être alerté quand il sera disponible ? Répondez par votre email pour Oui, N pour Non.");
            DataInputStream alerteResponse = new DataInputStream(clientSocket.getInputStream());
            String isAlerted = alerteResponse.readUTF();


            if (!"N".equalsIgnoreCase(isAlerted)) {
                PreparedStatement psUpdate = con.prepareStatement("UPDATE dvds SET EnAttente = ? WHERE numero = ?");
                psUpdate.setString(1, isAlerted); // Pour l'instant on ne met pas hashedEmail car on arrive pas à déhasher le sha1 car normalement ce n'est pas possible ou très compliqué
                psUpdate.setInt(2, dvdId);
                psUpdate.executeUpdate();
                serverResponse = "Vous allez être alerté";
                wantAlert = true;

            } else {
                serverResponse = "Désolé vous ne serez pas alerté";
            }
        }


        if (rs.next() && !isBanned && !isRepaired && !isBorrowed) {
            int reservePar = rs.getInt("reservePar");
            LocalDateTime reservationTime = rs.getTimestamp("reservationTime").toLocalDateTime();

            if (reservePar != 0 && !reservationTime.isEqual(DEFAULT_DATETIME)) {
                if (reservationTime.isBefore(LocalDateTime.now().minusSeconds(60))) {
                    // Reservation has expired, release the DVD.
                    PreparedStatement updatePs = con.prepareStatement("UPDATE dvds SET reservePar = ?, reservationTime = ? WHERE numero = ?");
                    updatePs.setInt(1, 0);
                    updatePs.setTimestamp(2, Timestamp.valueOf(DEFAULT_DATETIME));
                    updatePs.setInt(3, dvdId);
                    int rowsAffected = updatePs.executeUpdate();
                    if (rowsAffected > 0) {
                        // Reservation released successfully.
                        System.out.println("Reservation released for DVD number " + dvdId);
                        // The DVD is now available for reservation.
                        reservePar = 0;
                        reservationTime = DEFAULT_DATETIME;
                    }
                } else {
                    // Reservation has not yet expired.
                    long secondsUntilExpiry = Duration.between(LocalDateTime.now(), reservationTime.plusSeconds(60)).getSeconds();
                    if (secondsUntilExpiry <= 30) {
                        waitingReservations.put(dvdId, abonneId);
                        out.writeUTF("Il reste moins de 30secondes à la réservation en cours, veuillez patienter avec cette petite musique.");
                        int musicDuration = getMusicDurationInSeconds(); // Implement this function based on your music file
                        executorService.schedule(() -> {
                            try (Connection newCon = DriverManager.getConnection(url, user, password)) {
                                Integer waitingAbonneId = waitingReservations.remove(dvdId);
                                if (waitingAbonneId != null) {
                                    PreparedStatement newPs = newCon.prepareStatement("UPDATE dvds SET reservePar = ?, reservationTime = CURRENT_TIMESTAMP WHERE numero = ?");
                                    newPs.setInt(1, waitingAbonneId);
                                    newPs.setInt(2, dvdId);
                                    newPs.executeUpdate();
                                    System.out.println("Updating reservation for DVD " + dvdId + " to abonne " + waitingAbonneId);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }, musicDuration, TimeUnit.SECONDS);
                        playWaitingMusic((int) secondsUntilExpiry, dvdId);
                    } else {
                        out.writeUTF("La réservation précédente n'a pas encore expiré. Veuillez réessayer dans " + secondsUntilExpiry + " secondes.");
                    }
                }
            }

            if (reservePar == 0 && reservationTime.isEqual(DEFAULT_DATETIME)) {
                // Reserving the DVD.
                PreparedStatement updatePs = con.prepareStatement("UPDATE dvds SET reservePar = ?, reservationTime = CURRENT_TIMESTAMP WHERE numero = ?");
                updatePs.setInt(1, abonneId);
                updatePs.setInt(2, dvdId);
                int rowsAffected = updatePs.executeUpdate();
                if (rowsAffected > 0) {
                    executorService.schedule(() -> {
                        try (Connection newCon = DriverManager.getConnection(url, user, password)) {
                            Integer waitingAbonneId = waitingReservations.remove(dvdId);
                            PreparedStatement newPs;
                            if (waitingAbonneId != null) {
                                newPs = newCon.prepareStatement("UPDATE dvds SET reservePar = ?, reservationTime = CURRENT_TIMESTAMP WHERE numero = ?");
                                newPs.setInt(1, waitingAbonneId);
                                newPs.setInt(2, dvdId);
                            } else {
                                newPs = newCon.prepareStatement("UPDATE dvds SET reservePar = 0, reservationTime = ? WHERE numero = ?");
                                newPs.setTimestamp(1, Timestamp.valueOf(DEFAULT_DATETIME));
                                newPs.setInt(2, dvdId);
                            }
                            newPs.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }, 60, TimeUnit.SECONDS);

                    serverResponse = "DVD réservé avec succès.";
                } else {
                    serverResponse = "Erreur lors de la réservation du DVD.";
                }
            } else {
                serverResponse = "Le DVD est actuellement réservé. Veuillez réessayer plus tard.";
            }
        } else if (isBanned == true) {
            serverResponse = serverResponseBanDate;
        } else if (isRepaired == true) {
            serverResponse = "Le dvd " + dvdId + " est en réparation";
        } else if (isBorrowed == true && wantAlert == true) {
            serverResponse = "Le dvd " + dvdId + " est déjà emprunté mais vous serez alerté par email lors de sa disponibilité";
        } else if (isBorrowed == true && wantAlert != true) {
            serverResponse = "Le dvd " + dvdId + " est déjà emprunté";
        } else {
            serverResponse = "DVD non trouvé.";
        }
        out.writeUTF(serverResponse);
    }
}

