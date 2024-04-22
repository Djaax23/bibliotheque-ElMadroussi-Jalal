import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Auteur extends JFrame {

    private static Connection connection;

    public static void main(String[] args) {
        // Crée une fenêtre principale
        JFrame fenetre = new JFrame();
        fenetre.setTitle("Auteur"); // Définit le titre de la fenêtre
        fenetre.setSize(650, 500); // Définit la taille de la fenêtre
        fenetre.setResizable(true); // Empêche le redimensionnement de la fenêtre
        fenetre.setLocationRelativeTo(null); // Centre la fenêtre sur l'écran
        

        // Connexion à la base de données
        try {
            // Chargement du driver JDBC
            Class.forName("com.mysql.jdbc.Driver");
            // Connexion à la base de données
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ap2", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Création de la table
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Nom");
        model.addColumn("Prénom");
        model.addColumn("Date de Naissance");
        model.addColumn("Description");
        table.setModel(model);
        refreshTable(table, model);
        table.getTableHeader().setReorderingAllowed(false); // Empêcher le déplacement des colonnes
        // Désactiver l'édition des cellules par double-clic
        table.setDefaultEditor(Object.class, null);

        // Ajout de la table à un JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);

        // Création des boutons
        JButton ajouterButton = new JButton("Ajouter");
        JButton supprimerButton = new JButton("Supprimer");
        JButton modifierButton = new JButton("Modifier");

        // Ajout des actions aux boutons
        ajouterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = JOptionPane.showInputDialog("Nom:");
                String prenom = JOptionPane.showInputDialog("Prénom:");
                String dateNaissance = JOptionPane.showInputDialog("Date de naissance:");
                String description = JOptionPane.showInputDialog("Description:");

                if (nom != null && !nom.isEmpty() && prenom != null && !prenom.isEmpty() && dateNaissance != null && dateNaissance.matches("\\d{4}-\\d{2}-\\d{2}" )&& description != null && !description.isEmpty()) {
                    ajouterLigne(nom, prenom, dateNaissance, description, table, model);
                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs et respecter le format de la date (aaaa-mm-jj).");
                }
            }
        });

        supprimerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supprimerLigne(table, model);
            }
        });

        modifierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifierLigne(table, model);
            }
        });

        // Création du panneau pour les boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ajouterButton);
        buttonPanel.add(supprimerButton);
        buttonPanel.add(modifierButton);

        // Ajout des composants à la fenêtre
        fenetre.setLayout(new BorderLayout());
        fenetre.add(scrollPane, BorderLayout.CENTER);
        fenetre.add(buttonPanel, BorderLayout.SOUTH);

        // Affiche la fenêtre
        fenetre.setVisible(true);
    }

    public static void ajouterLigne(String nom, String prenom, String dateNaissance, String description, JTable table, DefaultTableModel model) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO auteur (nom, prenom, date_naissance, description) VALUES (?, ?, ?, ?)");
            statement.setString(1, nom);
            statement.setString(2, prenom);
            statement.setString(3, dateNaissance);
            statement.setString(4, description);
            statement.executeUpdate();
            statement.close();

            refreshTable(table, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void supprimerLigne(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String nom = (String) table.getValueAt(selectedRow, 0);
            String prenom = (String) table.getValueAt(selectedRow, 1);
            String dateNaissance = (String) table.getValueAt(selectedRow, 2);
            String description = (String) table.getValueAt(selectedRow, 3);

            try {
                PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM auteur WHERE nom = ? AND prenom = ? AND date_naissance = ? AND description = ?");
                statement.setString(1, nom);
                statement.setString(2, prenom);
                statement.setString(3, dateNaissance);
                statement.setString(4, description);
                statement.executeUpdate();
                statement.close();

                refreshTable(table, model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void modifierLigne(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String nom = (String) table.getValueAt(selectedRow, 0);
            String prenom = (String) table.getValueAt(selectedRow, 1);
            String dateNaissance = (String) table.getValueAt(selectedRow, 2);
            String description = (String) table.getValueAt(selectedRow, 3);

            String nouveauNom = JOptionPane.showInputDialog("Nouveau nom:", nom);
            String nouveauPrenom = JOptionPane.showInputDialog("Nouveau prénom:", prenom);
            String nouvelleDateNaissance = JOptionPane.showInputDialog("Nouvelle date de naissance:", dateNaissance);
            String nouvelledescription = JOptionPane.showInputDialog("Nouvelle description:", description);

            if (nouveauNom != null && !nouveauNom.isEmpty() && nouveauPrenom != null && !nouveauPrenom.isEmpty() && nouvelleDateNaissance != null && nouvelleDateNaissance.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])")&& description != null && !description.isEmpty()) {
                try {
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE auteur SET nom = ?, prenom = ?, date_naissance = ?, description = ? WHERE nom = ? AND prenom = ? AND date_naissance = ? AND description = ?");
                    statement.setString(1, nouveauNom);
                    statement.setString(2, nouveauPrenom);
                    statement.setString(3, nouvelleDateNaissance);
                    statement.setString(4, nouvelledescription);
                    statement.setString(5, nom);
                    statement.setString(6, prenom);
                    statement.setString(7, dateNaissance);
                    statement.setString(8, description);
                    statement.executeUpdate();
                    statement.close();
    
                    refreshTable(table, model);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs et respecter le format de la date (aaaa-mm-jj).");
            }

            
        }
    }

    private static void refreshTable(JTable table, DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT nom, prenom, date_naissance, description FROM auteur");

            while (resultSet.next()) {
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                String dateNaissance = resultSet.getString("date_naissance");
                String description = resultSet.getString("description");

                model.addRow(new Object[]{nom, prenom, dateNaissance, description});
            }

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}