import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.*;

public class UserManagementApp {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/crud_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    private JFrame frame;
    private JTextField nameField, emailField;
    private JButton addButton, updateButton, deleteButton;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private int selectedUserId = -1;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UserManagementApp window = new UserManagementApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public UserManagementApp() {
        initialize();
    }

    private void initialize() {
        // Set up frame
        frame = new JFrame("User Management App");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // Panel for input form
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        addButton = new JButton("Add User");
        updateButton = new JButton("Update User");
        deleteButton = new JButton("Delete User");
        inputPanel.add(addButton);
        inputPanel.add(updateButton);
        inputPanel.add(deleteButton);

        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);

        // Table to display users
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email"}, 0);
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = userTable.getSelectedRow();
                if (row != -1) {
                    selectedUserId = (int) userTable.getValueAt(row, 0);
                    nameField.setText((String) userTable.getValueAt(row, 1));
                    emailField.setText((String) userTable.getValueAt(row, 2));
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Action listeners for buttons
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());

        // Load users on startup
        loadUsers();
    }

    // Load users into the table
    private void loadUsers() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            tableModel.setRowCount(0);  // Clear existing data

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                tableModel.addRow(new Object[]{id, name, email});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading users: " + e.getMessage());
        }
    }

    // Add a new user to the database
    private void addUser() {
        String name = nameField.getText();
        String email = emailField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both name and email.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "INSERT INTO users (name, email) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();
            loadUsers(); // Refresh the user list
            nameField.setText("");
            emailField.setText("");
            JOptionPane.showMessageDialog(frame, "User added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error adding user: " + e.getMessage());
        }
    }

    // Update the selected user in the database
    private void updateUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a user to update.");
            return;
        }

        String name = nameField.getText();
        String email = emailField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both name and email.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "UPDATE users SET name = ?, email = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setInt(3, selectedUserId);
            stmt.executeUpdate();
            loadUsers(); // Refresh the user list
            selectedUserId = -1; // Reset selection
            nameField.setText("");
            emailField.setText("");
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            JOptionPane.showMessageDialog(frame, "User updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error updating user: " + e.getMessage());
        }
    }

    // Delete the selected user from the database
    private void deleteUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a user to delete.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedUserId);
            stmt.executeUpdate();
            loadUsers(); // Refresh the user list
            selectedUserId = -1; // Reset selection
            nameField.setText("");
            emailField.setText("");
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            JOptionPane.showMessageDialog(frame, "User deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting user: " + e.getMessage());
        }
    }
}
