package com.cabinetmedical.patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.DefaultCaret;
import jade.gui.GuiEvent;

public class ConsultationPatientInterface extends JFrame {

    public static final int CMD_SEND_MESSAGE = 2;
    public static final int CMD_QUIT_CONSULTATION = 3;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton envoyerButton;
    private JButton quitterButton;
    private JTextArea diagnosticArea;

    private PatientContainer patientContainer;

    public ConsultationPatientInterface(PatientContainer container) {
        this.patientContainer = container;

        // --- Configuration de la fenêtre ---
        setTitle("Consultation Patient");
        setSize(700, 550); // Taille augmentée
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ou HIDE_ON_CLOSE selon votre gestion
        setLocationRelativeTo(null); // Centrer la fenêtre

        // Utilisation d'un BorderLayout pour la structure principale
        setLayout(new BorderLayout());

        // --- Panneau principal avec une bordure pour l'espacement extérieur ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Ajouter de l'espace autour du contenu
        add(mainPanel, BorderLayout.CENTER);

        // --- JTabbedPane pour les onglets ---
        JTabbedPane tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER); // Le TabbedPane prend la partie centrale du mainPanel


        // --- Onglet Consultation ---
        JPanel consultationPanel = new JPanel(new BorderLayout());
        // consultationPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Espace intérieur si désiré


        // Panneau de contrôle en haut (bouton Quitter)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Alignement à droite
        controlPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Petite marge verticale
        quitterButton = new JButton("Quitter Consultation");
        controlPanel.add(quitterButton);
        consultationPanel.add(controlPanel, BorderLayout.NORTH);

        // Zone d'affichage du chat avec ScrollPane et bordure
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(8, 8, 8, 8)); // Ajouter une marge interne au texte

        DefaultCaret caret = (DefaultCaret)chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // Défilement automatique

        JScrollPane scrollPane = new JScrollPane(chatArea);
        // Ajouter une bordure autour de la zone de chat
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Conversation"), // Bordure titrée extérieure
                new EmptyBorder(5, 5, 5, 5) // Bordure vide intérieure pour espacement
        ));
        consultationPanel.add(scrollPane, BorderLayout.CENTER);


        // Panneau pour la saisie de message et le bouton Envoyer avec bordure
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0)); // Espacement horizontal entre champ et bouton
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Marge au-dessus du panneau de saisie
        messageField = new JTextField();
        envoyerButton = new JButton("Envoyer");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(envoyerButton, BorderLayout.EAST);

        consultationPanel.add(inputPanel, BorderLayout.SOUTH);


        // --- Onglet Diagnostic ---
        JPanel diagnosticPanel = new JPanel(new BorderLayout());
        // Ajouter une bordure au panneau diagnostic
        diagnosticPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Diagnostic Final"), // Bordure titrée
                new EmptyBorder(10, 10, 10, 10) // Espace intérieur
        ));

        diagnosticArea = new JTextArea();
        diagnosticArea.setEditable(false);
        diagnosticArea.setLineWrap(true);
        diagnosticArea.setWrapStyleWord(true);
        diagnosticArea.setMargin(new Insets(8, 8, 8, 8)); // Marge interne

        JScrollPane diagnosticScrollPane = new JScrollPane(diagnosticArea);
        diagnosticPanel.add(diagnosticScrollPane, BorderLayout.CENTER);


        // Ajout des onglets au JTabbedPane
        tabbedPane.addTab("Consultation", consultationPanel);
        tabbedPane.addTab("Diagnostic", diagnosticPanel);


        // --- Gestion des événements ---

        // Événement pour le bouton Envoyer
        envoyerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Permettre l'envoi en appuyant sur Entrée dans le champ de texte
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Événement pour le bouton Quitter
        quitterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiEvent quitEvent = new GuiEvent(this, CMD_QUIT_CONSULTATION);
                if (patientContainer != null) {
                    patientContainer.postGuiEventToAgent(quitEvent);
                } else {
                    System.err.println("PatientContainer n'est pas défini dans ConsultationPatientInterface.");
                }
            }
        });
    }

    // Méthode pour envoyer un message (appelée par l'action listener)
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.trim().isEmpty()) {
            GuiEvent messageEvent = new GuiEvent(this, CMD_SEND_MESSAGE);
            messageEvent.addParameter(message);

            if (patientContainer != null) {
                patientContainer.postGuiEventToAgent(messageEvent);
            } else {
                System.err.println("PatientContainer n'est pas défini dans ConsultationPatientInterface.");
            }

            // Afficher le message du patient dans la zone de chat immédiatement
            appendMessage("Vous: " + message);

            messageField.setText("");
        }
    }

    // Méthode pour ajouter un message à la zone de chat (appelée par l'agent)
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    // Méthode pour afficher le diagnostic (appelée par l'agent)
    public void displayDiagnostic(String diagnostic) {
        SwingUtilities.invokeLater(() -> diagnosticArea.setText(diagnostic));
    }


    // Méthode pour afficher l'interface
    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    // Getter pour permettre à d'autres classes (comme le comportement de surveillance) d'accéder directement à cette instance GUI
    // Utile si la logique de mise à jour GUI est gérée en dehors du PatientContainer
    public JTextArea getChatArea() { return chatArea; }
    public JTextArea getDiagnosticArea() { return diagnosticArea; }

}