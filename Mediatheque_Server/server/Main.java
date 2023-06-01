import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.EmpruntServer;
import server.Mediatheque;
import server.ReservationServer;
import server.RetourServer;

public class Main {
    public static void main(String[] args) throws IOException {
        Mediatheque mediatheque = new Mediatheque();

        Thread reservationThread = new Thread(new ReservationServer());
        Thread empruntThread = new Thread(new EmpruntServer());
        Thread retourThread = new Thread(new RetourServer());

        reservationThread.start();
        empruntThread.start();
        retourThread.start();
    }
}
