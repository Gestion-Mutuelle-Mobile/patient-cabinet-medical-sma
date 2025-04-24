package com.cabinetmedical.simulators;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceptionnisteAgentSimulator extends Agent {

    // Structure pour stocker les patients en attente de consultation
    private Map<String, JSONObject> waitingPatients = new HashMap<>();

    // Liste des médecins disponibles
    private List<AID> availableDoctors = new ArrayList<>();
    private Map<String, Boolean> doctorAvailability = new HashMap<>();

    @Override
    protected void setup() {
//        System.out.println("Agent Réceptionniste " + getAID().getName() + " démarré.");
        System.out.println("Agent Réceptionniste " + getAID().getName() + " démarré.");
        System.out.println("Mon AID complet: " + getAID().getName());
        System.out.println("Mon nom local: " + getAID().getLocalName());

        // Correctement s'enregistrer dans le DF pour être trouvable
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("receptionniste-service");
        sd.setName(getLocalName() + "-receptionniste");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("Réceptionniste enregistré dans le DF avec succès");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Rechercher les médecins au démarrage
        findDoctors();

        // Comportement pour recevoir les messages
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    String senderName = msg.getSender().getLocalName();
                    int performative = msg.getPerformative();
                    String content = msg.getContent();

                    System.out.println("Réceptionniste a reçu un message de " + senderName +
                            " avec performative " + ACLMessage.getPerformative(performative) +
                            " et contenu: " + content);

                    // Traiter selon l'expéditeur et la performative
                    processMessage(msg, senderName, performative, content);

                } else {
                    block();
                }
            }
        });

        // Comportement périodique pour mettre à jour la liste des médecins
        addBehaviour(new TickerBehaviour(this, 30000) { // Toutes les 30 secondes
            @Override
            protected void onTick() {
                System.out.println("Réceptionniste met à jour sa liste de médecins...");
                findDoctors();
            }
        });

        // Comportement périodique pour vérifier et informer les patients en attente
        addBehaviour(new TickerBehaviour(this, 5000) { // Toutes les 5 secondes
            @Override
            protected void onTick() {
                notifyWaitingPatients();
            }
        });
    }

    private void processMessage(ACLMessage msg, String senderName, int performative, String content) {
        // Messages du patient
        if (senderName.contains("Patient")) {
            if (performative == ACLMessage.REQUEST && content.contains("REGISTRATION")) {
                // Traiter une demande d'inscription
                processRegistration(msg);
            } else if (performative == ACLMessage.QUERY_IF && content.equals("DOCTOR_AVAILABILITY")) {
                // Répondre à une demande de disponibilité
                replyToDoctorAvailabilityQuery(msg.getSender());
            } else if (performative == ACLMessage.INFORM && content.equals("CONSULTATION_STARTED")) {
                // Le patient informe qu'une consultation a commencée
                System.out.println("Réceptionniste informé: Consultation démarrée pour le patient " + senderName);
            } else if (performative == ACLMessage.INFORM && content.equals("CONSULTATION_FINISHED")) {
                // Le patient informe qu'une consultation est terminée
                System.out.println("Réceptionniste informé: Consultation terminée pour le patient " + senderName);
            }
        }
        // Messages du médecin
        else if (senderName.contains("medecin") || senderName.contains("Medecin")) {
            if (performative == ACLMessage.INFORM) {
                String doctorAID = msg.getSender().getName();

                if ("AVAILABLE".equals(content)) {
                    // Le médecin informe qu'il est disponible
                    doctorAvailability.put(doctorAID, true);
                    System.out.println("Médecin " + senderName + " a signalé qu'il est disponible.");
                } else if ("BUSY".equals(content)) {
                    // Le médecin informe qu'il est occupé
                    doctorAvailability.put(doctorAID, false);
                    System.out.println("Médecin " + senderName + " a signalé qu'il est occupé.");
                }

                // Vérifier si des patients sont en attente après mise à jour de la disponibilité
                if (!waitingPatients.isEmpty()) {
                    notifyWaitingPatients();
                }
            }
        }
    }

    private void findDoctors() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("medecin-service");
        template.addServices(sd);

        try {
            DFAgentDescription[] results = DFService.search(this, template);
            System.out.println("Réceptionniste a trouvé " + results.length + " médecins enregistrés.");

            // Mettre à jour la liste des médecins
            availableDoctors.clear();
            for (DFAgentDescription result : results) {
                AID doctorAID = result.getName();
                availableDoctors.add(doctorAID);

                // Initialiser le statut si nécessaire
                if (!doctorAvailability.containsKey(doctorAID.getName())) {
                    doctorAvailability.put(doctorAID.getName(), true); // Supposer disponible par défaut
                }
            }
        } catch (FIPAException fe) {
            System.err.println("Erreur lors de la recherche des médecins: " + fe.getMessage());
        }
    }

    private void processRegistration(ACLMessage msg) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject patientInfo = (JSONObject) parser.parse(msg.getContent());

            String patientName = (String) patientInfo.get("nom");
            // CORRECTION: Stockez directement l'AID du patient, pas juste le nom
            AID patientAID = msg.getSender();
            waitingPatients.put(patientAID.getName(), patientInfo);

            // Confirmer l'inscription
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.CONFIRM);
            reply.setContent("Registration confirmed for: " + patientName);
            send(reply);

            System.out.println("Réceptionniste a confirmé l'inscription du patient: " + patientName);

            // Vérifier immédiatement si un médecin est disponible
            checkAvailabilityForPatient(patientAID);

        } catch (ParseException e) {
            System.err.println("Erreur lors du parsing du JSON: " + e.getMessage());
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Registration data format invalid");
            send(reply);
        }
    }

    // Nouveau: vérifier spécifiquement pour un patient
    private void checkAvailabilityForPatient(AID patientAID) {
        AID availableDoctor = findAvailableDoctor();

        if (availableDoctor != null) {
            notifyPatientAboutDoctor(patientAID, availableDoctor);
        }
    }
    // Nouveau: notifier un patient spécifique d'un médecin disponible
    private void notifyPatientAboutDoctor(AID patientAID, AID doctorAID) {
        ACLMessage availableMsg = new ACLMessage(ACLMessage.INFORM);
        availableMsg.addReceiver(patientAID);
        availableMsg.setContent("DOCTOR_AVAILABLE:" + doctorAID.getLocalName());
        send(availableMsg);

        System.out.println("Réceptionniste a informé le patient " + patientAID.getLocalName() +
                " que le médecin " + doctorAID.getLocalName() + " est disponible.");

        // Retirer le patient de la liste d'attente
        waitingPatients.remove(patientAID.getName());
    }

    private void notifyWaitingPatients() {
        if (waitingPatients.isEmpty()) {
            return; // Pas de patients en attente
        }

        // Trouver le premier médecin disponible
        AID availableDoctor = findAvailableDoctor();

        if (availableDoctor != null) {
            // CORRECTION: Utiliser directement l'AID du patient
            String patientAIDName = waitingPatients.keySet().iterator().next();
            // CORRECTION: Utiliser la méthode createAgentFromName pour créer l'AID
            AID patientAID = new AID(patientAIDName, AID.ISGUID);

            notifyPatientAboutDoctor(patientAID, availableDoctor);
        }
    }

    private AID findAvailableDoctor() {
        for (AID doctorAID : availableDoctors) {
            Boolean isAvailable = doctorAvailability.get(doctorAID.getName());
            if (isAvailable != null && isAvailable) {
                return doctorAID;
            }
        }
        return null; // Aucun médecin disponible
    }

    private void replyToDoctorAvailabilityQuery(AID patientAID) {
        AID availableDoctor = findAvailableDoctor();

        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(patientAID);

        if (availableDoctor != null) {
            reply.setContent("DOCTOR_AVAILABLE:" + availableDoctor.getLocalName());
            System.out.println("Réceptionniste informe le patient que le médecin " +
                    availableDoctor.getLocalName() + " est disponible.");
        } else {
            reply.setContent("NO_DOCTOR_AVAILABLE");
            System.out.println("Réceptionniste informe le patient qu'aucun médecin n'est disponible.");
        }

        send(reply);
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent Réceptionniste " + getAID().getName() + " terminé.");
    }
}