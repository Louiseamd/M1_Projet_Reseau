package fr.ul.miage;

import java.io.*;
import java.net.*;

public class Serveur {
    private static final int PORT = 12349;
    private static final String SERVER_PASSWORD = "azerty"; // Mot de passe défini statiquement
    private static int nbreClient = 0;
    private static boolean running = true;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);
            while (running) {
                Socket socket = serverSocket.accept();
                nbreClient++;
                System.out.println("Client " + nbreClient + " connecté");
                new ClientHandler(socket, nbreClient).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur de serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getServerPassword() {
        return SERVER_PASSWORD;
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Erreur d'initialisation du handler client : " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                // Boucle pour gérer les commandes du protocole
                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println("Commande reçue du client " + clientNumber + " : " + command);
                    // Gérer la commande en fonction de son type
                    if (command.equals("ITS_ME")) {
                        writer.println("GIMME_PASSWORD");
                    } else if (command.startsWith("PASSWD ")) {
                        String password = command.substring(7);
                        if (password.equals(Serveur.getServerPassword())) {
                            writer.println("HELLO_YOU");
                        } else {
                            writer.println("YOU_DONT_FOOL_ME");
                            socket.close();
                            break;
                        }
                    } else if (command.equals("READY")) {
                        writer.println("OK");
                        // TODO : Ajouter ici la gestion des tâches à envoyer au client
                    } else {
                        writer.println("UNKNOWN_COMMAND");
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur de communication avec le client " + clientNumber + " : " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    System.out.println("Connexion avec le client " + clientNumber + " fermée.");
                } catch (IOException e) {
                    System.err.println("Erreur de fermeture du socket : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
