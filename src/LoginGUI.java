import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;

public class LoginGUI extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JCheckBox      rememberMe;
    private User           currentUser;

    // ── Palette: deep navy-purple night sky ──────────────────────────────────
    private static final Color BG_TOP       = new Color(0x0A0818);   // near-black indigo
    private static final Color BG_MID       = new Color(0x1A1040);   // deep navy purple
    private static final Color BG_BOT       = new Color(0x2D1B69);   // rich purple
    private static final Color GLOW_COLOR   = new Color(0x6C47FF);   // violet glow
    private static final Color GLASS_FILL   = new Color(20, 14, 50, 175); // translucent dark
    private static final Color GLASS_BORDER = new Color(255, 255, 255, 50);
    private static final Color FIELD_FILL   = new Color(255, 255, 255, 18);
    private static final Color FIELD_BORDER = new Color(255, 255, 255, 55);
    private static final Color FIELD_FOCUS  = new Color(140, 120, 255, 180);
    private static final Color WHITE        = Color.WHITE;
    private static final Color WHITE_DIM    = new Color(255, 255, 255, 165);
    private static final Color WHITE_FAINT  = new Color(255, 255, 255, 90);
    private static final Color ACCENT       = new Color(0xA78BFA);   // lavender
    private static final Color ACCENT_BRIGHT= new Color(0xC4B5FD);
    private static final Color BTN_FILL     = new Color(0x7C3AED);
    private static final Color STAR_COLOR   = new Color(255, 255, 255, 200);

    // star field data (generated once)
    private static final int[][] STARS = generateStars(120);

    public LoginGUI() {
        setTitle("ARC. — Login");
        setSize(1080, 720);
        setMinimumSize(new Dimension(1080, 720));
        setResizable(false);
        setUndecorated(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try { setIconImage(new ImageIcon("src/images/arc_logo.png").getImage()); }
        catch (Exception ignored) {}

        // ── Root panel: animated starfield gradient background ────────────
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight();

                // 3-stop vertical gradient
                GradientPaint grad = new GradientPaint(
                    0, 0,   BG_TOP,
                    0, H,   BG_BOT);
                g2.setPaint(grad);
                g2.fillRect(0, 0, W, H);

                // soft violet glow — top-centre (like moonlight)
                RadialGradientPaint glow = new RadialGradientPaint(
                    W * 0.5f, H * 0.18f, H * 0.55f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(108, 71, 255, 55), new Color(0,0,0,0)});
                g2.setPaint(glow);
                g2.fillRect(0, 0, W, H);

                // second glow — bottom-right
                RadialGradientPaint glow2 = new RadialGradientPaint(
                    W * 0.82f, H * 0.78f, H * 0.40f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(80, 30, 180, 45), new Color(0,0,0,0)});
                g2.setPaint(glow2);
                g2.fillRect(0, 0, W, H);

                // draw stars
                for (int[] star : STARS) {
                    int sx = (int)(star[0] / 1000.0 * W);
                    int sy = (int)(star[1] / 1000.0 * H);
                    int  r = star[2];
                    int  a = star[3];
                    g2.setColor(new Color(255, 255, 255, a));
                    g2.fillOval(sx - r, sy - r, r * 2, r * 2);
                }

                // subtle horizontal line at horizon (~35% down)
                g2.setColor(new Color(255, 255, 255, 8));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, (int)(H * 0.35), W, (int)(H * 0.35));

                g2.dispose();
            }
        };
        root.setBackground(BG_TOP);

        // ── Card ─────────────────────────────────────────────────────────
        JPanel card = buildCard();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(card, gbc);

        setContentPane(root);
    }

    // ── Card ─────────────────────────────────────────────────────────────────

    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                // subtle outer glow
                for (int i = 8; i >= 1; i--) {
                    g2.setColor(new Color(120, 80, 255, i * 4));
                    g2.setStroke(new BasicStroke(i * 2.2f));
                    g2.drawRoundRect(i, i, getWidth()-i*2, getHeight()-i*2, 28, 28);
                }

                // glass fill
                g2.setColor(GLASS_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

                // inner top highlight
                GradientPaint shine = new GradientPaint(
                    0, 0, new Color(255,255,255,30),
                    0, getHeight()/3f, new Color(255,255,255,0));
                g2.setPaint(shine);
                g2.fillRoundRect(0, 0, getWidth(), getHeight()/2, 28, 28);

                // border
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 28, 28);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(480, 580));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 1;
        g.fill  = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.CENTER;
        g.weightx = 1;

        // ── Logo / icon ───────────────────────────────────────────────────
        g.gridy = 0; g.insets = new Insets(36, 44, 6, 44);
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoRow.setOpaque(false);
        JLabel logoLbl = new JLabel("", SwingConstants.CENTER);
        try {
            Image img = new ImageIcon("src/images/white_logo.png")
                            .getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
            logoLbl.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            // Draw a glowing arc icon as fallback
            logoLbl = new JLabel() {
                @Override protected void paintComponent(Graphics g2) {
                    Graphics2D g2d = (Graphics2D) g2.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    // outer glow ring
                    g2d.setColor(new Color(167, 139, 250, 60));
                    g2d.setStroke(new BasicStroke(6f));
                    g2d.drawOval(6, 6, 44, 44);
                    // inner dashed ring
                    float[] dash = {4f, 4f};
                    g2d.setColor(ACCENT_BRIGHT);
                    g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND, 0f, dash, 0f));
                    g2d.drawOval(10, 10, 36, 36);
                    // centre dot
                    g2d.setColor(WHITE);
                    g2d.fillOval(22, 22, 12, 12);
                    g2d.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(56, 56); }
            };
        }
        logoRow.add(logoLbl);
        card.add(logoRow, g);

        // ── Title ─────────────────────────────────────────────────────────
        g.gridy = 1; g.insets = new Insets(6, 44, 4, 44);
        JLabel title = new JLabel(
            "<html><center>"
            + "<span style='color:white'>Welcome </span>"
            + "<span style='color:#C4B5FD;font-style:italic'>back!</span>"
            + "</center></html>", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        card.add(title, g);

        // ── Subtitle ──────────────────────────────────────────────────────
        g.gridy = 2; g.insets = new Insets(0, 56, 24, 56);
        JLabel sub = new JLabel(
            "<html><center>Sign in to access your scheduled events<br>and personal planner</center></html>",
            SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(WHITE_DIM);
        card.add(sub, g);

        // ── Username ──────────────────────────────────────────────────────
        g.gridy = 3; g.insets = new Insets(0, 40, 12, 40);
        card.add(buildField("Username", false), g);

        // ── Password ──────────────────────────────────────────────────────
        g.gridy = 4; g.insets = new Insets(0, 40, 10, 40);
        card.add(buildField("Password", true), g);

        // ── Remember / Forgot ─────────────────────────────────────────────
        g.gridy = 5; g.insets = new Insets(0, 40, 22, 40);
        card.add(buildRememberForgot(), g);

        // ── Login button ──────────────────────────────────────────────────
        g.gridy = 6; g.insets = new Insets(0, 40, 20, 40);
        card.add(buildLoginBtn(), g);

        // ── Sign-up link ──────────────────────────────────────────────────
        g.gridy = 7; g.insets = new Insets(0, 40, 36, 40);
        card.add(buildSignUpRow(), g);

        return card;
    }

    // ── Input field ───────────────────────────────────────────────────────────

    private JPanel buildField(String labelTxt, boolean isPw) {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setOpaque(false);

        JLabel lbl = new JLabel(labelTxt);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(WHITE_DIM);
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            boolean focused = false;
            {
                // detect focus change from child
                addContainerListener(new java.awt.event.ContainerAdapter() {
                    @Override public void componentAdded(java.awt.event.ContainerEvent e) {
                        e.getChild().addFocusListener(new FocusAdapter() {
                            @Override public void focusGained(FocusEvent fe) { focused = true;  repaint(); }
                            @Override public void focusLost (FocusEvent fe) { focused = false; repaint(); }
                        });
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(focused ? new Color(255,255,255,28) : FIELD_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(focused ? FIELD_FOCUS : FIELD_BORDER);
                g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 18));
        row.setPreferredSize(new Dimension(0, 52));

        if (isPw) {
            passwordField = new JPasswordField();
            passwordField.setEchoChar('•');
            style(passwordField, "Enter your password");
            row.add(passwordField, BorderLayout.CENTER);

            JLabel eye = new JLabel("◎");
            eye.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            eye.setForeground(WHITE_FAINT);
            eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eye.addMouseListener(new MouseAdapter() {
                boolean visible = false;
                @Override public void mouseClicked(MouseEvent e) {
                    visible = !visible;
                    passwordField.setEchoChar(visible ? (char)0 : '•');
                    eye.setText(visible ? "◉" : "◎");
                }
                @Override public void mouseEntered(MouseEvent e) { eye.setForeground(ACCENT_BRIGHT); }
                @Override public void mouseExited (MouseEvent e) { eye.setForeground(WHITE_FAINT); }
            });
            row.add(eye, BorderLayout.EAST);
        } else {
            usernameField = new JTextField();
            style(usernameField, "Enter your username");
            row.add(usernameField, BorderLayout.CENTER);
        }

        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private void style(JTextField f, String ph) {
        f.setBorder(null);
        f.setOpaque(false);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setForeground(WHITE_FAINT);
        f.setCaretColor(WHITE);
        f.setText(ph);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) { f.setText(""); f.setForeground(WHITE); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(ph); f.setForeground(WHITE_FAINT); }
            }
        });
    }

    // ── Remember / Forgot ─────────────────────────────────────────────────────

    private JPanel buildRememberForgot() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMe.setForeground(WHITE_DIM);
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);

        JLabel forgot = new JLabel("Forgot password?");
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgot.setForeground(ACCENT);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { forgot.setForeground(ACCENT_BRIGHT); }
            @Override public void mouseExited (MouseEvent e) { forgot.setForeground(ACCENT); }
        });

        row.add(rememberMe, BorderLayout.WEST);
        row.add(forgot,     BorderLayout.EAST);
        return row;
    }

    // ── Login button ──────────────────────────────────────────────────────────

    private JButton buildLoginBtn() {
        JButton btn = new JButton("Log In") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0xA78BFA),
                        getWidth(), getHeight(), new Color(0x6D28D9));
                    g2.setPaint(gp);
                } else {
                    g2.setColor(BTN_FILL);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 54));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> login());
        // press effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                btn.setForeground(Color.WHITE); btn.repaint(); }
            @Override public void mouseReleased(MouseEvent e) {
                btn.setForeground(Color.WHITE); btn.repaint(); }
        });
        return btn;
    }

    // ── Sign-up row ───────────────────────────────────────────────────────────

    private JPanel buildSignUpRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);

        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noAcc.setForeground(WHITE_DIM);

        JButton signUp = new JButton("Sign Up");
        signUp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signUp.setForeground(ACCENT_BRIGHT);
        signUp.setContentAreaFilled(false);
        signUp.setBorderPainted(false);
        signUp.setFocusPainted(false);
        signUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUp.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { signUp.setForeground(WHITE); }
            @Override public void mouseExited (MouseEvent e) { signUp.setForeground(ACCENT_BRIGHT); }
        });
        signUp.addActionListener(e -> openRegister());

        row.add(noAcc);
        row.add(signUp);
        return row;
    }

    // ── Star field generator ──────────────────────────────────────────────────

    private static int[][] generateStars(int count) {
        int[][] stars = new int[count][4];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < count; i++) {
            stars[i][0] = rnd.nextInt(1000);          // x (0-1000 = 0-100%)
            stars[i][1] = rnd.nextInt(700);           // y (biased toward top)
            stars[i][2] = rnd.nextInt(2) + 1;         // radius 1-2
            stars[i][3] = 60 + rnd.nextInt(160);      // alpha 60-220
        }
        return stars;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || username.equals("Enter your username") ||
            password.isEmpty() || password.equals("Enter your password")) {
            showError("Please enter your username and password.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUser = new User(rs.getInt("id"),
                                       rs.getString("username"),
                                       rs.getString("email"));
                new DashboardGUI(currentUser).setVisible(true);
                dispose();
            } else {
                showError("Invalid username or password.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openRegister() {
        new RegisterGUI().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}