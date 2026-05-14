import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class RegisterGUI extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    
    public RegisterGUI() {
        setTitle("Arc - Register");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set window icon
        try {
            ImageIcon logo = new ImageIcon("src/images/arc_logo.png");
            setIconImage(logo.getImage());
        } catch (Exception e) {
            System.out.println("Logo not found");
        }
        
        // Use BorderLayout with zero gaps
        setLayout(new BorderLayout(0, 0));
        
        // ===== LEFT PANEL WITH HIGH QUALITY IMAGE =====
        JPanel leftImgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    BufferedImage originalImage = ImageIO.read(new File("src/images/left_img_bg.jpg"));
                    if (originalImage != null) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                            RenderingHints.VALUE_RENDER_QUALITY);
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                            RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.drawImage(originalImage, 0, 0, getWidth(), getHeight(), this);
                        g2d.dispose();
                    }
                } catch (Exception e) {
                    // Silently handle
                }
            }
        };
        leftImgPanel.setLayout(new BorderLayout());
        leftImgPanel.setPreferredSize(new Dimension(400, 600));
        
        // ===== RIGHT PANEL WITH REGISTER FORM =====
        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // App Logo/Icon at top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        try {
            ImageIcon appLogo = new ImageIcon("src/images/arc_logo.png");
            Image scaledLogo = appLogo.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
            registerPanel.add(logoLabel, gbc);
        } catch (Exception e) {
            // Skip if no logo
        }
        
        // Title
        gbc.gridy = 1;
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        registerPanel.add(titleLabel, gbc);
        
        // Subtitle
        gbc.gridy = 2;
        JLabel subtitleLabel = new JLabel("Join the Arc community today");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        registerPanel.add(subtitleLabel, gbc);
        
        // Spacer
        gbc.gridy = 3;
        registerPanel.add(Box.createVerticalStrut(10), gbc);
        
        // ===== USERNAME FIELD WITH LABEL =====
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel usernameContainer = new JPanel(new BorderLayout(5, 5));
        usernameContainer.setBackground(Color.WHITE);
        
        // Username Label
        JLabel usernameLabel = new JLabel("USERNAME");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        usernameLabel.setForeground(new Color(120, 120, 120));
        usernameContainer.add(usernameLabel, BorderLayout.NORTH);
        
        // Username input panel
        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.setBackground(Color.WHITE);
        usernamePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        try {
            ImageIcon usernameImg = new ImageIcon("src/images/username.png");
            Image scaledUsername = usernameImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel usernameIcon = new JLabel(new ImageIcon(scaledUsername));
            usernamePanel.add(usernameIcon, BorderLayout.WEST);
        } catch (Exception e) {
            JLabel usernameIcon = new JLabel("👤");
            usernameIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            usernamePanel.add(usernameIcon, BorderLayout.WEST);
        }

        usernameField = new JTextField(15);
        usernameField.setBorder(null);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setToolTipText("Choose a username");
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        
        usernameContainer.add(usernamePanel, BorderLayout.CENTER);
        registerPanel.add(usernameContainer, gbc);
        
        // ===== EMAIL FIELD WITH LABEL =====
        gbc.gridy = 5;
        
        JPanel emailContainer = new JPanel(new BorderLayout(5, 5));
        emailContainer.setBackground(Color.WHITE);
        
        // Email Label
        JLabel emailLabel = new JLabel("EMAIL ADDRESS");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        emailLabel.setForeground(new Color(120, 120, 120));
        emailContainer.add(emailLabel, BorderLayout.NORTH);
        
        // Email input panel
        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setBackground(Color.WHITE);
        emailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        try {
            ImageIcon emailImg = new ImageIcon("src/images/mail.png");
            Image scaledEmail = emailImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel emailIcon = new JLabel(new ImageIcon(scaledEmail));
            emailPanel.add(emailIcon, BorderLayout.WEST);
        } catch (Exception e) {
            JLabel emailIcon = new JLabel("📧");
            emailIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emailPanel.add(emailIcon, BorderLayout.WEST);
        }

        emailField = new JTextField(15);
        emailField.setBorder(null);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setToolTipText("Enter your email address");
        emailPanel.add(emailField, BorderLayout.CENTER);
        
        emailContainer.add(emailPanel, BorderLayout.CENTER);
        registerPanel.add(emailContainer, gbc);
        
        // ===== PASSWORD FIELD WITH LABEL =====
        gbc.gridy = 6;
        
        JPanel passwordContainer = new JPanel(new BorderLayout(5, 5));
        passwordContainer.setBackground(Color.WHITE);
        
        // Password Label
        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passwordLabel.setForeground(new Color(120, 120, 120));
        passwordContainer.add(passwordLabel, BorderLayout.NORTH);
        
        // Password input panel
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        try {
            ImageIcon passwordImg = new ImageIcon("src/images/password.png");
            Image scaledPassword = passwordImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel passwordIcon = new JLabel(new ImageIcon(scaledPassword));
            passwordPanel.add(passwordIcon, BorderLayout.WEST);
        } catch (Exception e) {
            JLabel passwordIcon = new JLabel("🔒");
            passwordIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            passwordPanel.add(passwordIcon, BorderLayout.WEST);
        }
        
        passwordField = new JPasswordField(15);
        passwordField.setBorder(null);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setToolTipText("Create a password (min. 6 characters)");
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        passwordContainer.add(passwordPanel, BorderLayout.CENTER);
        registerPanel.add(passwordContainer, gbc);
        
        // ===== CONFIRM PASSWORD FIELD WITH LABEL =====
        gbc.gridy = 7;
        
        JPanel confirmContainer = new JPanel(new BorderLayout(5, 5));
        confirmContainer.setBackground(Color.WHITE);
        
        // Confirm Password Label
        JLabel confirmLabel = new JLabel("CONFIRM PASSWORD");
        confirmLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        confirmLabel.setForeground(new Color(120, 120, 120));
        confirmContainer.add(confirmLabel, BorderLayout.NORTH);
        
        // Confirm password input panel
        JPanel confirmPanel = new JPanel(new BorderLayout());
        confirmPanel.setBackground(Color.WHITE);
        confirmPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        try {
            ImageIcon confirmImg = new ImageIcon("src/images/password.png");
            Image scaledConfirm = confirmImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel confirmIcon = new JLabel(new ImageIcon(scaledConfirm));
            confirmPanel.add(confirmIcon, BorderLayout.WEST);
        } catch (Exception e) {
            JLabel confirmIcon = new JLabel("✓");
            confirmIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
            confirmPanel.add(confirmIcon, BorderLayout.WEST);
        }
        
        confirmField = new JPasswordField(15);
        confirmField.setBorder(null);
        confirmField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmField.setToolTipText("Confirm your password");
        confirmPanel.add(confirmField, BorderLayout.CENTER);
        
        confirmContainer.add(confirmPanel, BorderLayout.CENTER);
        registerPanel.add(confirmContainer, gbc);
        
        // Terms and Conditions Checkbox
        gbc.gridy = 8;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        JCheckBox termsCheckbox = new JCheckBox("I agree to the Terms and Conditions");
        termsCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        termsCheckbox.setBackground(Color.WHITE);
        termsCheckbox.setForeground(Color.GRAY);
        registerPanel.add(termsCheckbox, gbc);
        
        // Register Button
        gbc.gridy = 9;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton registerBtn = new JButton("CREATE ACCOUNT");
        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setPreferredSize(new Dimension(250, 45));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(39, 174, 96));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(46, 204, 113));
            }
        });
        
        registerBtn.addActionListener(e -> register(termsCheckbox.isSelected()));
        registerPanel.add(registerBtn, gbc);
        
        // Back to Login Section
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 10, 20, 10);
        
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        backPanel.setBackground(Color.WHITE);
        
        JLabel haveAccountLabel = new JLabel("Already have an account?");
        haveAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        haveAccountLabel.setForeground(Color.GRAY);
        
        JButton backBtn = new JButton("Sign In");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setForeground(new Color(46, 204, 113));
        backBtn.setBorder(null);
        backBtn.setBackground(Color.WHITE);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });
        
        backPanel.add(haveAccountLabel);
        backPanel.add(backBtn);
        registerPanel.add(backPanel, gbc);
        
        // Add panels to frame
        add(leftImgPanel, BorderLayout.WEST);
        add(registerPanel, BorderLayout.CENTER);
        
        leftImgPanel.setBackground(new Color(46, 204, 113));
    }
    
    private void register(boolean termsAccepted) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            return;
        }
        
        if (!termsAccepted) {
            JOptionPane.showMessageDialog(this, "Please accept the Terms and Conditions!");
            return;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                new LoginGUI().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed!");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterGUI().setVisible(true));
    }
}