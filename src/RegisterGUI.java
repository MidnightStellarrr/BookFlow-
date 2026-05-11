import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterGUI extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField, confirmField;
    
    public RegisterGUI() {
        setTitle("AVL Scheduler - Register");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, gbc);
        
        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        emailField = new JTextField(15);
        panel.add(emailField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        confirmField = new JPasswordField(15);
        panel.add(confirmField, gbc);
        
        // Buttons
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        
        registerBtn.addActionListener(e -> register());
        backBtn.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(backBtn);
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void register() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if username exists
            String checkSql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }
            
            // Insert new user
            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
            new LoginGUI().setVisible(true);
            dispose();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}