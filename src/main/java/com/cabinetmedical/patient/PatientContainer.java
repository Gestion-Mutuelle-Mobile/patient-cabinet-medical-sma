package com.cabinetmedical.patient;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.gui.GuiEvent;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;
import java.util.Scanner;

public class PatientContainer {

    private AgentContainer patientJadeContainer;
    private Patient_Agent patientAgent;

    private InscriptionPatientInterface inscriptionGui;
    private ConsultationPatientInterface consultationGui;

    private LinkedBlockingQueue<GuiEvent> guiEventQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        // Créer une instance de la classe conteneur
        PatientContainer container = new PatientContainer();

        // Demander l'adresse du Main Container
        Scanner scanner = new Scanner(System.in);
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║     SYSTÈME DE CONSULTATION MÉDICALE - PATIENT    ║");
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

        container.startContainer(host, port);
    }

    public void closeInterfaces() {
        SwingUtilities.invokeLater(() -> {
            if (inscriptionGui != null) {
                inscriptionGui.dispose();
            }
            if (consultationGui != null) {
                consultationGui.dispose();
            }
        });
    }

    public void startContainer(String host, int port) {
        try {
            Runtime runtime = Runtime.instance();
            ProfileImpl profile = new ProfileImpl();

            // Configurer les propriétés pour se connecter au Main Container existant
            profile.setParameter(ProfileImpl.MAIN_HOST, host);
            profile.setParameter(ProfileImpl.MAIN_PORT, String.valueOf(port));
            profile.setParameter(ProfileImpl.CONTAINER_NAME, "PatientContainer");

            // Créer le conteneur d'agents (non-principal)
            patientJadeContainer = runtime.createAgentContainer(profile);
            patientJadeContainer.start();

            System.out.println("Conteneur Patient JADE démarré. Connecté au Main Container à " + host + ":" + port);

            SwingUtilities.invokeLater(() -> {
                inscriptionGui = new InscriptionPatientInterface(this);
                consultationGui = new ConsultationPatientInterface(this);
                showRegistrationInterface();
            });

            // Créer et lancer l'agent Patient_Agent
            AgentController agentController = patientJadeContainer.createNewAgent(
                    "Patient_Agent",
                    "com.cabinetmedical.patient.Patient_Agent",
                    new Object[] { this }
            );

            agentController.start();
            System.out.println("Agent Patient_Agent créé et démarré.");

        } catch (ControllerException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du démarrage du conteneur JADE ou de l'agent.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Une erreur inattendue est survenue.");
        }
    }

    // Autres méthodes inchangées...
    public void setPatientAgent(Patient_Agent agent) {
        this.patientAgent = agent;
        System.out.println("Référence de l'agent patient reçue par le conteneur.");
    }

    public void showRegistrationInterface() {
        SwingUtilities.invokeLater(() -> {
            if (inscriptionGui != null) {
                inscriptionGui.setVisible(true);
                if (consultationGui != null) {
                    consultationGui.setVisible(false);
                }
            }
        });
    }

    public void showConsultationInterface() {
        SwingUtilities.invokeLater(() -> {
            if (consultationGui != null) {
                consultationGui.setVisible(true);
                if (inscriptionGui != null) {
                    inscriptionGui.setVisible(false);
                }
            }
        });
    }

    public void postGuiEventToAgent(GuiEvent ge) {
        guiEventQueue.offer(ge);
        System.out.println("Événement GUI mis dans la file d'attente.");
    }

    public ConsultationPatientInterface getConsultationGui() {
        return consultationGui;
    }

    public void appendMessageToChat(String message) {
        if (consultationGui != null) {
            consultationGui.appendMessage(message);
        }
    }

    public void displayDiagnostic(String diagnostic) {
        if (consultationGui != null) {
            consultationGui.displayDiagnostic(diagnostic);
        }
    }

    public LinkedBlockingQueue<GuiEvent> getGuiEventQueue() {
        return guiEventQueue;
    }
}