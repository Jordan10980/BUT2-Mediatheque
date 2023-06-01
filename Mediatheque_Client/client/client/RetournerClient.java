package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class RetournerClient implements Runnable{
    public void run() {
        try {
            Socket socket = new Socket("localhost", 1002);

            Scanner sc = new Scanner(System.in);
            System.out.println("Veuillez entrer votre numéro d'abonné :");
            String numeroAbonne = sc.next();
            System.out.println("Veuillez entrer le numéro du DVD que vous retournez :");
            String numeroDVD = sc.next();

            // Send subscriber ID and DVD ID to the server.
            String outData = numeroAbonne + ";" + numeroDVD;
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(outData);

            // Wait for a question from the server.
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String serverQuestion = in.readUTF();
            System.out.println(serverQuestion);

            // Answer the question.
            String degradationAnswer = sc.next();
            DataOutputStream degradationOut = new DataOutputStream(socket.getOutputStream());
            degradationOut.writeUTF(degradationAnswer);

            // Receive response from the server.
            String serverResponse = in.readUTF();
            System.out.println(serverResponse);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
