# Agent Patient pour Système Multi-Agent de Cabinet Médical

## Description du Projet

Ce projet implémente le composant "Patient" d'un système multi-agent (SMA) simulé pour un cabinet médical. L'agent patient permet à un utilisateur d'interagir avec le système pour s'inscrire, demander une consultation médicale, communiquer (type chat) avec un agent Médecin via un système de fichiers partagés, et recevoir un diagnostic.

Le composant Patient est structuré autour d'un agent JADE qui gère la logique, d'une interface graphique Swing pour l'interaction utilisateur, et d'un conteneur Java pour orchestrer le tout et assurer la communication entre l'interface et l'agent.

## Technologies Utilisées

* **Java**
* **Swing** : Pour l'interface graphique utilisateur.
* **JADE (Java Agent Development Framework)** : Pour la création et la gestion des agents autonomes.
* **json-simple** : Pour la manipulation d'objets JSON dans les messages et les fichiers.
* **Maven** : Pour la gestion des dépendances et la compilation du projet.

## Configuration du Projet

Pour mettre en place le projet sur votre machine, suivez les étapes suivantes :

1.  **Installer le JDK (Java Development Kit)** : Assurez-vous d'avoir Java 8 ou une version supérieure installée. Vous pouvez télécharger le JDK depuis [Adoptium](https://adoptium.net/) ou [Oracle](https://www.oracle.com/java/technologies/downloads/).
2.  **Installer un IDE Java** : Un environnement de développement intégré comme [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/) (gratuit) est recommandé.
3.  **Cloner ou Télécharger le Projet** : Obtenez les fichiers source du projet.
4.  **Ouvrir le Projet dans l'IDE** : Importez le projet dans IntelliJ IDEA ou votre IDE préféré en tant que projet Maven existant.
5.  **Ajouter les Dépendances Maven** : Les dépendances nécessaires (JADE et json-simple) sont déclarées dans le fichier `pom.xml`. Votre IDE devrait automatiquement télécharger ces dépendances. Si ce n'est pas le cas, ouvrez le fichier `pom.xml` et forcez un rechargement des dépendances Maven.

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.tilab.jade</groupId>
            <artifactId>jade</artifactId>
            <version>4.5.0</version> </dependency>
        <dependency>
            <groupId>com.googlecode.json-simple</groupId>
            <artifactId>json-simple</artifactId>
            <version>1.1.1</version> </dependency>
        </dependencies>
    ```
6.  **Structurer les Packages** : Assurez-vous que les classes sont organisées dans les bons packages (`com.cabinetmedical.patient`, `com.cabinetmedical.simulators`).

## Exécution de l'Application

Pour exécuter l'agent patient et ses interfaces, vous devez d'abord lancer la plateforme JADE (le Main Container), puis exécuter le conteneur Patient.

1.  **Lancer le Main Container JADE** : Exécutez la classe `jade.Boot`. Il est recommandé d'ajouter l'option `-gui` pour visualiser la plateforme et les messages échangés (très utile pour le débogage).
    ```bash
    java -cp chemin/vers/votre/jade.jar;chemin/vers/vos/classes jade.Boot -gui
    ```
    (Adaptez le séparateur de classpath `;` ou `:` selon votre système d'exploitation et les chemins d'accès)
2.  **Lancer le Conteneur Patient** : Exécutez la classe `com.cabinetmedical.patient.PatientContainer`. Ce conteneur se connectera au Main Container JADE et démarrera l'agent Patient ainsi que l'interface graphique. Si vous avez inclus les agents simulateurs pour les tests, ils seront également lancés à cette étape.

## Architecture

Le composant Patient est architecturé autour de trois éléments principaux :

* **`PatientContainer`**: Le point d'entrée JavaFX, responsable du lancement de JADE, de la création de l'agent et de la gestion de l'affichage des interfaces Swing. Il contient une file d'attente pour la communication entre l'interface et l'agent.
* **`Patient_Agent`**: L'agent JADE. Il contient la logique métier et exécute des comportements pour :
    * Traiter les événements de l'interface utilisateur (via la file d'attente).
    * Envoyer et recevoir des messages ACL des autres agents.
    * Surveiller et lire le fichier de communication du médecin (`send_by_doctor_expert.txt`).
    * Écrire dans le fichier de communication du patient (`send_by_patient.txt`).
* **Interfaces Graphiques (Swing)**:
    * `InscriptionPatientInterface`: Interface pour la saisie des informations patient.
    * `ConsultationPatientInterface`: Interface pour le chat patient-médecin et l'affichage du diagnostic.

## Communication et Intégration

L'agent Patient communique avec les autres agents du SMA (Réceptionniste, Médecin) et le système expert :

* **Communication ACL** : Utilisée pour le contrôle et le workflow (inscription, demande/confirmation de consultation, accord, notifications de départ). Les messages sont échangés avec des performatives ACL spécifiques et un contenu structuré (généralement JSON).
* **Communication par Fichiers JSON** : Utilisée pour le contenu du chat patient-médecin. L'agent Patient écrit dans `send_by_patient.txt` (format JSON `{"pb":...}` ou `{"response":...}`) et lit depuis `send_by_doctor_expert.txt` (format JSON `{"request":...}`, `{"answer":...}`, potentiellement `{"diagnostic":...}`).

L'intégration réussie dépend de l'accord entre les équipes sur les **performatives ACL**, les **structures JSON** des messages et des fichiers, et les **AID (Agent Identifiers)** utilisés.

## Test et Débogage

* **GUI RMA** : Lancez le Main Container avec `-gui` pour visualiser les agents et les messages ACL.
* **Agents Simulateurs** : Les classes `ReceptionnisteAgentSimulator` et `MedecinAgentSimulator` (situées dans `com.cabinetmedical.simulators`) peuvent être incluses dans le `PatientContainer` pour simuler les réponses des autres agents pendant le développement individuel.
* **Simulation Fichier Manuelle** : Testez la lecture/écriture des fichiers (`send_by_patient.txt`, `send_by_doctor_expert.txt`) en modifiant manuellement leur contenu dans un éditeur de texte pendant que l'agent Patient s'exécute.
* **Console Output** : Utilisez `System.out.println` et `System.err.println` dans votre code pour suivre l'exécution et afficher les valeurs clés.

Ce README vous donne les informations essentielles pour comprendre et exécuter le projet d'agent patient.
