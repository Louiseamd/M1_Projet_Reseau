package fr.ul.miage;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serveur {
    // Déclaration des constantes et des variables statiques
    private static final int PORT = 1337; // Numéro de port p
    private static final String API_URL = "https://projet-raizo-idmc.netlify.app/.netlify/functions/validate_work";
    private static final String SERVER_PASSWORD = "azerty"; // Mot de passe pour l'authentification des clients
    private static final String BASE_URL = "https://projet-raizo-idmc.netlify.app/.netlify/functions"; // URL de base pour les appels API
    private static final String GROUP_ID = "recio6yL2vGi3ZBty"; // ID de notre groupe
    private static int nbreClient = 0; // Nombre de clients connectés au serveur
    private static int nbreWorker = 0; // Nombre de clients prêts à effectuer des tâches
    private static boolean running = true; // Indicateur pour savoir si le serveur est en cours d'exécution
    private static List<ClientHandler> readyClients = new ArrayList<>(); // Liste des clients prêts à effectuer des tâches, les "workers"
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);
            // Créer un thread pour écouter les commandes saisies au clavier
            Thread commandThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (running) {
                    String command = scanner.nextLine();
                    if (command.startsWith("SOLVE ")) {
                        int difficulty = Integer.parseInt(command.substring(6));
                        startTask(difficulty);
                    }
                    
                }
            });
            commandThread.start();

            while (running) {
                Socket socket = serverSocket.accept();
                nbreClient++;
                System.out.println("Client " + nbreClient + " connecté");
                ClientHandler clientHandler = new ClientHandler(socket, nbreClient);
                executor.execute(clientHandler);
            }
        }
    }

    public static String getServerPassword() {
        return SERVER_PASSWORD;
    }

    public static int getNbreWorker() {
        return nbreWorker;
    }

    public static void sendCommandToReadyClients(int difficulty, String work, int increment) {
        // Envoyer une commande à tous les clients prêts à effectuer des tâches
    	
	    for (ClientHandler client : readyClients) {
	        int clientIndex = readyClients.indexOf(client);
            int start = clientIndex;
	        client.sendCommand("NONCE " + start + " " + String.valueOf(increment));
	        client.sendCommand("SOLVE " + difficulty);
	        client.sendCommand("PAYLOAD " + work);
	        }
    	
    }

    // Méthode pour générer une tâche à partir de l'API
    public static String generateWork(int difficulty) {
        try {
            String apiUrl = BASE_URL + "/generate_work?d=" + difficulty;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + GROUP_ID);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            return "Failed to generate work: " + e.getMessage();
        }
    }

    // Méthode pour extraire les données utiles à partir de la réponse JSON de l'API
    private static String extractData(String jsonResponse) {
        int startIndex = jsonResponse.indexOf("\"data\":\"");
        if (startIndex != -1) {
            int secondQuoteIndex = jsonResponse.indexOf('"', startIndex + 8);
            if (secondQuoteIndex != -1) {
                return jsonResponse.substring(startIndex + 8, secondQuoteIndex);
            }
        }
        return "Failed to extract data from JSON response.";
    }
    //Méthode pour valider ou non le nonce renvoyé par le Client
    public static String validateWork(int difficulty, long nonce, String hash) throws IOException {
	    URL url = new URL(API_URL);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");
	    connection.setRequestProperty("Authorization", "Bearer " + GROUP_ID);
	    connection.setDoOutput(true);

	    String jsonInputString = String.format("{\"d\": %d, \"n\": \"%s\", \"h\": \"%s\"}", difficulty, Long.toHexString(nonce), hash);

	    try (OutputStream os = connection.getOutputStream()) {
	      byte[] input = jsonInputString.getBytes("utf-8");
	      os.write(input, 0, input.length);
	    }

	    int responseCode = connection.getResponseCode();
	    String responseMessage = connection.getResponseMessage();

	    try (BufferedReader in = new BufferedReader(new InputStreamReader(
	      (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream()))) {
	      StringBuilder response = new StringBuilder();
	      String inputLine;
	      while ((inputLine = in.readLine()) != null) {
	        response.append(inputLine);
	      }
	      return responseCode + " " + responseMessage + ": " + response.toString();
	    }
	  }

    // Méthode pour lancer une tâche et envoyer les commandes associées aux clients prêts
    public static void startTask(int difficulty) {
        String work = extractData(generateWork(difficulty));
        int increment = readyClients.size() - 1;
        // Envoyer la commande SOLVE aux clients
        sendCommandToReadyClients(difficulty , work, increment); 
    }

    // Classe interne pour la gestion des clients connectés au serveur vie thread
    static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;
        private BufferedReader reader;
        private PrintWriter writer;
        private boolean isReady = false;

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
                while (isReady) {
                    String command = reader.readLine();
                    if (command == null) {
                        break;
                    }
                    System.out.println("Commande reçue pour le client " + clientNumber + " : " + command);
                    
                 // Gérer la commande FOUND envoyée par le client
                    if (command.startsWith("FOUND")) { 
                        String[] parts = command.split(" ");
                        String hash = parts[1];
                        long nonce = Long.parseLong(parts[2], 16);
                        int difficulty = Integer.valueOf(parts[3]);
                        String response = validateWork(difficulty, nonce, hash);
                        writer.println(response);
                        if(response.startsWith("HTTP/1.1 200 OK")) {
                        	writer.println("OK");
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur de communication avec le client " + clientNumber + " : " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    System.out.println("Connexion avec le client " + clientNumber + " fermée.");
                    readyClients.remove(this);
                } catch (IOException e) {
                    System.err.println("Erreur de fermeture du socket : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Méthode pour authentifier un client qui se connecte au serveur
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
                    String password = parts[1];

                    // Récupérer le mot de passe envoyé par le client
                    if (password.equals(Serveur.getServerPassword())) {
                        // Si le mot de passe est correct, envoyer la commande HELLO_YOU
                        writer.println("HELLO_YOU");

                        // Attendre la commande READY du client
                        response = reader.readLine();
                        if (response != null && response.equalsIgnoreCase("READY")) {
                            // Si le client est prêt, informer le client que sa participation est prise en compte
                            writer.println("OK");
                            // À ce stade, le client est considéré comme connecté et peut participer à des tâches
                            synchronized (Serveur.class) {
                                Serveur.nbreWorker++;
                            }
                            isReady = true;
                            readyClients.add(this);
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

        public boolean isReady() {
            return isReady; // booleen qui se met  à vrai quand le client passe à "
        }

        public void sendCommand(String command) {
            writer.println(command);
        }

        public static int getClientIndex(ClientHandler client) {
            return readyClients.indexOf(client);
        }
    }
}
