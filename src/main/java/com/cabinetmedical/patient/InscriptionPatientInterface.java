package com.cabinetmedical.patient;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import jade.gui.GuiEvent;

public class InscriptionPatientInterface extends JFrame {

    public static final int CMD_INSCRIPTION = 1;

    private JTextField nomField;
    private JTextField prenomField;
    private JTextField ageField;
    private JComboBox<String> sexeCombo;
    private JTextField adresseField;
    private JTextField telephoneField;
    private JTextField emailField;
    private JButton enregistrerButton;

    private PatientContainer patientContainer;
    private Color accentColor = new Color(41, 128, 185);

    public InscriptionPatientInterface(PatientContainer container) {
        this.patientContainer = container;

        // --- Configuration de la fenêtre ---
        setTitle("Cabinet Médical - Inscription Patient");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuration du look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Panneau principal avec background ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 245));
        setContentPane(mainPanel);

        // --- Bannière supérieure ---
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Panneau du formulaire ---
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Panneau des boutons ---
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Ajouter une petite animation au chargement
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                animateForm();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inscription Patient");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Service de consultation médicale à distance");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(220, 220, 220));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 245));
        formPanel.setBorder(new EmptyBorder(30, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 15, 5);
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;

        // Instructions
        JLabel instructionsLabel = new JLabel(
                "<html><body>Veuillez remplir ce formulaire pour vous inscrire et accéder à une consultation médicale à distance.</body></html>");
        instructionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(instructionsLabel, gbc);

        // Séparateur
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(200, 200, 200));
        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 20, 5);
        formPanel.add(separator, gbc);
        gbc.insets = new Insets(5, 5, 15, 5);

        // Titre section
        JLabel personInfoLabel = new JLabel("Informations Personnelles");
        personInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        personInfoLabel.setForeground(accentColor);
        gbc.gridy++;
        formPanel.add(personInfoLabel, gbc);

        // Nom
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel nomLabel = createFormLabel("Nom");
        formPanel.add(nomLabel, gbc);

        nomField = createFormTextField();
        gbc.gridx = 1;
        formPanel.add(nomField, gbc);

        // Prénom
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel prenomLabel = createFormLabel("Prénom");
        formPanel.add(prenomLabel, gbc);

        prenomField = createFormTextField();
        gbc.gridx = 1;
        formPanel.add(prenomField, gbc);

        // Âge
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel ageLabel = createFormLabel("Âge");
        formPanel.add(ageLabel, gbc);

        ageField = createFormTextField();
        gbc.gridx = 1;
        formPanel.add(ageField, gbc);

        // Sexe
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel sexeLabel = createFormLabel("Sexe");
        formPanel.add(sexeLabel, gbc);

        sexeCombo = new JComboBox<>(new String[]{"Sélectionnez", "Masculin", "Féminin", "Autre"});
        sexeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sexeCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        gbc.gridx = 1;
        formPanel.add(sexeCombo, gbc);

        // Titre section coordonnées
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel contactInfoLabel = new JLabel("Coordonnées");
        contactInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        contactInfoLabel.setForeground(accentColor);
        gbc.insets = new Insets(25, 5, 15, 5);
        formPanel.add(contactInfoLabel, gbc);
        gbc.insets = new Insets(5, 5, 15, 5);

        // Adresse
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel adresseLabel = createFormLabel("Adresse");
        formPanel.add(adresseLabel, gbc);

        adresseField = createFormTextField();
        gbc.gridx = 1;
        formPanel.add(adresseField, gbc);

        // Téléphone
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel telephoneLabel = createFormLabel("Téléphone");
        formPanel.add(telephoneLabel, gbc);

        telephoneField = createFormTextField();
        gbc.gridx = 1;
        formPanel.add(telephoneField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = createFormLabel("Email");
        formPanel.add(emailLabel, gbc);

        emailField = createFormTextField();
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Espace supplémentaire à la fin
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 1.0;
        formPanel.add(Box.createVerticalGlue(), gbc);

        return formPanel;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text + " :");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JTextField createFormTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 240, 245));
        buttonPanel.setBorder(new EmptyBorder(10, 40, 20, 40));

        enregistrerButton = new JButton("Enregistrer et Demander Consultation");
        enregistrerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        enregistrerButton.setBackground(Color.blue);
        enregistrerButton.setForeground(Color.BLACK);
        enregistrerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        enregistrerButton.setFocusPainted(false);
        enregistrerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Effet de survol
        enregistrerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                enregistrerButton.setBackground(accentColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                enregistrerButton.setBackground(accentColor);
            }
        });

        enregistrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    submitForm();
                }
            }
        });

        buttonPanel.add(enregistrerButton);
        return buttonPanel;
    }

    private boolean validateForm() {
        String nom = nomField.getText().trim();
        String age = ageField.getText().trim();
        String sexe = sexeCombo.getSelectedIndex() > 0 ? (String) sexeCombo.getSelectedItem() : "";
        String telephone = telephoneField.getText().trim();

        StringBuilder errors = new StringBuilder();

        if (nom.isEmpty()) errors.append("- Le nom est obligatoire\n");
        if (age.isEmpty()) {
            errors.append("- L'âge est obligatoire\n");
        } else {
            try {
                int ageValue = Integer.parseInt(age);
                if (ageValue <= 0 || ageValue > 120) {
                    errors.append("- L'âge doit être compris entre 1 et 120\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- L'âge doit être un nombre valide\n");
            }
        }
        if (sexe.isEmpty()) errors.append("- Veuillez sélectionner votre sexe\n");
        if (telephone.isEmpty()) errors.append("- Le téléphone est obligatoire\n");

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez corriger les erreurs suivantes:\n" + errors.toString(),
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void submitForm() {
        String nom = nomField.getText().trim();
        if (prenomField.getText().trim().length() > 0) {
            nom = nom + " " + prenomField.getText().trim();
        }
        String age = ageField.getText().trim();

        String sexe;
        switch(sexeCombo.getSelectedIndex()) {
            case 1: sexe = "M"; break;
            case 2: sexe = "F"; break;
            default: sexe = "A"; break;
        }

        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();

        // Créer l'événement avec les données du formulaire
        GuiEvent event = new GuiEvent(this, CMD_INSCRIPTION);
        event.addParameter(nom);
        event.addParameter(age);
        event.addParameter(sexe);
        event.addParameter(adresse);
        event.addParameter(telephone);

        // Envoyer l'événement à l'agent via le container
        patientContainer.postGuiEventToAgent(event);

        // Feedback visuel
        enregistrerButton.setText("Inscription envoyée...");
        enregistrerButton.setEnabled(false);

        // Animation de chargement et message de confirmation
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Votre demande d'inscription a été envoyée avec succès!\n" +
                                    "Veuillez patienter pendant que nous vérifions la disponibilité d'un médecin.",
                            "Inscription Enregistrée",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Réinitialiser le bouton après confirmation
                    enregistrerButton.setText("Enregistrer et Demander Consultation");
                    enregistrerButton.setEnabled(true);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void animateForm() {
        JPanel contentPane = (JPanel) getContentPane();
        float[] hsb = Color.RGBtoHSB(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), null);

        // Animation subtile de la couleur d'accentuation
        new Thread(() -> {
            try {
                for (float i = 0.7f; i <= 1.0f; i += 0.05f) {
                    final float brightness = i;
                    SwingUtilities.invokeLater(() -> {
                        Color animatedColor = Color.getHSBColor(hsb[0], hsb[1], brightness * hsb[2]);
                        ((JPanel)contentPane.getComponent(0)).setBackground(animatedColor);
                        enregistrerButton.setBackground(animatedColor);
                        contentPane.repaint();
                    });
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Méthode pour afficher l'interface
    public void display() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            nomField.requestFocus();
        });
    }

    // Getters pour les champs si nécessaire
    public String getNom() {
        return nomField.getText() + (prenomField.getText().trim().length() > 0 ? " " + prenomField.getText() : "");
    }
    public String getAge() { return ageField.getText(); }
    public String getSexe() {
        switch(sexeCombo.getSelectedIndex()) {
            case 1: return "M";
            case 2: return "F";
            default: return "A";
        }
    }
    public String getAdresse() { return adresseField.getText(); }
    public String getTelephone() { return telephoneField.getText(); }
    public String getEmail() { return emailField.getText(); }
}