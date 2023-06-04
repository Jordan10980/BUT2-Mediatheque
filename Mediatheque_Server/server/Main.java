import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import server.EmpruntServer;
import server.Mediatheque;
import server.ReservationServer;
import server.RetourServer;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        // Créer une instance de la médiathèque
        Mediatheque mediatheque = new Mediatheque();

        // Créer des threads pour les serveurs de réservation, d'emprunt et de retour
        Thread reservationThread = new Thread(new ReservationServer());
        Thread empruntThread = new Thread(new EmpruntServer());
        Thread retourThread = new Thread(new RetourServer());

        // Démarrer les threads
        reservationThread.start();
        empruntThread.start();
        retourThread.start();
    }
}
