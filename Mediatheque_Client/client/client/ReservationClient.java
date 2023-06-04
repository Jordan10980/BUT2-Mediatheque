package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ReservationClient implements Runnable {

    public void run() {
        try {
            // Établir une connexion avec le serveur sur localhost et le port 1000
            Socket socket = new Socket("localhost", 1000);

            // Demander à l'utilisateur l'ID de l'abonné
            System.out.println("ID de l'abonné : ");
            Scanner sc = new Scanner(System.in);
            String abonneId = sc.next();

            // Demander à l'utilisateur l'ID du DVD
            System.out.println("ID du DVD : ");
            String dvdId = sc.next();

            String outputData = abonneId + ";" + dvdId;

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(outputData);


            DataInputStream in = new DataInputStream(socket.getInputStream());
            String serverResponse = in.readUTF();
            System.out.println(serverResponse);

            // Si le serveur pose une question, le client répond à la question
            if (serverResponse.contains("Voulez-vous être alerté")) {
                System.out.println("Votre réponse : ");
                String clientResponse = sc.next();

                // Envoyer la réponse au serveur
                out.writeUTF(clientResponse);

                String finalResponse = in.readUTF();
                System.out.println(finalResponse);
            }

            // Fermer la connexion avec le serveur
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
