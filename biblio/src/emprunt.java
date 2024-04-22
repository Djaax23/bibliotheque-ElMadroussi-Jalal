import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;

public class emprunt extends JFrame {

    private static Connection connection;

    public static void main(String[] args) {
        // Crée une fenêtre principale
        JFrame fenetre = new JFrame();
        fenetre.setTitle("Livre"); // Définit le titre de la fenêtre
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
        model.addColumn("N° d'emprunt");
        model.addColumn("Titre");
        model.addColumn("Adherent");
        model.addColumn("Date d'emprunt");
        model.addColumn("Date de retour");
        table.setModel(model);
        refreshTable(table, model);
        table.getTableHeader().setReorderingAllowed(false); // Empêcher le déplacement des colonnes
        // Désactiver l'édition des cellules par double-clic
        table.setDefaultEditor(Object.class, null);

        // Ajout de la table à un JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);

        // Création des boutons
        JButton emprunterButton = new JButton("Ajouter");
        JButton retournerButton = new JButton("Supprimer");

        retournerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supprimerLigne(table, model);
            }
        });

        emprunterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> jComboBox = new JComboBox<>();
                jComboBox.setBounds(80, 50, 140, 20);
                try {
                    PreparedStatement statement = connection.prepareStatement("SELECT num, nom, prenom FROM adherent");
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        
                        String nom = resultSet.getString("nom");
                        String prenom = resultSet.getString("prenom");
                        String nomComplet = nom + " " + prenom;
                        jComboBox.addItem(nomComplet);
                    }
                    resultSet.close();
                    statement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                fenetre.add(jComboBox);
        
                JComboBox<String> jComboBox1 = new JComboBox<>();
                jComboBox1.setBounds(80, 50, 140, 20);
                try {
                    PreparedStatement statement2 = connection.prepareStatement("SELECT ISBN, titre FROM livre");
                    ResultSet resultSet2 = statement2.executeQuery();
                    while (resultSet2.next()) {
                        String ISBN = resultSet2.getString("ISBN");
                        String titre = resultSet2.getString("titre");
                        jComboBox1.addItem(ISBN+" " + titre);
                    }
                    resultSet2.close();
                    statement2.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                fenetre.add(jComboBox1);
        
                int idAdherentSelectionne = -1; // Variable pour stocker l'ID de l'adhérent sélectionné
                int idLivreSelectionne = -1; // Variable pour stocker l'ID du livre sélectionné
        
                int result = JOptionPane.showConfirmDialog(null, jComboBox, "Sélectionnez un adherent",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String selectedAdherent = jComboBox.getSelectedItem().toString();
                    String[] adherentParts = selectedAdherent.split(" ");
                    String nomAdherent = adherentParts[0];
                    String prenomAdherent = adherentParts[1];
                    try {
                        PreparedStatement statement = connection.prepareStatement("SELECT num FROM adherent WHERE nom = ? AND prenom = ?");
                        statement.setString(1, nomAdherent);
                        statement.setString(2, prenomAdherent);
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            
                            idAdherentSelectionne = resultSet.getInt("num");
                            System.out.println("adherent : " + idAdherentSelectionne);
                        }
                        resultSet.close();
                        statement.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez sélectionner un adherent.");
                    return;
                }
        
                int result1 = JOptionPane.showConfirmDialog(null, jComboBox1, "Sélectionnez un livre",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result1 == JOptionPane.OK_OPTION) {
                    String selectedLivre = jComboBox1.getSelectedItem().toString();
                    String[] livreParts = selectedLivre.split(" ");
                    idLivreSelectionne = Integer.parseInt(livreParts[0]); // Récupérer l'ID du livre
                    System.out.println("Livre : " + idLivreSelectionne);
                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez sélectionner un livre.");
                    return;
                }
        
                ajouterLigne(idLivreSelectionne, idAdherentSelectionne, table, model);
            }
        });

        // Création du panneau pour les boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(emprunterButton);
        buttonPanel.add(retournerButton);

        // Ajout des composants à la fenêtre
        fenetre.setLayout(new BorderLayout());
        fenetre.add(scrollPane, BorderLayout.CENTER);
        fenetre.add(buttonPanel, BorderLayout.SOUTH);

        // Affiche la fenêtre
        fenetre.setVisible(true);
    }
    public static void ajouterLigne(int idLivreSelectionne, int idAdherentSelectionne, JTable table, DefaultTableModel model) {
        try {
            LocalDate date_emprunt = LocalDate.now();
            LocalDate date_retour = date_emprunt.plusWeeks(4);
    
            // Conversion de LocalDate en java.sql.Date
            java.sql.Date sqlDate_emprunt = java.sql.Date.valueOf(date_emprunt);
            java.sql.Date sqlDate_retour = java.sql.Date.valueOf(date_retour);
    
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO emprunt (id_livre, id_adherent, date_emprunt, date_retour) VALUES (?, ?, ?, ?)");
    
            statement.setInt(1, idLivreSelectionne); // Utilisation de idLivreSelectionne
            statement.setInt(2, idAdherentSelectionne); // Utilisation de idAdherentSelectionne
            statement.setDate(3, sqlDate_emprunt);
            statement.setDate(4, sqlDate_retour);
    
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
            String id = (String) table.getValueAt(selectedRow, 0);

            try {

                // Récupérer l'ID de l'auteur à partir de son ancien nom complet
        
                PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM emprunt WHERE id_emprunt = ? ");
                statement.setString(1, id);
                statement.executeUpdate();
                statement.close();

                refreshTable(table, model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void refreshTable(JTable table, DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT emprunt.id_emprunt, emprunt.id_adherent, adherent.nom, adherent.prenom, emprunt.id_livre, livre.Titre, emprunt.date_emprunt, emprunt.date_retour FROM emprunt INNER JOIN livre ON livre.ISBN = emprunt.id_livre INNER JOIN adherent ON adherent.num = emprunt.id_adherent ORDER BY id_emprunt");

            while (resultSet.next()) {
                String id_emprunt = resultSet.getString("id_emprunt");
                String nom_adherent = resultSet.getString("nom");
                String prenom_adherent = resultSet.getString("prenom");
                String titre_livre = resultSet.getString("Titre");
                String date_emprunt = resultSet.getString("date_emprunt");
                String date_retour = resultSet.getString("date_retour");
                String together = nom_adherent + " " + prenom_adherent;

                model.addRow(new Object[] { id_emprunt, titre_livre, together, date_emprunt, date_retour });
            }

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
