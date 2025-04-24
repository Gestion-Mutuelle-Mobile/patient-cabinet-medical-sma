package com.cabinetmedical.patient; // Assurez-vous que le package correspond au vôtre

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List; // Nécessaire pour Files.readAllLines

public class MonitorDoctorFileBehaviour extends TickerBehaviour {

    private String filePath;
    private Patient_Agent myPatientAgent; // Référence à l'agent propriétaire
    private FileTime lastModifiedTime; // Pour stocker le temps de dernière modification connu
    private long lastSize = -1; // Pour stocker la taille du fichier, une vérification supplémentaire utile


    public MonitorDoctorFileBehaviour(String filePath) {
        // Définir le délai de vérification (par exemple, toutes les 500ms ou 1000ms)
        super(null, 500); // Vérifie le fichier toutes les 500 millisecondes (0.5 secondes)
        this.filePath = filePath;
        this.lastModifiedTime = FileTime.fromMillis(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.myPatientAgent = (Patient_Agent) myAgent; // Récupérer la référence à l'agent propriétaire
        System.out.println("Démarrage de la surveillance du fichier : " + filePath);
        // Vérifier le fichier une première fois au démarrage si il existe
        checkFile();
    }

    @Override
    protected void onTick() {
        checkFile();
    }

    private void checkFile() {
        try {
            java.nio.file.Path file = Paths.get(filePath);

            if (Files.exists(file)) {
                FileTime currentLastModifiedTime = Files.getLastModifiedTime(file);
                long currentSize = Files.size(file); // Obtenir la taille actuelle

                // Vérifier si le fichier a été modifié (temps ou taille)
                if (currentLastModifiedTime.compareTo(lastModifiedTime) > 0 || currentSize != lastSize) {
                    // Une modification a été détectée
                    if (currentLastModifiedTime.compareTo(lastModifiedTime) > 0) {
                        System.out.println("Fichier " + filePath + " modifié (temps).");
                    } else {
                        System.out.println("Fichier " + filePath + " modifié (taille).");
                    }

                    lastModifiedTime = currentLastModifiedTime; // Mettre à jour le temps connu
                    lastSize = currentSize; // Mettre à jour la taille connue


                    // Lire le contenu du fichier
                    // Utiliser readAllLines pour lire ligne par ligne, bien que le format JSON soit généralement sur une seule ligne
                    // String content = new String(Files.readAllBytes(file)); // Alternative simple si JSON sur une seule ligne
                    List<String> lines = Files.readAllLines(file);
                    String content = String.join("", lines); // Joindre toutes les lignes lues

                    System.out.println("Contenu lu : " + content);

                    // Analyser le contenu (JSON) et l'afficher dans l'interface
                    processDoctorMessage(content);

                }
            } else {
                // Le fichier n'existe pas encore, ou a été supprimé
                // Réinitialiser le temps de dernière modification si le fichier n'existe plus
                lastModifiedTime = FileTime.fromMillis(0);
                lastSize = -1;
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture ou de la surveillance du fichier " + filePath + ": " + e.getMessage());
            // e.printStackTrace();
        }
    }

    private void processDoctorMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            System.out.println("Contenu du fichier médecin vide, ignorer.");
            return;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(content);
            if (obj instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj;

                // --- Vérification si l'interface de consultation est visible avant la mise à jour ---
                // Accéder à l'interface de consultation via le PatientContainer
                ConsultationPatientInterface consultationGui = myPatientAgent.getGui().getConsultationGui(); // Il faudra ajouter un getter getConsultationGui() dans PatientContainer

                if (consultationGui != null && consultationGui.isVisible()) { // Vérifier si l'interface existe et est visible
                    System.out.println("Interface de consultation visible, mise à jour de l'UI.");

                    if (jsonObject.containsKey("request")) {
                        String requestContent = (String) jsonObject.get("request");
                        System.out.println("Message Médecin (Request) : " + requestContent);
                        SwingUtilities.invokeLater(() -> consultationGui.appendMessage("Médecin: " + requestContent)); // Utiliser directement la référence GUI
                    } else if (jsonObject.containsKey("answer")) {
                        String answerContent = (String) jsonObject.get("answer");
                        System.out.println("Message Médecin (Answer) : " + answerContent);
                        SwingUtilities.invokeLater(() -> consultationGui.appendMessage("Médecin (Réponse): " + answerContent)); // Utiliser directement la référence GUI
                    } else if (jsonObject.containsKey("diagnostic")) {
                        String diagnosticContent = (String) jsonObject.get("diagnostic");
                        System.out.println("Diagnostic reçu du médecin : " + diagnosticContent);
                        SwingUtilities.invokeLater(() -> consultationGui.displayDiagnostic(diagnosticContent)); // Utiliser directement la référence GUI
                        // TODO: Potentiellement, après affichage du diagnostic, signaler à l'agent de gérer la fin de consultation
                        // SwingUtilities.invokeLater(() -> myPatientAgent.processGuiEvent(new GuiEvent(this, Patient_Agent.CMD_END_CONSULTATION))); // Exemple: nouvel événement GUI pour la fin
                    }
                    else {
                        System.out.println("Format JSON du médecin inconnu ou inattendu : " + content);
                        SwingUtilities.invokeLater(() -> consultationGui.appendMessage("Médecin (Message inattendu): " + content));
                    }
                } else {
                    System.out.println("Interface de consultation non visible. Ignorer la mise à jour de l'UI.");
                    // Si l'interface n'est pas visible, c'est que la consultation est probablement terminée.
                    // On pourrait décider de marquer le comportement comme terminé ici aussi si la non-visibilité signifie la fin définitive.
                    // myPatientAgent.stopFileMonitoring(); // Arrêter le comportement si l'interface n'est plus visible (dépend du flux)
                }
                // ----------------------------------------------------------------------------------------------

            } else {
                System.err.println("Contenu du fichier médecin n'est pas un objet JSON : " + content);
                if (myPatientAgent.getGui() != null) {
                    myPatientAgent.getGui().appendMessageToChat("Médecin (Erreur format JSON): " + content); // Ici on repasse par le conteneur, pourrait aussi utiliser consultationGui si non null
                }
            }
        } catch (ParseException e) {
            System.err.println("Erreur lors de l'analyse JSON du fichier médecin : " + e.getMessage());
            if (myPatientAgent.getGui() != null) {
                myPatientAgent.getGui().appendMessageToChat("Médecin (Erreur analyse JSON): " + content);
            }
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors du traitement du message médecin : " + e.getMessage());
            if (myPatientAgent.getGui() != null) {
                myPatientAgent.getGui().appendMessageToChat("Médecin (Erreur traitement): " + e.getMessage());
            }
        }
    }

    // Getter pour permettre à d'autres parties de l'agent d'accéder à la référence du PatientContainer via ce comportement si nécessaire
    public PatientContainer getGui() {
        return myPatientAgent.getGui();
    }
}