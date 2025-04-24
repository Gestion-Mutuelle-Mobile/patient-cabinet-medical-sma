package com.cabinetmedical.simulators; // Nouveau package pour les simulateurs

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// Vous pourriez avoir besoin d'importer JSON si vous voulez vérifier le contenu, mais pour un simulateur minimaliste, ce n'est pas obligatoire.
// import org.json.simple.JSONObject;
// import org.json.simple.parser.JSONParser;
// import org.json.simple.parser.ParseException;


public class ReceptionnisteAgentSimulator extends Agent {

    @Override
    protected void setup() {
        System.out.println("Simulateur Agent Réceptionniste " + getAID().getName() + " démarré.");

        // Ajouter un comportement cyclique pour recevoir les messages
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Attendre de recevoir un message de n'importe quel agent
                ACLMessage msg = receive(); // receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM)); // Si vous voulez filtrer

                if (msg != null) {
                    System.out.println("Simulateur Réceptionniste a reçu un message de " + msg.getSender().getName() +
                            " avec la performative " + ACLMessage.getPerformative(msg.getPerformative()));
                    System.out.println("Contenu du message : " + msg.getContent());

                    // Simuler une validation : répondre CONFIRM ou REJECT
                    ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM); // Par défaut, on confirme

                    // Vous pourriez ajouter une logique ici pour parfois rejeter, basé sur le contenu du message ou aléatoirement
                    // if (msg.getContent() != null && msg.getContent().contains("condition de rejet")) {
                    //     reply.setPerformative(ACLMessage.REJECT);
                    // }


                    reply.addReceiver(msg.getSender()); // Envoyer la réponse à l'expéditeur d'origine
                    reply.setContent("Demande reçue. Statut: Confirmé."); // Contenu simple de la réponse

                    send(reply); // Envoyer le message de réponse

                    System.out.println("Simulateur Réceptionniste a envoyé un message " + ACLMessage.getPerformative(reply.getPerformative()) + " à " + msg.getSender().getName());

                } else {
                    block(); // Bloquer le comportement jusqu'à la réception d'un message
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("Simulateur Agent Réceptionniste " + getAID().getName() + " terminé.");
    }
}