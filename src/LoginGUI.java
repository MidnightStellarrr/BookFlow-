import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private User currentUser;
    
    public LoginGUI() {
        setTitle("Arc - Login");
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
                    // Silently handle - image just won't show
                }
            }
        };
        leftImgPanel.setLayout(new BorderLayout());
        leftImgPanel.setPreferredSize(new Dimension(400, 600));
        
        // ===== RIGHT PANEL WITH LOGIN FORM =====
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
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
            loginPanel.add(logoLabel, gbc);
        } catch (Exception e) {
            // Skip if no logo
        }
        
        // Title
        gbc.gridy = 1;
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        loginPanel.add(titleLabel, gbc);
        
        // Subtitle
        gbc.gridy = 2;
        JLabel subtitleLabel = new JLabel("Please login to your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        loginPanel.add(subtitleLabel, gbc);
        
        // Spacer
        gbc.gridy = 3;
        loginPanel.add(Box.createVerticalStrut(20), gbc);
        
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
        JPanel usernameInputPanel = new JPanel(new BorderLayout());
        usernameInputPanel.setBackground(Color.WHITE);
        usernameInputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Username field with image icon (with try-catch)
        try {
            ImageIcon usernameImg = new ImageIcon("src/images/username.png");
            Image scaledUsername = usernameImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel usernameIcon = new JLabel(new ImageIcon(scaledUsername));
            usernameInputPanel.add(usernameIcon, BorderLayout.WEST);
        } catch (Exception e) {
            // Fallback to text if image not found
            JLabel usernameIcon = new JLabel("👤");
            usernameIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            usernameInputPanel.add(usernameIcon, BorderLayout.WEST);
        }

        usernameField = new JTextField(15);
        usernameField.setBorder(null);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setToolTipText("Enter your username");
        usernameInputPanel.add(usernameField, BorderLayout.CENTER);
        
        usernameContainer.add(usernameInputPanel, BorderLayout.CENTER);
        loginPanel.add(usernameContainer, gbc);
        
        // ===== PASSWORD FIELD WITH LABEL =====
        gbc.gridy = 5;
        
        JPanel passwordContainer = new JPanel(new BorderLayout(5, 5));
        passwordContainer.setBackground(Color.WHITE);
        
        // Password Label
        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passwordLabel.setForeground(new Color(120, 120, 120));
        passwordContainer.add(passwordLabel, BorderLayout.NORTH);
        
        // Password input panel
        JPanel passwordInputPanel = new JPanel(new BorderLayout());
        passwordInputPanel.setBackground(Color.WHITE);
        passwordInputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Password field with image icon (with try-catch)
        try {
            ImageIcon passwordImg = new ImageIcon("src/images/password.png");
            Image scaledPassword = passwordImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel passwordIcon = new JLabel(new ImageIcon(scaledPassword));
            passwordInputPanel.add(passwordIcon, BorderLayout.WEST);
        } catch (Exception e) {
            // Fallback to text if image not found
            JLabel passwordIcon = new JLabel("🔒");
            passwordIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            passwordInputPanel.add(passwordIcon, BorderLayout.WEST);
        }
        
        passwordField = new JPasswordField(15);
        passwordField.setBorder(null);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setToolTipText("Enter your password");
        passwordInputPanel.add(passwordField, BorderLayout.CENTER);
        
        passwordContainer.add(passwordInputPanel, BorderLayout.CENTER);
        loginPanel.add(passwordContainer, gbc);
        
        // Forgot Password Link
        gbc.gridy = 6;
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotPanel.setBackground(Color.WHITE);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(100, 149, 237));
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPanel.add(forgotLabel);
        loginPanel.add(forgotPanel, gbc);
        
        // Login Button
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBackground(new Color(100, 149, 237));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(250, 45));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(70, 130, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(100, 149, 237));
            }
        });
        
        loginBtn.addActionListener(e -> login());
        loginPanel.add(loginBtn, gbc);
        
        // Register Section
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 10, 20, 10);
        
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerPanel.setBackground(Color.WHITE);
        
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noAccountLabel.setForeground(Color.GRAY);
        
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerBtn.setForeground(new Color(100, 149, 237));
        registerBtn.setBorder(null);
        registerBtn.setBackground(Color.WHITE);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> openRegister());
        
        registerPanel.add(noAccountLabel);
        registerPanel.add(registerBtn);
        loginPanel.add(registerPanel, gbc);
        
        // Add panels to frame
        add(leftImgPanel, BorderLayout.WEST);
        add(loginPanel, BorderLayout.CENTER);
        
        // Set left panel background color while image loads
        leftImgPanel.setBackground(new Color(100, 149, 237));
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUser = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email")
                );
                
                JOptionPane.showMessageDialog(this, "Welcome " + username + "!");
                
                // Open dashboard
                new DashboardGUI(currentUser).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
    
    private void openRegister() {
        new RegisterGUI().setVisible(true);
        dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}