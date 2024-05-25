**Documentation du projet**

**Introduction**
Le projet est une implémentation d'un système de minage distribué, où les clients (mineurs) se connectent à un serveur central pour résoudre des tâches de calcul complexes.
Le système utilise un protocole de communication personnalisé pour la distribution et la validation des tâches.

**Architecture**
Le projet est divisé en deux parties principales : le serveur et le client.

**Serveur**
Le serveur est le point central du système, responsable de la gestion des connexions clients, de la distribution des tâches et de la consolidation des résultats. 
Le serveur est implémenté en Java en utilisant les sockets TCP pour la communication avec les clients.
Le serveur utilise un pool de threads fixe pour gérer les connexions clients, ce qui permet de limiter le nombre de threads actifs simultanément et d'améliorer les performances. 
Chaque instance de ClientHandler est exécutée dans son propre thread, ce qui permet de traiter les commandes et de mettre à jour l'état du client de manière indépendante.
Le serveur utilise également un mot de passe pour l'authentification des clients. 
Bien que cela ne soit pas une sécurité sans faille, cela permet d'empêcher des clients non autorisés de se connecter au serveur et de participer à la résolution de tâches.

**Client**
Le client est l'interface utilisateur pour les mineurs. Le client se connecte au serveur, s'authentifie et attend la distribution de tâches.
Le client utilise l'algorithme de hachage SHA-256 pour résoudre les tâches et envoie les résultats au serveur pour la consolidation. Le client est implémenté en Java en utilisant les sockets TCP pour la communication avec le serveur.

**Protocole de communication**
Le système utilise un protocole de communication personnalisé pour la distribution et la consolidation des tâches.
Le protocole est basé sur des commandes textuelles simples, où chaque commande est envoyée sur une nouvelle ligne.

**Voici les commandes principales du protocole de communication utilisé dans le projet :**

WHO_ARE_YOU_? : commande envoyée par le serveur pour demander l'identité du client.
ITS_ME : réponse du client à la commande WHO_ARE_YOU_?.
GIMME_PASSWORD : commande envoyée par le serveur pour demander le mot de passe du client.
PASSWD : réponse du client à la commande GIMME_PASSWORD, avec le mot de passe réel.
HELLO_YOU : commande envoyée par le serveur pour confirmer l'authentification réussie du client.
READY : commande envoyée par le client pour indiquer qu'il est prêt à recevoir une tâche.
OK : commande envoyée par le serveur pour confirmer que le client est prêt à recevoir une tâche.
NONCE : commande envoyée par le serveur pour fournir au client le nonce à utiliser pour la tâche actuelle.
PAYLOAD : commande envoyée par le serveur pour fournir au client les données à hasher pour la tâche actuelle.
SOLVE : commande envoyée par le serveur pour fournir au client la difficulté de la tâche actuelle.
FOUND : commande envoyée par le client pour fournir au serveur la solution de la tâche actuelle, avec le nonce et le hachage.

**Tests**
Les tests ont été effectués tout au long de la conception et du développement du projet. Nous avons adopté une approche itérative, 
en mettant en place des scénarios de test pour chaque fonctionnalité et en corrigeant les bugs au fur et à mesure qu'ils se présentaient.
Nous avons effectué des tests pour les méthodes critiques du projet, et des tests d'intégration pour vérifier le bon fonctionnement du système dans son ensemble.

**Conclusion**
Le projet est une implémentation réussie d'un système de minage distribué, qui démontre la puissance de la collaboration et de la communication dans la résolution de problèmes complexes.
Le système est fiable, évolutif et performant, grâce à une conception modulaire, une gestion efficace des threads et un protocole de communication simple mais puissant.
