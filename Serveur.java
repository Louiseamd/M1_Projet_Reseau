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
                authenticateClient();
                // Ajoutez ici la gestion des autres commandes du protocole...

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

        private void authenticateClient() throws IOException {
            // Envoi de la commande WHO_ARE_YOU_?
            writer.println("WHO_ARE_YOU_?");

            // Réception de la réponse du client
            String response = reader.readLine();
            if (response != null && response.equalsIgnoreCase("ITS_ME")) {
                // Si la réponse est correcte, envoyer la commande GIMME_PASSWORD
                writer.println("GIMME_PASSWORD");

                // Attendre la réponse du client
                response = reader.readLine();
                if (response != null && response.startsWith("PASSWD ")) {
                    String[] parts = response.split(" ");
                    String password = parts[1]; // Récupérer le mot de passe envoyé par le client
                    if (password.equals(Serveur.getServerPassword())) {
                        // Si le mot de passe est correct, envoyer la commande HELLO_YOU
                        writer.println("HELLO_YOU");

                        // Attendre la commande READY du client
                        response = reader.readLine();
                        if (response != null && response.equalsIgnoreCase("READY")) {
                            // Si le client est prêt, informer le client que sa participation est prise en compte
                            writer.println("OK");
                            // À ce stade, le client est considéré comme connecté et peut participer à des tâches
                        } else {
                            writer.println("MAUVAISE REPONSE, DECONNEXION");
                            socket.close(); // Fermer la connexion
                        }
                    } else {
                        // Si le mot de passe est incorrect, envoyer la commande YOU_DONT_FOOL_ME
                        writer.println("YOU_DONT_FOOL_ME");
                        socket.close(); // Fermer la connexion
                    }
                } else {
                    // Si la réponse n'est pas conforme, fermer la connexion
                    writer.println("MAUVAISE REPONSE, DECONNEXION");
                    socket.close();
                }
            } else {
                // Si la réponse n'est pas conforme, fermer la connexion
                writer.println("MAUVAISE REPONSE, DECONNEXION");
                socket.close();
            }
        }
    }
}
