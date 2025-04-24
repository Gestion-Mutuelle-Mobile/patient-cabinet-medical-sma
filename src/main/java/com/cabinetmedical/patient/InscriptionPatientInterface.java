package com.cabinetmedical.patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Pour ajouter de l'espace
import javax.swing.border.TitledBorder; // Pour ajouter un titre et un contour
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jade.gui.GuiEvent;

public class InscriptionPatientInterface extends JFrame {

    public static final int CMD_INSCRIPTION = 1;

    private JTextField nomField;
    private JTextField ageField;
    private JTextField sexeField;
    private JTextField adresseField;
    private JTextField telephoneField;
    private JButton enregistrerButton;

    private PatientContainer patientContainer;

    public InscriptionPatientInterface(PatientContainer container) {
        this.patientContainer = container;

        // --- Configuration de la fenêtre ---
        setTitle("Inscription Patient");
        setSize(500, 450); // Taille augmentée
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer la fenêtre

        // Utilisation d'un BorderLayout pour la structure principale
        setLayout(new BorderLayout());

        // --- Panneau principal avec une bordure pour l'espacement extérieur ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Ajouter de l'espace autour du contenu principal
        add(mainPanel, BorderLayout.CENTER);

        // --- Panneau pour les champs d'entrée avec une bordure titrée ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        // Ajouter une bordure avec un titre
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Informations Personnelles", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Augmenter les marges entre les composants
        gbc.fill = GridBagConstraints.HORIZONTAL; // Les champs de texte s'étirent horizontalement

        // Labels et champs de texte
        JLabel nomLabel = new JLabel("Nom :");
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; // Alignement à gauche
        inputPanel.add(nomLabel, gbc);
        nomField = new JTextField(30); // Largeur préférée
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; // Le champ prend l'espace restant
        inputPanel.add(nomField, gbc);

        JLabel ageLabel = new JLabel("Âge :");
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        inputPanel.add(ageLabel, gbc);
        ageField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        inputPanel.add(ageField, gbc);

        JLabel sexeLabel = new JLabel("Sexe :");
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        inputPanel.add(sexeLabel, gbc);
        sexeField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        inputPanel.add(sexeField, gbc);

        JLabel adresseLabel = new JLabel("Adresse :");
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        inputPanel.add(adresseLabel, gbc);
        adresseField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        inputPanel.add(adresseField, gbc);

        JLabel telephoneLabel = new JLabel("Téléphone :");
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        inputPanel.add(telephoneLabel, gbc);
        telephoneField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        inputPanel.add(telephoneField, gbc);

        mainPanel.add(inputPanel, BorderLayout.CENTER); // Ajouter le panneau d'entrée au centre du panneau principal

        // --- Panneau pour le bouton avec un alignement centré ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Utiliser FlowLayout centré
        enregistrerButton = new JButton("Enregistrer et Demander Consultation");
        buttonPanel.add(enregistrerButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Ajouter le panneau du bouton en bas du panneau principal


        // Ajout d'un écouteur d'événement au bouton (inchangé)
        enregistrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupérer les données des champs
                String nom = nomField.getText();
                String age = ageField.getText();
                String sexe = sexeField.getText();
                String adresse = adresseField.getText();
                String telephone = telephoneField.getText();

                // Créer un GuiEvent avec les données
                GuiEvent registrationEvent = new GuiEvent(this, CMD_INSCRIPTION);
                registrationEvent.addParameter(nom);
                registrationEvent.addParameter(age);
                registrationEvent.addParameter(sexe);
                registrationEvent.addParameter(adresse);
                registrationEvent.addParameter(telephone);

                // Envoyer l'événement à l'agent via le PatientContainer
                if (patientContainer != null) {
                    patientContainer.postGuiEventToAgent(registrationEvent);
                    // setVisible(false); // Cacher cette fenêtre après l'envoi
                } else {
                    System.err.println("PatientContainer n'est pas défini dans InscriptionPatientInterface.");
                }
            }
        });
    }

    // Méthode pour afficher l'interface
    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    // Getters pour les champs (peuvent être utiles si le conteneur récupère directement les données au lieu de passer par l'événement)
    public String getNom() { return nomField.getText(); }
    public String getAge() { return ageField.getText(); }
    public String getSexe() { return sexeField.getText(); }
    public String getAdresse() { return adresseField.getText(); }
    public String getTelephone() { return telephoneField.getText(); }
}