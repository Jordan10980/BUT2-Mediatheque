package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class EmprunterClient implements Runnable {

    public void run() {
        try {
            // Établir une connexion avec le serveur sur localhost et le port 1001
            Socket socket = new Socket("localhost", 1001);

            // Obtenir l'ID de l'abonné et l'ID du DVD de l'utilisateur
            Scanner sc = new Scanner(System.in);
            System.out.println("Numéro de l'abonné pour l'emprunt : ");
            String numeroAbonne = sc.next();
            System.out.println("Numéro du DVD pour l'emprunt : ");
            String numeroDVD = sc.next();

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(numeroAbonne + ";" + numeroDVD);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            String serverResponse = in.readUTF();

            System.out.println(serverResponse);

            // Fermer la connexion avec le serveur
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
