package com.cabinetmedical.patient;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jade.gui.GuiEvent;

public class ConsultationPatientInterface extends JFrame {

    public static final int CMD_SEND_MESSAGE = 2;
    public static final int CMD_QUIT_CONSULTATION = 3;

    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton envoyerButton;
    private JButton quitterButton;
    private JTextPane diagnosticPane;
    private JLabel statusLabel;
    private Font chatFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Color userBubbleColor = new Color(224, 242, 255);
    private Color doctorBubbleColor = new Color(255, 235, 235);
    private Color systemMessageColor = new Color(245, 245, 245);

    private PatientContainer patientContainer;
    private String patientName;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public ConsultationPatientInterface(PatientContainer container) {
        this.patientContainer = container;

        // Configuration de la fenêtre
        setTitle("Cabinet Médical - Consultation");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Essayer d'appliquer le look and feel du système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Conteneur principal
        JPanel contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(new Color(240, 240, 245));
        setContentPane(contentPane);

        // Barre d'en-tête
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Panneau à onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Onglet Consultation
        JPanel consultationPanel = createConsultationPanel();
        tabbedPane.addTab("Consultation", consultationPanel);

        // Onglet Diagnostic
        JPanel diagnosticPanel = createDiagnosticPanel();
        tabbedPane.addTab("Diagnostic", diagnosticPanel);

        // Message de bienvenue
        appendSystemMessage("Bienvenue dans votre consultation médicale. Veuillez patienter pendant que nous vous connectons à un médecin disponible.");

        // Configuration des événements
        configureEventListeners();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("Consultation Médicale");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("En attente d'un médecin disponible...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(Color.WHITE);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createConsultationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panneau de chat avec défilement vertical
        chatPanel = new JPanel();
        // Utiliser un layout vertical plus approprié
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        // IMPORTANT: Ajouter un JPanel avec glue qui pousse tout vers le haut
        JPanel viewportPanel = new JPanel(new BorderLayout());
        viewportPanel.setBackground(Color.WHITE);
        viewportPanel.add(chatPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(viewportPanel); // Changer pour utiliser viewportPanel
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Panneau pour saisie de message
        panel.add(createInputPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Champ de texte avec style
        messageField = new JTextField();
        messageField.setFont(chatFont);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        inputPanel.add(messageField, BorderLayout.CENTER);

        // Panneau pour les boutons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        // Bouton Envoyer
        envoyerButton = new JButton("Envoyer");
        styleButton(envoyerButton, new Color(41, 128, 185));
        envoyerButton.setForeground(Color.BLACK);

        buttonsPanel.add(envoyerButton);

        // Bouton Quitter
        quitterButton = new JButton("Terminer");
        quitterButton.setForeground(Color.black);
        styleButton(quitterButton, new Color(231, 76, 60));
        buttonsPanel.add(quitterButton);

        inputPanel.add(buttonsPanel, BorderLayout.EAST);
        return inputPanel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Effet de survol
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(
                        Math.max((int)(bgColor.getRed() * 0.8), 0),
                        Math.max((int)(bgColor.getGreen() * 0.8), 0),
                        Math.max((int)(bgColor.getBlue() * 0.8), 0)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private JPanel createDiagnosticPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Label pour le titre
        JLabel titleLabel = new JLabel("Diagnostic Médical");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(41, 128, 185));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Zone de texte pour le diagnostic
        diagnosticPane = new JTextPane();
        diagnosticPane.setEditable(false);
        diagnosticPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        diagnosticPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        diagnosticPane.setBackground(Color.WHITE);

        // Message initial
        try {
            diagnosticPane.getDocument().insertString(0,
                    "Le diagnostic sera affiché ici à la fin de votre consultation avec le médecin.",
                    null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(diagnosticPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void configureEventListeners() {
        ActionListener sendAction = e -> sendMessage();

        envoyerButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction);

        quitterButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Êtes-vous sûr de vouloir terminer la consultation ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                GuiEvent quitEvent = new GuiEvent(this, CMD_QUIT_CONSULTATION);
                patientContainer.postGuiEventToAgent(quitEvent);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitterButton.doClick();
            }
        });
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            GuiEvent messageEvent = new GuiEvent(this, CMD_SEND_MESSAGE);
            messageEvent.addParameter(message);
            patientContainer.postGuiEventToAgent(messageEvent);

            // Afficher le message
            appendUserMessage(message);

            messageField.setText("");
            messageField.requestFocus();
        }
    }

    // Méthodes pour ajouter des messages au chat
    public void appendUserMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel bubblePanel = createBubblePanel(message, userBubbleColor, true);
            chatPanel.add(bubblePanel);
            updateChatPanel();
        });
    }

    public void appendDoctorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel bubblePanel = createBubblePanel(message, doctorBubbleColor, false);
            chatPanel.add(bubblePanel);
            updateChatPanel();
        });
    }

    public void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel msgPanel = new JPanel(new BorderLayout());
            msgPanel.setBackground(Color.WHITE);
            msgPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
            // Ne pas fixer de taille maximale
            // msgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JLabel timeLabel = new JLabel(LocalDateTime.now().format(timeFormatter));
            timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            timeLabel.setForeground(new Color(150, 150, 150));

            // Utiliser un JTextPane pour un meilleur rendu HTML
            JTextPane msgLabel = new JTextPane();
            msgLabel.setContentType("text/html");
            msgLabel.setText("<html><body><i>" + message + "</i></body></html>");
            msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            msgLabel.setForeground(new Color(100, 100, 100));
            msgLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
            msgLabel.setEditable(false);
            msgLabel.setOpaque(false);
            msgLabel.setBackground(new Color(0,0,0,0)); // Transparent

            JPanel innerPanel = new JPanel(new BorderLayout());
            innerPanel.setBackground(systemMessageColor);
            innerPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            innerPanel.add(timeLabel, BorderLayout.NORTH);
            innerPanel.add(msgLabel, BorderLayout.CENTER);

            msgPanel.add(innerPanel, BorderLayout.CENTER);
            chatPanel.add(msgPanel);
            updateChatPanel();
        });
    }

    private JPanel createBubblePanel(String message, Color bubbleColor, boolean isUser) {
        // Panel principal qui contient toute la bulle
        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setBackground(Color.WHITE);
        bubblePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Panel qui contiendra la bulle avec bordure arrondie
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(bubbleColor);


        // Bordures arrondies
        bubble.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(bubbleColor, 15),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Panel pour l'en-tête (nom + heure)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(isUser ? "Vous" : "Dr. Médecin");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(isUser ? new Color(0, 90, 150) : new Color(150, 0, 0));

        JLabel timeLabel = new JLabel(LocalDateTime.now().format(timeFormatter));
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        timeLabel.setForeground(new Color(100, 100, 100));

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);

        // Zone de texte pour le message
        JTextArea textArea = new JTextArea(message);
        textArea.setFont(chatFont);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(null);

        // Limiter la largeur du texte
        int maxWidth = 400; // Largeur maximale en pixels
        Dimension textSize = textArea.getPreferredSize();
        if (textSize.width > maxWidth) {
            textSize.width = maxWidth;
            textArea.setSize(textSize);
        }

        // Assembler la bulle
        bubble.add(headerPanel);
        bubble.add(Box.createVerticalStrut(5)); // Petit espace
        bubble.add(textArea);

        // Panel wrapper pour aligner la bulle
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(bubble);

        bubblePanel.add(wrapperPanel, BorderLayout.CENTER);

        return bubblePanel;
    }

    private void updateChatPanel() {
        chatPanel.revalidate();
        chatPanel.repaint();

        // Faire défiler automatiquement vers le bas - corriger le problème de défilement
        SwingUtilities.invokeLater(() -> {
            // Défilement automatique amélioré
            Rectangle bounds = null;
            try {
                bounds = chatPanel.getComponent(chatPanel.getComponentCount() - 1).getBounds();
            } catch (ArrayIndexOutOfBoundsException e) {
                // Si aucun composant n'est présent, ignorer
                return;
            }

            // S'assurer que le rectangle est visible
            Rectangle visibleRect = new Rectangle(bounds.x, bounds.y + bounds.height, bounds.width, 10);
            chatPanel.scrollRectToVisible(visibleRect);

            // Alternative si le scrollRectToVisible ne fonctionne pas
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void displayDiagnostic(String diagnostic) {
        SwingUtilities.invokeLater(() -> {
            try {
                diagnosticPane.getDocument().remove(0, diagnosticPane.getDocument().getLength());

                SimpleAttributeSet titleStyle = new SimpleAttributeSet();
                StyleConstants.setBold(titleStyle, true);
                StyleConstants.setFontSize(titleStyle, 18);
                StyleConstants.setForeground(titleStyle, new Color(192, 57, 43));

                SimpleAttributeSet contentStyle = new SimpleAttributeSet();
                StyleConstants.setFontFamily(contentStyle, "Segoe UI");
                StyleConstants.setFontSize(contentStyle, 14);

                diagnosticPane.getDocument().insertString(0, "DIAGNOSTIC MÉDICAL\n\n", titleStyle);
                diagnosticPane.getDocument().insertString(diagnosticPane.getDocument().getLength(), diagnostic, contentStyle);

                // Changer l'onglet actif pour afficher le diagnostic
                ((JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, diagnosticPane)).setSelectedIndex(1);

                // Notification
                JOptionPane.showMessageDialog(this,
                        "Le diagnostic est maintenant disponible dans l'onglet Diagnostic.",
                        "Diagnostic Disponible",
                        JOptionPane.INFORMATION_MESSAGE);

                // Mettre à jour le statut
                updateStatus("Consultation terminée - Diagnostic disponible");

            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    // Méthode pour afficher l'interface
    public void display() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            messageField.requestFocus();
        });
    }

    // Méthode de compatibilité avec l'ancien code
    public void appendMessage(String message) {
        if (message.startsWith("Vous: ")) {
            appendUserMessage(message.substring(6));
        } else if (message.startsWith("Médecin: ") || message.startsWith("Dr.")) {
            appendDoctorMessage(message.substring(message.indexOf(": ") + 2));
        } else if (message.startsWith("Système: ")) {
            appendSystemMessage(message.substring(9));
        } else {
            appendSystemMessage(message);
        }
    }

    // Classe pour créer des bordures arrondies
    private class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Remplir avec la couleur de fond
            g2d.setColor(color);
            g2d.fillRoundRect(x, y, width - 1, height - 1, radius, radius);

//            // Optionnel: dessiner un contour légèrement plus foncé pour l'effet 3D
//            g2d.setColor(color.darker());
//            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    // Méthodes supplémentaires pour la mise à jour de l'interface selon l'état de la consultation
    public void setConsultationStarted(String doctorName) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("En consultation avec Dr. " + doctorName);
            appendSystemMessage("Le Dr. " + doctorName + " a rejoint la consultation. Vous pouvez maintenant discuter.");
            // Activer les contrôles
            messageField.setEnabled(true);
            envoyerButton.setEnabled(true);
        });
    }

    public void setConsultationEnded() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Consultation terminée");
            appendSystemMessage("La consultation est maintenant terminée.");
            // Désactiver les contrôles
            messageField.setEnabled(false);
            envoyerButton.setEnabled(false);
        });
    }

    public void setWaitingForDoctor() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("En attente d'un médecin disponible...");
            // Désactiver temporairement les contrôles
            messageField.setEnabled(false);
            envoyerButton.setEnabled(false);
        });
    }

    public void setDoctorFound() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Médecin trouvé - Connexion en cours...");
            appendSystemMessage("Un médecin est disponible. Établissement de la connexion...");
        });
    }
}