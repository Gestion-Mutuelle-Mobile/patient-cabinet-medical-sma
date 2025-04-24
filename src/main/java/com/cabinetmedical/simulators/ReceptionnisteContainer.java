package com.cabinetmedical.simulators;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import java.util.Scanner;

public class ReceptionnisteContainer {

    public static void main(String[] args) {
        // Scanner pour permettre la saisie des paramètres de connexion
        Scanner scanner = new Scanner(System.in);
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   SYSTÈME DE CONSULTATION MÉDICALE - RÉCEPTIONNIST║");
        System.out.println("╚═══════════════════════════════════════════════════╝");

        System.out.print("Adresse IP du conteneur principal (laisser vide pour localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = "localhost";
        }

        System.out.print("Port du conteneur principal (laisser vide pour 1099): ");
        String portStr = scanner.nextLine().trim();
        int port = 1099;
        if (!portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation du port par défaut: 1099");
            }
        }

        try {
            // Créer le runtime JADE
            Runtime runtime = Runtime.instance();

            // Configurer le profil pour se connecter au conteneur principal
            ProfileImpl profile = new ProfileImpl();
            profile.setParameter(ProfileImpl.MAIN_HOST, host);
            profile.setParameter(ProfileImpl.MAIN_PORT, String.valueOf(port));
            profile.setParameter(ProfileImpl.CONTAINER_NAME, "ReceptionnisteContainer");

            // Créer le conteneur
            AgentContainer container = runtime.createAgentContainer(profile);
            System.out.println("Conteneur JADE pour le Réceptionniste démarré");

            // Créer l'agent réceptionniste
            AgentController receptionist = container.createNewAgent(
                    "Receptionniste",
                    "com.cabinetmedical.simulators.ReceptionnisteAgentSimulator",
                    null
            );

            // Démarrer l'agent
            receptionist.start();
            System.out.println("Agent Réceptionniste démarré");

        } catch (ControllerException e) {
            System.err.println("Erreur lors du démarrage du conteneur ou de l'agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}