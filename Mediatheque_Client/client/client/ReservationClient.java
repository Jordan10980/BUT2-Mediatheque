package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ReservationClient implements Runnable{
    public void run() {
        try {
            Socket socket = new Socket("localhost", 1000);

            System.out.println("ID de l'abonné : ");
            Scanner sc = new Scanner(System.in);
            String abonneId = sc.next();

            System.out.println("ID du DVD : ");
            String dvdId = sc.next();

            // Combine the subscriber ID and the DVD ID into a single string,
            // separated by a semicolon.
            String outputData = abonneId + ";" + dvdId;

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(outputData);

            // Wait for a question from the server
            //DataInputStream in = new DataInputStream(socket.getInputStream());
            //String serverQuestion = in.readUTF();
            //System.out.println(serverQuestion);

            // Answer the question.
            //String AlerteAnswer = sc.next();
            //DataOutputStream AlerteOut = new DataOutputStream(socket.getOutputStream());
            //AlerteOut.writeUTF(AlerteAnswer);

            //On récupère la donnée envoyée par le serveur
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String serverResponse = in.readUTF();
            System.out.println(serverResponse);



            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
