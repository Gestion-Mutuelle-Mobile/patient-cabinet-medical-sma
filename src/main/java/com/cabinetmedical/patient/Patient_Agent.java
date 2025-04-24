package com.cabinetmedical.patient;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Patient_Agent extends GuiAgent {

    private PatientContainer gui;

    private String nom;
    private int age;
    private String sexe;
    private String adresse;
    private String telephone;

    // AID pour la communication avec d'autres agents
    private AID receptionnisteAID;
    private AID medecinAID;

    // États de l'agent patient
    public enum PatientState {
        INITIAL,
        REGISTERED,
        WAITING_FOR_DOCTOR,
        IN_CONSULTATION,
        CONSULTATION_FINISHED
    }

    private PatientState currentState = PatientState.INITIAL;

    public static final int CMD_INSCRIPTION = 1;
    public static final int CMD_SEND_MESSAGE = 2;
    public static final int CMD_QUIT_CONSULTATION = 3;

    @Override
    protected void setup() {
        System.out.println("L'agent patient " + getAID().getName() + " est démarré.");
        System.out.println("Patient_Agent démarré avec AID: " + getAID().getName());


        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof PatientContainer) {
            this.gui = (PatientContainer) args[0];
            this.gui.setPatientAgent(this);
        } else {
            System.err.println("Erreur : Référence au PatientContainer non fournie. Arrêt de l'agent.");
            doDelete();
            return;
        }

        // Chercher le réceptionniste
        receptionnisteAID = new AID("Receptionniste", AID.ISLOCALNAME);

        // Ajouter un comportement pour traiter les événements GUI
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                GuiEvent ge = gui.getGuiEventQueue().poll();
                if (ge != null) {
                    processGuiEvent(ge);
                } else {
                    block(100);
                }
            }
        });

        // Comportement pour recevoir et traiter les messages ACL
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Traiter le message reçu
                    processACLMessage(msg);
                } else {
                    block();
                }
            }
        });

        // Ajouter un comportement pour vérifier la disponibilité du médecin
        addBehaviour(new TickerBehaviour(this, 10000) { // Vérification toutes les 10 secondes
            @Override
            protected void onTick() {
                if (currentState == PatientState.WAITING_FOR_DOCTOR) {
                    checkDoctorAvailability();
                }
            }
        });
    }

    // Traiter les événements GUI
    private void processGuiEvent(GuiEvent ge) {
        int command = ge.getType();
        System.out.println("Agent Patient a reçu un événement GUI de type : " + command);

        switch (command) {
            case CMD_INSCRIPTION:
                handleRegistrationEvent(ge);
                break;

            case CMD_SEND_MESSAGE:
                handleChatMessageEvent(ge);
                break;

            case CMD_QUIT_CONSULTATION:
                handleQuitConsultationEvent();
                break;

            default:
                System.out.println("Agent Patient : Événement GUI de type inconnu reçu : " + command);
                break;
        }
    }

    // Traiter les événements d'inscription
    private void handleRegistrationEvent(GuiEvent ge) {
        if (ge.getParameter(0) instanceof String &&
                ge.getParameter(1) instanceof String &&
                ge.getParameter(2) instanceof String &&
                ge.getParameter(3) instanceof String &&
                ge.getParameter(4) instanceof String) {

            this.nom = (String) ge.getParameter(0);
            try {
                this.age = Integer.parseInt((String) ge.getParameter(1));
            } catch (NumberFormatException e) {
                this.age = 0;
            }
            this.sexe = (String) ge.getParameter(2);
            this.adresse = (String) ge.getParameter(3);
            this.telephone = (String) ge.getParameter(4);

            System.out.println("Agent Patient a reçu les données d'inscription : Nom=" + nom +
                    ", Age=" + age + ", Sexe=" + sexe +
                    ", Adresse=" + adresse + ", Tel=" + telephone);

            sendRegistrationToReceptionniste();
            currentState = PatientState.REGISTERED;
        } else {
            System.err.println("Agent Patient : Paramètres d'inscription invalides reçus.");
        }
    }

    // Traiter les messages du chat
    private void handleChatMessageEvent(GuiEvent ge) {
        if (ge.getParameter(0) instanceof String) {
            String chatMessage = (String) ge.getParameter(0);
            System.out.println("Agent Patient a reçu un message de chat de l'interface : " + chatMessage);

            if (currentState == PatientState.IN_CONSULTATION && medecinAID != null) {
                sendMessageToDoctor(chatMessage);
            } else {
                gui.appendMessageToChat("Système: Vous n'êtes pas en consultation avec un médecin.");
            }
        } else {
            System.err.println("Agent Patient : Paramètre de message chat invalide reçu.");
        }
    }

    // Traiter la fin de consultation
    private void handleQuitConsultationEvent() {
        System.out.println("Agent Patient signale qu'il quitte la consultation.");

        if (currentState == PatientState.IN_CONSULTATION && medecinAID != null) {
            sendEndConsultationToDoctor();

            if (receptionnisteAID != null) {
                sendEndConsultationToReceptionniste();
            }

            currentState = PatientState.CONSULTATION_FINISHED;

            // Mettre à jour l'interface
            if (gui.getConsultationGui() != null) {
                gui.getConsultationGui().setConsultationEnded();
            }
        }

        // Revenir à l'interface d'inscription
        gui.showRegistrationInterface();
    }

    // Traiter les messages ACL reçus
    private void processACLMessage(ACLMessage msg) {
        String senderName = msg.getSender().getLocalName();
        int performative = msg.getPerformative();
        String content = msg.getContent();

        System.out.println("Agent Patient a reçu un message de " + senderName +
                " avec la performative " + ACLMessage.getPerformative(performative) +
                " et le contenu : " + content);

        // Messages du réceptionniste
        // Messages du réceptionniste
        if (senderName.contains("Receptionniste")) {
            if (performative == ACLMessage.INFORM) {
                System.out.println("Message du réceptionniste reçu: " + content); // DEBUG

                if (content.startsWith("DOCTOR_AVAILABLE:")) {
                    String doctorName = content.substring("DOCTOR_AVAILABLE:".length());
                    System.out.println("Médecin disponible: " + doctorName); // DEBUG

                    medecinAID = new AID(doctorName, AID.ISLOCALNAME);
                    gui.appendMessageToChat("Système: Un médecin (" + doctorName + ") est disponible. Consultation en cours...");

                    // Démarrer automatiquement la consultation
                    startConsultation();
                }
            }
        }
        else if (senderName.contains("medecin") || senderName.contains("Medecin")) {
            if (performative == ACLMessage.INFORM) {
                gui.appendMessageToChat("Dr. " + senderName + ": " + content);
            } else if (performative == ACLMessage.AGREE) {
                gui.appendMessageToChat("Système: Le médecin a accepté votre consultation.");
                // Mettre à jour l'état de l'interface
                if (gui.getConsultationGui() != null) {
                    gui.getConsultationGui().setConsultationStarted(senderName);
                }

                // Afficher l'interface de consultation
                gui.showConsultationInterface();

                currentState = PatientState.IN_CONSULTATION;
            } else if (performative == ACLMessage.REFUSE) {
                gui.appendMessageToChat("Système: Le médecin n'a pas pu accepter votre consultation.");
                currentState = PatientState.REGISTERED;
            } else if (performative == ACLMessage.FAILURE) {
                gui.appendMessageToChat("Système (Erreur): " + content);
            } else if (performative == ACLMessage.PROPOSE) {
                // Si le médecin envoie un diagnostic
                if (content.startsWith("DIAGNOSTIC:")) {
                    String diagnostic = content.substring(11); // Longueur de "DIAGNOSTIC:"
                    gui.displayDiagnostic(diagnostic);
                    gui.appendMessageToChat("Système: Un diagnostic a été proposé par le médecin.");
                }
            }
        }
    }

    // Envoyer les données d'inscription au réceptionniste
    private void sendRegistrationToReceptionniste() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(receptionnisteAID);

        JSONObject patientInfo = new JSONObject();
        patientInfo.put("type", "REGISTRATION");
        patientInfo.put("nom", this.nom);
        patientInfo.put("age", this.age);
        patientInfo.put("sexe", this.sexe);
        patientInfo.put("adresse", this.adresse);
        patientInfo.put("telephone", this.telephone);

        msg.setContent(patientInfo.toJSONString());
        send(msg);

        System.out.println("Agent Patient a envoyé les données d'inscription à l'agent Réceptionniste.");
        gui.appendMessageToChat("Système: Inscription envoyée, en attente de confirmation...");
    }

    // Vérifier la disponibilité du médecin
    private void checkDoctorAvailability() {
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(receptionnisteAID);
        msg.setContent("DOCTOR_AVAILABILITY");
        send(msg);
        System.out.println("Agent Patient demande la disponibilité d'un médecin.");
    }

    // Démarrer une consultation avec un médecin
    // Dans la classe Patient_Agent.java

    private void startConsultation() {
        // Vérifier que nous avons bien une référence au médecin
        if (medecinAID == null) {
            gui.appendMessageToChat("Système: Impossible de démarrer la consultation, aucun médecin disponible.");
            return;
        }
        System.out.println("Démarrage de la consultation avec le médecin: " +
                (medecinAID != null ? medecinAID.getName() : "null"));

        // Envoyer la demande de consultation au médecin spécifique
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(medecinAID);
        msg.setContent("CONSULTATION_REQUEST");
        send(msg);

        System.out.println("Agent Patient a envoyé une demande de consultation au médecin: " + medecinAID.getLocalName());
        gui.appendMessageToChat("Système: Demande de consultation envoyée au Dr. " + medecinAID.getLocalName() + "...");

        // Informer le réceptionniste que la consultation démarre
        ACLMessage infoMsg = new ACLMessage(ACLMessage.INFORM);
        infoMsg.addReceiver(new AID("Receptionniste", AID.ISLOCALNAME));
        infoMsg.setContent("CONSULTATION_STARTED");
        send(infoMsg);
    }
    // Envoyer un message au médecin pendant la consultation
    private void sendMessageToDoctor(String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(medecinAID);
        msg.setContent(message);
        send(msg);
        System.out.println("Agent Patient a envoyé un message au médecin: " + message);
    }

    // Terminer la consultation avec le médecin
    private void sendEndConsultationToDoctor() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(medecinAID);
        msg.setContent("END_CONSULTATION");
        send(msg);
        System.out.println("Agent Patient a envoyé une notification de fin de consultation au médecin.");
    }

    // Informer le réceptionniste de la fin de la consultation
    private void sendEndConsultationToReceptionniste() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(receptionnisteAID);
        msg.setContent("CONSULTATION_FINISHED");
        send(msg);
        System.out.println("Agent Patient a informé le réceptionniste que la consultation est terminée.");
    }

    @Override
    protected void takeDown() {
        System.out.println("L'agent patient " + getAID().getName() + " est terminé.");
        if (gui != null) {
            gui.closeInterfaces();
        }
    }

    public PatientContainer getGui() {
        return gui;
    }

    public PatientState getCurrentState() {
        return currentState;
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        // Cette méthode est appelée quand on utilise myAgent.postGuiEvent(event)
        processGuiEvent(guiEvent);
    }

    // Getters
    public String getNom() { return nom; }
    public int getAge() { return age; }
    public String getSexe() { return sexe; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
}