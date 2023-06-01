package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class EmprunterClient implements Runnable{
    public void run() {
        try {
            Socket socket = new Socket("localhost", 1001);

            // Get the subscriber ID and DVD ID from the user.
            Scanner sc = new Scanner(System.in);
            System.out.println("Numéro de l'abonné pour l'emprunt : ");
            String numeroAbonne = sc.next();
            System.out.println("Numéro du DVD pour l'emprunt : ");
            String numeroDVD = sc.next();

            // Send the subscriber ID and DVD ID to the server.
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(numeroAbonne + ";" + numeroDVD);

            // Get the response from the server.
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String serverResponse = in.readUTF();

            //Donner la date de rendu excepté
            //System.out.println("Vous devez rendre le dvd pour : " +);

            // Print the server response.
            System.out.println(serverResponse);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
