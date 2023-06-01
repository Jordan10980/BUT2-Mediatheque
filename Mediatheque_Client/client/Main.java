import client.EmprunterClient;
import client.ReservationClient;
import client.RetournerClient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Choisissez une action :");
            System.out.println("1. Réserver un document");
            System.out.println("2. Emprunter un document");
            System.out.println("3. Retourner un document");
            System.out.println("4. Quitter l'application");

            int choix = scanner.nextInt();

            switch (choix) {
                case 1:
                    ReservationClient reservationClient = new ReservationClient();
                    reservationClient.run();
                    break;
                case 2:
                    EmprunterClient emprunterClient = new EmprunterClient();
                    emprunterClient.run();
                    break;
                case 3:
                    RetournerClient retournerClient = new RetournerClient();
                    retournerClient.run();
                    break;
                case 4:
                    System.out.println("Fermeture de l'application.");
                    return;
                default:
                    System.out.println("Choix invalide, veuillez choisir une option entre 1 et 4.");
                    break;
            }
        }
    }
}