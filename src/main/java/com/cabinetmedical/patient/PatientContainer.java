package com.cabinetmedical.patient; // Assurez-vous que le package correspond au vôtre

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.gui.GuiEvent; // Importation nécessaire pour envoyer des événements à l'agent
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.*;

public class PatientContainer {

    private AgentContainer patientJadeContainer; // Renommé pour plus de clarté
    private Patient_Agent patientAgent; // Référence à l'agent patient

    private InscriptionPatientInterface inscriptionGui;
    private ConsultationPatientInterface consultationGui;

    // --- Ajoutez cette file d'attente ---
    private LinkedBlockingQueue<GuiEvent> guiEventQueue = new LinkedBlockingQueue<>();
    // Nous utiliserons GuiEvent comme type d'éléments, mais cela pourrait être une classe personnalisée si besoin.
    // -------------------------------------

    public static void main(String[] args) {
        // Créer une instance de la classe conteneur
        PatientContainer container = new PatientContainer();
        container.startContainer();
        // L'affichage de l'interface d'inscription se fera après le démarrage du conteneur JADE
    }

    public void closeInterfaces() {
        SwingUtilities.invokeLater(() -> {
            if (inscriptionGui != null) {
                inscriptionGui.dispose(); // Libère les ressources de la fenêtre Swing
            }
            if (consultationGui != null) {
                consultationGui.dispose(); // Libère les ressources de la fenêtre Swing
            }
        });
    }

    public void startContainer() {
        try {
            Runtime runtime = Runtime.instance();
            Properties properties = new ExtendedProperties();

            // Configurer les propriétés pour le conteneur JADE
            properties.setProperty(ProfileImpl.GUI, "true"); // Active la GUI RMA pour le débogage
            properties.setProperty(ProfileImpl.MAIN_HOST, "localhost"); // Adresse du Main Container (ajustez si nécessaire)
            properties.setProperty(ProfileImpl.MAIN_PORT, "1099"); // Port du Main Container (ajustez si nécessaire)
            properties.setProperty(ProfileImpl.CONTAINER_NAME, "PatientContainer"); // Nom de ce conteneur


            ProfileImpl profile = new ProfileImpl(properties);

            // Créer le conteneur d'agents
            patientJadeContainer = runtime.createAgentContainer(profile);

            // Démarrer le conteneur
            patientJadeContainer.start();

            System.out.println("Conteneur Patient JADE démarré.");


            // ... création et démarrage des agents simulateurs pour les tests ...
            try {
                AgentController receptionistSim = patientJadeContainer.createNewAgent("Receptionniste_Agent", "com.cabinetmedical.simulators.ReceptionnisteAgentSimulator", null);
                receptionistSim.start();
                System.out.println("Agent Réceptionniste Simulateur démarré.");

                AgentController medecinSim = patientJadeContainer.createNewAgent("Medecin_Agent", "com.cabinetmedical.simulators.MedecinAgentSimulator", null);
                medecinSim.start();
                System.out.println("Agent Médecin Simulateur démarré.");

            } catch (ControllerException e) {
                System.err.println("Erreur lors du démarrage des agents simulateurs.");
                e.printStackTrace();
            }
            // --- Fin de la section de simulation ---

            SwingUtilities.invokeLater(() -> {
                inscriptionGui = new InscriptionPatientInterface(this);
                consultationGui = new ConsultationPatientInterface(this);
                showRegistrationInterface();
            });


            // Créer et lancer l'agent Patient_Agent
            AgentController agentController = patientJadeContainer.createNewAgent(
                    "Patient_Agent",
                    "com.cabinetmedical.patient.Patient_Agent",
                    new Object[] { this } // Toujours passer la référence du conteneur à l'agent
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

    // Cette méthode est appelée par l'agent une fois qu'il est initialisé
    // pour obtenir une référence à lui-même si le conteneur en a besoin.
    public void setPatientAgent(Patient_Agent agent) {
        this.patientAgent = agent;
        System.out.println("Référence de l'agent patient reçue par le conteneur.");
        // À ce stade, l'agent est lié au conteneur graphique
    }

    // Méthode pour afficher l'interface d'inscription
    public void showRegistrationInterface() {
        SwingUtilities.invokeLater(() -> {
            if (inscriptionGui != null) {
                inscriptionGui.setVisible(true);
                if (consultationGui != null) {
                    consultationGui.setVisible(false); // Cacher l'interface de consultation si elle est affichée
                }
            }
        });
    }

    // Méthode pour afficher l'interface de consultation
    public void showConsultationInterface() {
        SwingUtilities.invokeLater(() -> {
            if (consultationGui != null) {
                consultationGui.setVisible(true);
                if (inscriptionGui != null) {
                    inscriptionGui.setVisible(false); // Cacher l'interface d'inscription
                }
            }
        });
    }

    // Méthode appelée par l'interface graphique pour envoyer un événement à l'agent
    public void postGuiEventToAgent(GuiEvent ge) {
        guiEventQueue.offer(ge);
        System.out.println("Événement GUI mis dans la file d'attente.");
    }

    public ConsultationPatientInterface getConsultationGui() {
        return consultationGui;
    }

    // Méthode appelée par l'agent pour ajouter un message à la zone de chat
    public void appendMessageToChat(String message) {
        if (consultationGui != null) {
            consultationGui.appendMessage(message);
        }
    }

    // Méthode appelée par l'agent pour afficher le diagnostic final
    public void displayDiagnostic(String diagnostic) {
        if (consultationGui != null) {
            consultationGui.displayDiagnostic(diagnostic);
        }
    }

    public LinkedBlockingQueue<GuiEvent> getGuiEventQueue() {
        return guiEventQueue;
    }
}