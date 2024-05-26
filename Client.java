package fr.ul.miage;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12351;
    private static int difficulty;
    private static long nonceStart;
    private static long nonceIncrement;
    private static String payload;
    private static boolean ready = false;
    private static Long currentNonce;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            Scanner scanner = new Scanner(System.in);

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

                        // Envoie la réponse au serveur
                        writer.println(reponse);

                        // Vérifie si la réponse est correcte
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
                        System.out.print("Votre réponse : ");
                        String response = scanner.nextLine();
                        writer.println(response);
                        break;
                    case "YOU_DONT_FOOL_ME":
                        System.out.println("Mot de passe incorrect. Déconnexion...");
                        socket.close();
                        return;
                    case "OK":
                    	ready = true;
                        System.out.println("Connecté et prêt pour des tâches.");
                        // Ajouter une boucle while pour continuer à écouter les instructions du serveur
                        while ((instruction = reader.readLine()) != null) {
                        	Thread t = null;
                            // Traitement de l'instruction
                            if (instruction.startsWith("NONCE ")) {
                                String[] parts = instruction.split(" ");
                                nonceStart = Long.parseLong(parts[1]);
                                nonceIncrement = Long.parseLong(parts[2]);
                                System.out.println("NONCE " + nonceStart + " " + nonceIncrement);
                            }

                            if (instruction.startsWith("SOLVE ")) {
                                String[] parts = instruction.split(" ");
                                difficulty = Integer.parseInt(parts[1]); // Stocker la difficulté reçue
                                System.out.println("SOLVE " + difficulty);

                             // Résoudre la tâche en utilisant la difficulté et les nonces
                                
                                if (reader.ready()) {
                                    payload = reader.readLine().substring(8);
                                    System.out.println("PAYLOAD " + payload);
                                }
                                System.out.println("Le minage commence");
                                t = new Thread(() -> searchAndValidateNonces(payload, difficulty, nonceStart, nonceIncrement, socket));
                                t.start();
                                if(instruction.startsWith("CANCELLED")) {
                                    t.interrupt(); // suspendre la tâche en cours
                                    writer.println("READY");
                                    ready = true;
                                }
                            }
                            if(instruction.startsWith("PROGRESS")) {
                                if(ready == true) {
                                    writer.println("NOPE");
                                }
                                else {
                                    // si thread en cours d'exécution
                                    if(t != null && t.isAlive()) {
                                        // recup nonce courant 
                                        long currentNonce = 0; // il faut qu'on trouve comment recup le nonce en cours
                                        //nonce en hexa
                                        writer.println("TESTING " + Long.toHexString(currentNonce));
                                    }
                                    else {
                                        writer.println("TESTING");
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        System.out.println("Instruction inconnue : " + instruction);
                        break;
                }

                // Attendre 1 seconde avant la prochaine instruction
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Erreur d'interruption : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de communication : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fonctions pour le minage
    public static String generateTarget(int difficulty) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < difficulty; i++) {
          sb.append("0");
        }
        return sb.toString();
      }

      public static String calculateHash(String data, long nonce) {
        try {
          MessageDigest digest = MessageDigest.getInstance("SHA-256");

          // Convertir le nonce en bytes sans les zéros de début
          byte[] nonceBytes = longToTrimmedBytes(nonce);

          // Concaténer les données en string avec les bytes du nonce
          byte[] dataBytes = data.getBytes("utf-8");
          byte[] payload = new byte[dataBytes.length + nonceBytes.length];
          System.arraycopy(dataBytes, 0, payload, 0, dataBytes.length);
          System.arraycopy(nonceBytes, 0, payload, dataBytes.length, nonceBytes.length);

          // Calculer le hash
          byte[] hashBytes = digest.digest(payload);
          return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
          e.printStackTrace();
          return null;
        }
      }

      private static byte[] longToTrimmedBytes(long x) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean nonZeroFound = false;

        for (int i = 0; i < Long.BYTES; i++) {
          byte b = (byte)(x >> (8 * (Long.BYTES - 1 - i)));
          if (b != 0 || nonZeroFound) {
            baos.write(b);
            nonZeroFound = true;
          }
        }

        return baos.toByteArray();
      }

      private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes) {
          sb.append(String.format("%02x", b));
        }
        return sb.toString();
      }

      
      public static void searchAndValidateNonces(String data, int difficulty, long nonceStart, long nonceIncrement, Socket socket) {
   	    	long nonce = nonceStart;
    	    String target = generateTarget(difficulty);

    	    while (true) {
    	        String hash = calculateHash(data, nonce);

    	        if (hash.startsWith(target)) {
    	            System.out.println("Bon hash trouvé : " + hash + " avec le nonce : " + Long.toHexString(nonce));
    	            try {
    	                // Envoyer un message au Serveur de la forme "FOUND " avec premier argument le hash trouvé" "et deuxième argument le nonce trouvé"
    	                String message = "FOUND " + hash + " " + Long.toHexString(nonce) + difficulty;
    	                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // utiliser la variable socket dans la classe Client
    	                out.println(message);

    	                
    	            } catch (IOException e) {
    	                e.printStackTrace();
    	            }
    	        }

    	        nonce += nonceIncrement;
    	    }
    	}


      
      
}
