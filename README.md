**Projet de minage distribué**
Ce projet est un système de minage distribué où un serveur centralisé gère et distribue des tâches à un ou plusieurs Workers (clients). La communication entre le serveur et les clients se fait via l'API socket TCP.

**Structure du projet**
Le projet est divisé en deux classes :

**Le serveur**
**Les clients (Workers)**

**SERVEUR**
Le serveur est responsable de :

- Récupérer les tâches à partir d'une application web.
- Distribuer les tâches aux travailleurs connectés.
-  Demander l'état des travailleurs.
-  Annuler la tâche en cours.
-  Valider les résultats fournis par les travailleurs avec l'application web.
-  Informer les autres travailleurs d'abandonner la recherche en cours si un résultat est validé.
-  Le serveur dispose d'une interface en ligne de commande (CLI) pour interagir avec l'utilisateur.

Les clients - Workers
Les clients sont des Workers qui se connectent au serveur et attendent une tâche. Lorsqu'ils reçoivent une tâche du serveur, ils commencent à rechercher un nonce qui répond aux critères spécifiés. Lorsqu'un travailleur trouve un nonce valide, il l'envoie au serveur, qui le valide avec l'application web. Si le nonce est correct, le serveur informe les autres Workers d'abandonner leur recherche.

Tâches
Les tâches consistent à trouver un hash qui commence par un certain nombre de zéros (définissant la difficulté de la tâche) à partir d'un ensemble de données donné. Pour ce faire, les Workers doivent trouver le nonce à ajouter aux données pour obtenir le hash correct.

Répartition des tâches
Pour la recherche de nonce, un algorithme de répartition simple est mis en œuvre. Chaque worker commence sa recherche avec un nonce différent, calculé en fonction de son numéro d'identification et du nombre total de worker. Ainsi, tous les nonces seront testés sans aucune redondance.

Exemple de répartition des nonces avec 3 travailleurs numérotés 1, 2 et 3 :
Worker 1 : 0, 3, 6, ...
Worker 2: 1, 4, 7, ...
Worker 3 : 2, 5, 8, .

Le serveur est écrit en Java.
Il est capable de gérer plusieurs clients simultanément.
La communication avec le serveur se fait via des sockets sur le port 1337.
Il est facile à installer pour un utilisateur lambda (scripts de lancement, etc.).
Le serveur est fait uniquement avec le JDK.
Le code est documenté, facile à maintenir et évolutif.
Le code est robuste, mais n'est pas encore capable d'être déployé en production tel quel.
Le code est fourni avec un scénario de test.

**Pour lancer le projet, suivez les étapes suivantes :**

Clonez le dépôt Git du projet.
Compilez le code source à l'aide de la commande javac ou d'un IDE (IntelliJ IDEA, Eclipse, etc.).
Lancez le serveur en exécutant la classe Server dans le répertoire server.
Lancez un ou plusieurs clients en exécutant la classe Client dans le répertoire client.
Utilisez l'interface en ligne de commande (CLI) du serveur pour interagir avec les clients et gérer les tâches.


**Auteurs**
Hawa Diabate (https://github.com/hawalamalienne/)
Louise Amedjkane (https://github.com/Louiseamd/)
Sabah Hamouta (https://github.com/NatsuPow/)
Aboubacar Keita (https://github.com/NatsuPow/)

**Remerciements**
Nous remercions notre enseignant et nos collègues du M1 MIAGE pour leurs conseils tout au long du projet.
