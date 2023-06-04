package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RetournerClient implements Runnable {

    public void run() {
        try {
            // Établir une connexion avec le serveur sur localhost et le port 1002
            Socket socket = new Socket("localhost", 1002);

            Scanner sc = new Scanner(System.in);

            // Demander à l'utilisateur son numéro d'abonné
            System.out.println("Veuillez entrer votre numéro d'abonné :");
            String numeroAbonne = sc.next();

            // Demander à l'utilisateur le numéro du DVD qu'il retourne
            System.out.println("Veuillez entrer le numéro du DVD que vous retournez :");
            String numeroDVD = sc.next();

            String outData = numeroAbonne + ";" + numeroDVD;
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(outData);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            String serverResponse = in.readUTF();
            System.out.println(serverResponse);

            // Si le serveur pose une question, le client répond à la question
            if (serverResponse.contains("Le DVD est-il dégradé ? Répondez par Y pour Oui, N pour Non.")) {
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


