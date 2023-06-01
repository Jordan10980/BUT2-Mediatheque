package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Socket clientSocket;
    private Mediatheque mediatheque;

    public ServerThread(Socket clientSocket, Mediatheque mediatheque) {
        this.clientSocket = clientSocket;
        this.mediatheque = mediatheque;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String input = in.readLine();
            String[] inputParts = input.split(";");
            int numeroAbonne = Integer.parseInt(inputParts[0]);
            int numeroDocument = Integer.parseInt(inputParts[1]);

            String result = mediatheque.reserver(numeroAbonne, numeroDocument);
            out.println(result);

            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
