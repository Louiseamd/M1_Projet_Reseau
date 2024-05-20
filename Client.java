package fr.ul.miage;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12349;
    private static final String SERVER_PASSWORD = "azerty"; // Le mot de passe du serveur est défini ici

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            Scanner scanner = new Scanner(System.in); // Créer un Scanner pour la saisie du client

            // Lecture et traitement des instructions du serveur
            String instruction;
            while ((instruction = reader.readLine()) != null) {
                System.out.println("Serveur : " + instruction);

                // Réponse en fonction de l'instruction reçue
                switch (instruction) {
                    case "WHO_ARE_YOU_?":
                        // Le client saisit sa réponse
                        System.out.print("Votre réponse : ");
                        String reponse = scanner.nextLine();

                        // Envoyer la réponse au serveur
                        writer.println(reponse);

                        // Vérifier si la réponse est correcte
                        if (!reponse.equalsIgnoreCase("ITS_ME")) {
                            System.out.println("Mauvaise réponse, déconnexion du serveur...");
                            socket.close();
                            return;
                        }
                        break;
                    case "GIMME_PASSWORD":
                        // Le client saisit le mot de passe
                        System.out.print("Mot de passe : ");
                        String password = scanner.nextLine();

                        // Envoyer le mot de passe au serveur
                        writer.println("PASSWD " + password);
                        break;
                    case "HELLO_YOU":
                        // Le client indique qu'il est prêt
                        writer.println("READY");
                        break;
                    case "YOU_DONT_FOOL_ME":
                        System.out.println("Mot de passe incorrect. Déconnexion...");
                        socket.close(); // Déconnecter le client
                        return; // Sortir de la méthode main pour arrêter le client
                    case "OK":
                        System.out.println("Connecté et prêt pour des tâches.");
                        // Le client est connecté et prêt pour d'autres commandes
                        // Ajoutez ici la gestion des autres commandes du protocole...
                        break;
                    default:
                        System.out.println("Instruction inconnue : " + instruction);
                        break;
                }

                // Attendre 1 seconde avant la prochaine instruction
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            System.err.println("Erreur de communication : " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Erreur d'interruption : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
