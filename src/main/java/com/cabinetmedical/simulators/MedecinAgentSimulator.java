package com.cabinetmedical.simulators; // Nouveau package pour les simulateurs

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class MedecinAgentSimulator extends Agent {

    @Override
    protected void setup() {
        System.out.println("Simulateur Agent Médecin " + getAID().getName() + " démarré.");

        // Ajouter un comportement cyclique pour recevoir les messages
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Attendre de recevoir un message de l'agent Patient (par exemple, un INFORM "prêt")
                // Vous pouvez ajouter un MessageTemplate si vous attendez un type spécifique de message
                // MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM); // Exemple

                ACLMessage msg = receive(); // receive(template); // Si vous utilisez un filtre

                if (msg != null) {
                    System.out.println("Simulateur Médecin a reçu un message de " + msg.getSender().getName() +
                            " avec la performative " + ACLMessage.getPerformative(msg.getPerformative()));
                    System.out.println("Contenu du message : " + msg.getContent());

                    // Simuler l'accord pour la consultation (qui déclenche la communication fichier côté Patient)
                    ACLMessage reply = new ACLMessage(ACLMessage.AGREE); // La performative AGREE signale un accord

                    reply.addReceiver(msg.getSender()); // Envoyer la réponse à l'expéditeur d'origine
                    reply.setContent("Prêt pour la consultation via fichier."); // Contenu simple

                    send(reply); // Envoyer le message de réponse

                    System.out.println("Simulateur Médecin a envoyé un message " + ACLMessage.getPerformative(reply.getPerformative()) + " à " + msg.getSender().getName());

                    // TODO: Pour un test plus poussé, vous pourriez ajouter ici un TickerBehaviour
                    // qui, après l'AGREE, écrirait périodiquement dans send_by_doctor_expert.txt
                    // pour simuler les questions du médecin via fichier.

                } else {
                    block(); // Bloquer le comportement
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("Simulateur Agent Médecin " + getAID().getName() + " terminé.");
    }
}