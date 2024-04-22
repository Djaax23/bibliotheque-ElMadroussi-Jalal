import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Adherent extends JFrame {

    private static Connection connection;

    public static void main(String[] args) {
        // Crée une fenêtre principale
        JFrame fenetre = new JFrame();
        fenetre.setTitle("Adherent"); // Définit le titre de la fenêtre
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
        model.addColumn("Email");
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
                String email = JOptionPane.showInputDialog("Email:");

                if (nom != null && !nom.isEmpty() && prenom != null && !prenom.isEmpty() && email != null && !email.isEmpty()) {
                    ajouterLigne(nom, prenom, email, table, model);
                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs.");
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

    public static void ajouterLigne(String nom, String prenom, String email, JTable table, DefaultTableModel model) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO adherent (nom, prenom, email) VALUES (?, ?, ?)");
            statement.setString(1, nom);
            statement.setString(2, prenom);
            statement.setString(3, email);
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
            String email = (String) table.getValueAt(selectedRow, 2);

            try {
                PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM adherent WHERE nom = ? AND prenom = ? AND email = ?");
                statement.setString(1, nom);
                statement.setString(2, prenom);
                statement.setString(3, email);
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
            String email = (String) table.getValueAt(selectedRow, 2);

            String nouveauNom = JOptionPane.showInputDialog("Nouveau nom:", nom);
            String nouveauPrenom = JOptionPane.showInputDialog("Nouveau prénom:", prenom);
            String nouvelleemail = JOptionPane.showInputDialog("Nouvelle email:", email);

            if (nouveauNom != null && !nouveauNom.isEmpty() && nouveauPrenom != null && !nouveauPrenom.isEmpty() && email != null && !email.isEmpty()) {
                try {
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE adherent SET nom = ?, prenom = ?, email = ? WHERE nom = ? AND prenom = ? AND email = ?");
                    statement.setString(1, nouveauNom);
                    statement.setString(2, nouveauPrenom);
                    statement.setString(3, nouvelleemail);
                    statement.setString(4, nom);
                    statement.setString(5, prenom);
                    statement.setString(6, email);
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
            ResultSet resultSet = statement.executeQuery("SELECT nom, prenom, email FROM adherent");

            while (resultSet.next()) {
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                String email = resultSet.getString("email");

                model.addRow(new Object[]{nom, prenom, email});
            }

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}