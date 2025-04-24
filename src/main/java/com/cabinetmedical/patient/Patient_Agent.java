package com.cabinetmedical.patient; // Assurez-vous que le package correspond au vôtre

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour; // Importation pour le comportement cyclique
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
import java.util.concurrent.LinkedBlockingQueue;


public class Patient_Agent extends GuiAgent {

    public PatientContainer getGui() {
        return gui;
    }

    public void setGui(PatientContainer gui) {
        this.gui = gui;
    }

    private PatientContainer gui;

    private String nom;
    private int age;
    private String sexe;
    private String adresse;
    private String telephone;

    public static final int CMD_INSCRIPTION = 1;
    public static final int CMD_SEND_MESSAGE = 2;
    public static final int CMD_QUIT_CONSULTATION = 3;

    private MonitorDoctorFileBehaviour monitorBehaviour;


    @Override
    protected void setup() {
        System.out.println("L'agent patient " + getAID().getName() + " est démarré.");

        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof PatientContainer) {
            this.gui = (PatientContainer) args[0];
            this.gui.setPatientAgent(this);
        } else {
            System.err.println("Erreur : Référence au PatientContainer non fournie. Arrêt de l'agent.");
            doDelete();
            return;
        }

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

        // 1. Comportement pour recevoir et traiter les messages ACL des autres agents
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {

                ACLMessage msg = receive(); // receive(template) pour utiliser le filtre
                if (msg != null) {
                    // Traiter le message reçu
                    String senderName = msg.getSender().getLocalName(); // Nom local de l'expéditeur
                    int performative = msg.getPerformative(); // Type de performative (INFORM, CONFIRM, etc.)
                    String content = msg.getContent(); // Contenu du message

                    System.out.println("Agent Patient a reçu un message de " + senderName +
                            " avec la performative " + ACLMessage.getPerformative(performative) +
                            " et le contenu : " + content);


                    // Logique de traitement basée sur l'expéditeur et la performative
                    if ("Receptionniste_Agent".equals(senderName)) {
                        // Message provenant de l'agent Réceptionniste
                        if (performative == ACLMessage.CONFIRM) {
                            System.out.println("Réceptionniste a confirmé la demande de consultation. Affichage de l'interface de consultation.");
                            // Afficher l'interface de consultation
                            gui.showConsultationInterface();
                            // TODO: Potentiellement envoyer un message ACL au Médecin pour l'informer que le patient est prêt si le flux l'exige
                            sendReadyForConsultationToMedecin(); // Méthode à implémenter
                        } else if (performative == ACLMessage.REJECT_PROPOSAL) {
                            System.out.println("Réceptionniste a rejeté la demande de consultation.");
                            // Informer l'utilisateur via l'interface graphique (par exemple, dans la zone de chat ou une popup)
                            gui.appendMessageToChat("Système: Votre demande de consultation a été rejetée par la réceptionniste."); // Afficher dans le chat comme un message système
                            // TODO: Revenir à l'interface d'inscription ou proposer une autre action
                        }
                        // Ajouter d'autres cas de performatives de la Réceptionniste si nécessaire
                    } else if ("Medecin_Agent".equals(senderName)) {
                        // Message provenant de l'agent Médecin
                        if (performative == ACLMessage.INFORM) {
                            // Potentiellement le diagnostic final ou une autre information.
                            // Le document suggère que le diagnostic final peut être envoyé via fichier, mais un message ACL est aussi possible.
                            // Si c'est le diagnostic, afficher le dans l'onglet diagnostic.
                            System.out.println("Médecin envoie des informations (potentiellement un diagnostic).");
                            // TODO: Analyser le contenu pour déterminer si c'est le diagnostic et l'afficher
                            gui.displayDiagnostic(content); // Pour l'instant, afficher le contenu brut dans la zone de diagnostic
                        } else if (performative == ACLMessage.AGREE) {
                            // Le médecin a accepté la consultation. C'est le moment où la communication via fichiers peut commencer.
                            System.out.println("Médecin a accepté la consultation. Démarrage de la surveillance du fichier du médecin.");
                            // Démarrer la surveillance du fichier send_by_doctor_expert.txt
                            startFileMonitoring(); // Méthode à implémenter
                        } else if (performative == ACLMessage.REQUEST) {
                            // Si le médecin utilise des messages ACL pour poser des questions spécifiques,
                            // l'agent patient devra les afficher à l'utilisateur.
                            System.out.println("Médecin envoie une requête (via ACL).");
                            gui.appendMessageToChat("Médecin (via ACL): " + content); // Afficher dans le chat
                            // TODO: Gérer la réponse de l'utilisateur à cette requête ACL (peut nécessiter un GuiEvent spécifique ou un comportement)
                        }
                        // Ajouter d'autres cas de performatives du Médecin si nécessaire (ex: CANCEL si la consultation est annulée)
                    } else {
                        // Message d'un autre agent non géré
                        System.out.println("Agent Patient a reçu un message d'un agent inconnu (" + senderName + ").");
                    }

                } else {
                    block(); // Bloquer le comportement jusqu'à la réception d'un message
                }
            }
        });

        // 2. Comportement pour surveiller le fichier send_by_doctor_expert.txt (sera ajouté dans la prochaine étape)
        // addBehaviour(new MonitorDoctorFileBehaviour(this)); // Exemple, la classe MonitorDoctorFileBehaviour sera à créer
        startFileMonitoring();

    }

    private void processGuiEvent(GuiEvent ge) {
        int command = ge.getType(); // Récupérer le type de commande de l'événement

        System.out.println("Agent Patient (via file d'attente) a reçu un événement GUI de type : " + command);

        switch (command) {
            case CMD_INSCRIPTION:
                System.out.println("Traitement de l'événement : CMD_INSCRIPTION");
                if (ge.getParameter(0) instanceof String &&
                        ge.getParameter(1) instanceof String &&
                        ge.getParameter(2) instanceof String &&
                        ge.getParameter(3) instanceof String &&
                        ge.getParameter(4) instanceof String) {

                    this.nom = (String) ge.getParameter(0);
                    try {
                        this.age = Integer.parseInt((String) ge.getParameter(1));
                    } catch (NumberFormatException e) {
                        System.err.println("Agent Patient : Erreur lors de la conversion de l'âge : " + ge.getParameter(1));
                        // TODO: Gérer l'erreur
                        this.age = 0;
                    }
                    this.sexe = (String) ge.getParameter(2);
                    this.adresse = (String) ge.getParameter(3);
                    this.telephone = (String) ge.getParameter(4);

                    System.out.println("Agent Patient a reçu les données d'inscription : Nom=" + nom + ", Age=" + age + ", Sexe=" + sexe + ", Adresse=" + adresse + ", Tel=" + telephone);

                    sendRegistrationToReceptionniste();

                } else {
                    System.err.println("Agent Patient : Paramètres d'inscription invalides reçus.");
                }
                break;

            case CMD_SEND_MESSAGE:
                System.out.println("Traitement de l'événement : CMD_SEND_MESSAGE");
                if (ge.getParameter(0) instanceof String) {
                    String chatMessage = (String) ge.getParameter(0);
                    System.out.println("Agent Patient (via file) a reçu un message de chat de l'interface : " + chatMessage);

                    writeMessageToFile(chatMessage);
                    // TODO: Notifier le médecin si nécessaire

                } else {
                    System.err.println("Agent Patient : Paramètre de message chat invalide reçu.");
                }
                break;

            case CMD_QUIT_CONSULTATION:
                System.out.println("Traitement de l'événement : CMD_QUIT_CONSULTATION");
                System.out.println("Agent Patient (via file) signale qu'il quitte la consultation.");

                stopFileMonitoring(); // Appelle une nouvelle méthode pour arrêter le comportement

                sendQuitNotificationToMedecin();
                sendQuitNotificationToReceptionniste();
                // TODO: Gérer la fermeture de l'interface de consultation si nécessaire
                // gui.showRegistrationInterface(); // Exemple : revenir à l'accueil
                break;

            default:
                System.out.println("Agent Patient (via file) : Événement GUI de type inconnu reçu : " + command);
                break;
        }
    }

    // Méthode pour envoyer les données d'inscription à l'agent Réceptionniste
    private void sendRegistrationToReceptionniste() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new jade.core.AID("Receptionniste_Agent", jade.core.AID.ISLOCALNAME)); // Remplacez "Receptionniste_Agent" si le nom est différent

        JSONObject patientInfo = new JSONObject();
        patientInfo.put("nom", this.nom);
        patientInfo.put("age", this.age);
        patientInfo.put("sexe", this.sexe);
        patientInfo.put("adresse", this.adresse);
        patientInfo.put("telephone", this.telephone);

        msg.setContent(patientInfo.toJSONString());
        msg.setLanguage("French");

        send(msg);

        System.out.println("Agent Patient a envoyé les données d'inscription à l'agent Réceptionniste.");
    }

    // Méthode pour écrire le message du patient dans le fichier send_by_patient.txt
    // Cette méthode a été légèrement ajustée pour inclure les imports nécessaires et le format JSON
    private void writeMessageToFile(String message) {
        String filePath = "send_by_patient.txt";

        try {
            JSONObject jsonMessage = new JSONObject();
            String lowerCaseMessage = message.trim().toLowerCase();
            if ("oui".equals(lowerCaseMessage) || "non".equals(lowerCaseMessage)) {
                jsonMessage.put("response", lowerCaseMessage);
            } else {
                jsonMessage.put("pb", message);
            }

            String contentToWrite = jsonMessage.toJSONString();

            Files.write(Paths.get(filePath), contentToWrite.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Agent Patient a écrit dans " + filePath + ": " + contentToWrite);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'écriture dans le fichier " + filePath);
        }
    }

    // TODO: Méthode pour envoyer un message ACL au Médecin pour l'informer que le patient est prêt pour la consultation via fichiers
    private void sendReadyForConsultationToMedecin() {
        // Créer un message ACLMessage (quel type de performative ? INFORM ? AGREE ?)
        // Définir le destinataire (AID de l'agent Medecin)
        // Ajouter un contenu si nécessaire
        // Envoyer le message
        System.out.println("Agent Patient envoie un message au Médecin pour signaler qu'il est prêt pour la consultation fichier.");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // Exemple
        msg.addReceiver(new jade.core.AID("Medecin_Agent", jade.core.AID.ISLOCALNAME)); // Remplacez "Medecin_Agent" si différent
        msg.setContent("Patient is ready for file-based consultation."); // Exemple de contenu
        send(msg);
    }


    private void startFileMonitoring() {
        System.out.println("Agent Patient : Démarrage de la surveillance du fichier du médecin.");
        // Créer le comportement et stocker sa référence AVANT de l'ajouter
        this.monitorBehaviour = new MonitorDoctorFileBehaviour("send_by_doctor_expert.txt");
        addBehaviour(this.monitorBehaviour); // Ajouter le comportement
    }

    private void stopFileMonitoring() {
        System.out.println("Agent Patient : Arrêt de la surveillance du fichier du médecin.");
        if (this.monitorBehaviour != null) {
            this.monitorBehaviour.stop(); // Appeler la méthode stop() de TickerBehaviour
            // Alternativement, marquer le comportement comme terminé :
            // this.monitorBehaviour.done(); // Cette méthode est standard pour tout Behaviour
            // Puisque TickerBehaviour a stop(), c'est plus explicite ici.

            this.monitorBehaviour = null; // Supprimer la référence
        }
    }

    private void sendQuitNotificationToMedecin() {
        // Créer un message ACLMessage (CANCEL, INFORM ?)
        // Définir le destinataire (AID Medecin)
        // Envoyer le message
        System.out.println("Agent Patient envoie une notification de départ au Médecin.");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // Exemple
        msg.addReceiver(new jade.core.AID("Medecin_Agent", jade.core.AID.ISLOCALNAME)); // Remplacez "Medecin_Agent" si différent
        msg.setContent("Patient is quitting the consultation."); // Exemple de contenu
        send(msg);
    }

    private void sendQuitNotificationToReceptionniste() {
        // Créer un message ACLMessage (CANCEL, INFORM ?)
        // Définir le destinataire (AID Receptionniste)
        // Envoyer le message
        System.out.println("Agent Patient envoie une notification de départ à la Réceptionniste.");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // Exemple
        msg.addReceiver(new jade.core.AID("Receptionniste_Agent", jade.core.AID.ISLOCALNAME)); // Remplacez "Receptionniste_Agent" si différent
        msg.setContent("Patient is quitting the consultation."); // Exemple de contenu
        send(msg);
    }


    @Override
    protected void takeDown() {
        System.out.println("L'agent patient " + getAID().getName() + " est terminé.");
        // Demander au PatientContainer de fermer les interfaces graphiques
        if (gui != null) {
            gui.closeInterfaces(); // Appel à une nouvelle méthode que nous allons ajouter dans PatientContainer
        }
    }

    @Override
    protected void beforeMove() {
        System.out.println("L'agent patient " + getAID().getName() + " va migrer.");
    }

    @Override
    protected void afterMove() {
        System.out.println("L'agent patient " + getAID().getName() + " a migré.");
    }

    // La méthode postGuiEvent est gérée par GuiAgent et appelle onGuiEvent.
    // public void postGuiEvent(GuiEvent ge) { super.postGuiEvent(ge); }


    // Getters pour les informations du patient (peuvent être utiles)
    public String getNom() { return nom; }
    public int getAge() { return age; }
    public String getSexe() { return sexe; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {

    }
}