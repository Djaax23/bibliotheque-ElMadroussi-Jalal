import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Livre extends JFrame {

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
        model.addColumn("Titre");
        model.addColumn("Prix");
        model.addColumn("Auteur");
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
                String titre = JOptionPane.showInputDialog("Titre:");
                String prix = JOptionPane.showInputDialog("Prix:");

                // Liste déroulante pour les auteurs
                JComboBox<String> jComboBox = new JComboBox<>();
                jComboBox.setBounds(80, 50, 140, 20);
                try {
                    PreparedStatement statement = connection.prepareStatement("SELECT num, nom, prenom FROM auteur");
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

                int idAuteurSelectionne = -1; // Variable pour stocker l'ID de l'auteur sélectionné

                int result = JOptionPane.showConfirmDialog(null, jComboBox, "Sélectionnez un auteur",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    int selectedIndex = jComboBox.getSelectedIndex();
                    try {
                        PreparedStatement statement = connection.prepareStatement("SELECT num FROM auteur LIMIT ?, 1");
                        statement.setInt(1, selectedIndex);
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            idAuteurSelectionne = resultSet.getInt("num");
                        }
                        resultSet.close();
                        statement.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez sélectionner un auteur.");
                    return; // Sortir de la méthode si aucun auteur n'est sélectionné
                }

                if (titre != null && !titre.isEmpty() && prix != null && !prix.isEmpty()) {
                    ajouterLigne(titre, prix, idAuteurSelectionne, table, model);
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
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String ancienTitre = (String) table.getValueAt(selectedRow, 0);
                    String ancienPrix = (String) table.getValueAt(selectedRow, 1);
                    String ancienAuteur = (String) table.getValueAt(selectedRow, 2);
                    System.out.println("ancien auteur : " + ancienAuteur);

                    String nouveauTitre = JOptionPane.showInputDialog("Nouveau titre:", ancienTitre);

                    String nouveauPrix = JOptionPane.showInputDialog("Nouveau prix:", ancienPrix);

                    // Debug
                    System.out.println("Nouveau Titre : " + nouveauTitre);
                    System.out.println("Nouveau Prix : " + nouveauPrix);

                    // Liste déroulante pour les auteurs
                    JComboBox<String> jComboBox = new JComboBox<>();
                    jComboBox.setBounds(80, 50, 140, 20);
                    try {
                        PreparedStatement statement = connection
                                .prepareStatement("SELECT num, nom, prenom FROM auteur");
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

                    int idAuteurSelectionne = -1; // Variable pour stocker l'ID de l'auteur sélectionné

                    int result = JOptionPane.showConfirmDialog(null, jComboBox, "Sélectionnez un auteur",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        String nomComplet = (String) jComboBox.getSelectedItem();
                        System.out.println("Nom complet sélectionné : " + nomComplet);
                        String[] nomPrenom = nomComplet.split(" ");
                        String nom = nomPrenom[0];
                        String prenom = nomPrenom[1];
                        System.out.println("Nom : " + nom + ", Prénom : " + prenom);
                        try {
                            PreparedStatement statement = connection
                                    .prepareStatement("SELECT num FROM auteur WHERE nom = ? AND prenom = ?");
                            statement.setString(1, nom);
                            statement.setString(2, prenom);
                            ResultSet resultSet = statement.executeQuery();
                            if (resultSet.next()) {
                                idAuteurSelectionne = resultSet.getInt("num");
                                System.out.println("ID de l'auteur sélectionné : " + idAuteurSelectionne);
                            }
                            resultSet.close();
                            statement.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Veuillez sélectionner un auteur.");
                        return; // Sortir de la méthode si aucun auteur n'est sélectionné
                    }

                    if (nouveauTitre != null && !nouveauTitre.isEmpty() && nouveauPrix != null
                            && !nouveauPrix.isEmpty()) {
                                System.out.println("on est la");
                                modifierLigne(ancienTitre, ancienPrix, ancienAuteur, nouveauTitre, nouveauPrix, idAuteurSelectionne, table, model);
                    } else {
                        JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs.");
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Veuillez sélectionner une ligne à modifier.");
                }
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

    public static void ajouterLigne(String titre, String prix, int idAuteur, JTable table, DefaultTableModel model) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO livre (titre, prix, auteur) VALUES (?, ?, ?)");
            statement.setString(1, titre);
            statement.setString(2, prix);
            statement.setInt(3, idAuteur);
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
            String titre = (String) table.getValueAt(selectedRow, 0);
            String prix = (String) table.getValueAt(selectedRow, 1);
            String auteur = (String) table.getValueAt(selectedRow, 2);

            try {

                // Récupérer l'ID de l'auteur à partir de son ancien nom complet
        String[] parts = auteur.split(" ");
        String nom = parts[0];
        String prenom = parts[1];
        PreparedStatement statement52 = connection.prepareStatement("SELECT num FROM auteur WHERE nom = ? AND prenom = ?");
        statement52.setString(1, nom);
        statement52.setString(2, prenom);
        ResultSet resultSet = statement52.executeQuery();
        if (resultSet.next()) {
            auteur = resultSet.getString("num");
        }
                PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM livre WHERE titre = ? AND prix = ? AND auteur = ?");
                statement.setString(1, titre);
                statement.setString(2, prix);
                statement.setString(3, auteur);
                statement.executeUpdate();
                statement.close();

                refreshTable(table, model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void modifierLigne(String ancienTitre, String ancienPrix, String ancienAuteur, String nouveauTitre,
    String nouveauPrix, int idAuteurSelectionne, JTable table, DefaultTableModel model) {
    try {
        // Récupérer l'ID de l'auteur à partir de son ancien nom complet
        String[] parts = ancienAuteur.split(" ");
        String nom = parts[0];
        String prenom = parts[1];
        PreparedStatement statement = connection.prepareStatement("SELECT num FROM auteur WHERE nom = ? AND prenom = ?");
        statement.setString(1, nom);
        statement.setString(2, prenom);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            ancienAuteur = resultSet.getString("num");
        }
        resultSet.close();
        statement.close();

        // Mettre à jour la ligne dans la base de données
        PreparedStatement updateStatement = connection.prepareStatement(
                "UPDATE livre SET titre = ?, prix = ?, auteur = ? WHERE titre = ? AND prix = ? AND auteur = ?");
        updateStatement.setString(1, nouveauTitre);
        updateStatement.setString(2, nouveauPrix);
        updateStatement.setInt(3, idAuteurSelectionne);
        updateStatement.setString(4, ancienTitre);
        updateStatement.setString(5, ancienPrix);
        updateStatement.setString(6, ancienAuteur);

        System.out.println("Nouveau Titre : " + nouveauTitre);
        System.out.println("Nouveau Prix : " + nouveauPrix);
        System.out.println("ID de l'auteur sélectionné : " + idAuteurSelectionne);

        updateStatement.executeUpdate();
        updateStatement.close();

        refreshTable(table, model);
    } catch (Exception e) {
        e.printStackTrace();
    }
}




    private static void refreshTable(JTable table, DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT livre.titre, livre.prix, livre.auteur, auteur.nom, auteur.prenom FROM livre INNER JOIN auteur ON livre.auteur = auteur.num");

            while (resultSet.next()) {
                String titre = resultSet.getString("titre");
                String prix = resultSet.getString("prix");
                String nom_auteur = resultSet.getString("nom");
                String prenom_auteur = resultSet.getString("prenom");
                String together = nom_auteur + " " + prenom_auteur;

                model.addRow(new Object[] { titre, prix, together });
            }

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}