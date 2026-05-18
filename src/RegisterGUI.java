import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;

public class RegisterGUI extends JFrame {

    private JTextField     usernameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JCheckBox      termsCheckbox;

    // ── Palette: same night-sky purple as LoginGUI ───────────────────────────
    private static final Color BG_TOP        = new Color(0x0A0818);
    private static final Color BG_BOT        = new Color(0x2D1B69);
    private static final Color GLASS_FILL    = new Color(20, 14, 50, 175);
    private static final Color GLASS_BORDER  = new Color(255, 255, 255, 50);
    private static final Color FIELD_FILL    = new Color(255, 255, 255, 18);
    private static final Color FIELD_BORDER  = new Color(255, 255, 255, 55);
    private static final Color FIELD_FOCUS   = new Color(140, 120, 255, 180);
    private static final Color WHITE         = Color.WHITE;
    private static final Color WHITE_DIM     = new Color(255, 255, 255, 165);
    private static final Color WHITE_FAINT   = new Color(255, 255, 255, 90);
    private static final Color ACCENT        = new Color(0xA78BFA);
    private static final Color ACCENT_BRIGHT = new Color(0xC4B5FD);
    private static final Color BTN_FILL      = new Color(0x7C3AED);
    private static final Color DANGER        = new Color(0xFF6B6B);

    private static final int[][] STARS = generateStars(120);

    public RegisterGUI() {
        setTitle("ARC. — Register");
        setSize(1080, 720);
        setMinimumSize(new Dimension(1080, 720));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try { setIconImage(new ImageIcon("src/images/arc_logo.png").getImage()); }
        catch (Exception ignored) {}

        // ── Root: starfield gradient background ───────────────────────────
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();

                // vertical gradient
                g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, H, BG_BOT));
                g2.fillRect(0, 0, W, H);

                // violet glow top-centre
                g2.setPaint(new RadialGradientPaint(
                    W * 0.5f, H * 0.15f, H * 0.5f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(108, 71, 255, 55), new Color(0,0,0,0)}));
                g2.fillRect(0, 0, W, H);

                // second glow bottom-left
                g2.setPaint(new RadialGradientPaint(
                    W * 0.18f, H * 0.82f, H * 0.38f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(80, 30, 180, 42), new Color(0,0,0,0)}));
                g2.fillRect(0, 0, W, H);

                // stars
                for (int[] s : STARS) {
                    int sx = (int)(s[0] / 1000.0 * W);
                    int sy = (int)(s[1] / 1000.0 * H);
                    g2.setColor(new Color(255, 255, 255, s[3]));
                    g2.fillOval(sx - s[2], sy - s[2], s[2]*2, s[2]*2);
                }
                g2.dispose();
            }
        };
        root.setBackground(BG_TOP);

        JPanel card = buildCard();
        root.add(card, new GridBagConstraints());
        setContentPane(root);
    }

    // ── Card ─────────────────────────────────────────────────────────────────

    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // outer glow
                for (int i = 8; i >= 1; i--) {
                    g2.setColor(new Color(120, 80, 255, i * 4));
                    g2.setStroke(new BasicStroke(i * 2.2f));
                    g2.drawRoundRect(i, i, getWidth()-i*2, getHeight()-i*2, 28, 28);
                }
                // glass fill
                g2.setColor(GLASS_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                // top shine
                g2.setPaint(new GradientPaint(
                    0, 0, new Color(255,255,255,28),
                    0, getHeight()/3f, new Color(255,255,255,0)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight()/2, 28, 28);
                // border
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 28, 28);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(500, 660));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.CENTER;
        g.weightx = 1;

        // ── Logo ─────────────────────────────────────────────────────────
        g.gridy = 0; g.insets = new Insets(32, 44, 4, 44);
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoRow.setOpaque(false);
        JLabel logoLbl = new JLabel("", SwingConstants.CENTER);
        try {
            Image img = new ImageIcon("src/images/white_logo.png")
                            .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoLbl.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            logoLbl = new JLabel() {
                @Override protected void paintComponent(Graphics g2) {
                    Graphics2D g2d = (Graphics2D) g2.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(167, 139, 250, 60));
                    g2d.setStroke(new BasicStroke(6f));
                    g2d.drawOval(6, 6, 40, 40);
                    float[] dash = {4f, 4f};
                    g2d.setColor(ACCENT_BRIGHT);
                    g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, dash, 0f));
                    g2d.drawOval(10, 10, 32, 32);
                    g2d.setColor(WHITE);
                    g2d.fillOval(20, 20, 12, 12);
                    g2d.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(52, 52); }
            };
        }
        logoRow.add(logoLbl);
        card.add(logoRow, g);

        // ── Title ─────────────────────────────────────────────────────────
        g.gridy = 1; g.insets = new Insets(4, 44, 2, 44);
        JLabel title = new JLabel(
            "<html><center><span style='color:white'>Create an </span>"
            + "<span style='color:#C4B5FD;font-style:italic'>Account!</span></center></html>",
            SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        card.add(title, g);

        // ── Subtitle ──────────────────────────────────────────────────────
        g.gridy = 2; g.insets = new Insets(0, 56, 18, 56);
        JLabel sub = new JLabel(
            "<html><center>Join Arc and start managing your schedule<br>with ease today</center></html>",
            SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(WHITE_DIM);
        card.add(sub, g);

        // ── Two-column row: Username + Email ──────────────────────────────
        g.gridy = 3; g.insets = new Insets(0, 40, 10, 40);
        card.add(buildTwoColumnRow(), g);

        // ── Password ──────────────────────────────────────────────────────
        g.gridy = 4; g.insets = new Insets(0, 40, 10, 40);
        card.add(buildField("Password", true, false), g);

        // ── Confirm Password ──────────────────────────────────────────────
        g.gridy = 5; g.insets = new Insets(0, 40, 10, 40);
        card.add(buildField("Confirm Password", true, true), g);

        // ── Terms ────────────────────────────────────────────────────────
        g.gridy = 6; g.insets = new Insets(2, 40, 16, 40);
        termsCheckbox = new JCheckBox("I agree to the Terms and Conditions");
        termsCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        termsCheckbox.setForeground(WHITE_DIM);
        termsCheckbox.setOpaque(false);
        termsCheckbox.setFocusPainted(false);
        card.add(termsCheckbox, g);

        // ── Create Account button ─────────────────────────────────────────
        g.gridy = 7; g.insets = new Insets(0, 40, 14, 40);
        card.add(buildCreateBtn(), g);

        // ── Sign In link ──────────────────────────────────────────────────
        g.gridy = 8; g.insets = new Insets(0, 40, 32, 40);
        card.add(buildSignInRow(), g);

        return card;
    }

    // ── Two-column row (Username | Email) ─────────────────────────────────────

    private JPanel buildTwoColumnRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.add(buildField("Username", false, false));
        row.add(buildEmailField());
        return row;
    }

    // ── Generic field builder ─────────────────────────────────────────────────

    private JPanel buildField(String labelTxt, boolean isPw, boolean isConfirm) {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setOpaque(false);

        JLabel lbl = new JLabel(labelTxt);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(WHITE_DIM);
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel inputRow = makeInputRow();
        inputRow.setPreferredSize(new Dimension(0, 50));

        if (isPw) {
            JPasswordField f = new JPasswordField();
            f.setEchoChar('•');
            styleField(f, isConfirm ? "Confirm password" : "Enter password");
            if (isConfirm) confirmField = f; else passwordField = f;
            inputRow.add(f, BorderLayout.CENTER);

            // Eye toggle
            JLabel eye = new JLabel();
            ImageIcon seeIcon = loadIcon("src/images/see_password.png", 18, 18);
            if (seeIcon != null) {
                eye.setIcon(seeIcon);
            } else {
                eye.setText("◎");
                eye.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                eye.setForeground(WHITE_FAINT);
            }
            eye.setOpaque(false);
            eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final JPasswordField target = f;
            eye.addMouseListener(new MouseAdapter() {
                boolean vis = false;
                @Override public void mouseClicked(MouseEvent e) {
                    vis = !vis;
                    target.setEchoChar(vis ? (char)0 : '•');
                    if (seeIcon == null) eye.setText(vis ? "◉" : "◎");
                }
                @Override public void mouseEntered(MouseEvent e) { if (seeIcon == null) eye.setForeground(ACCENT_BRIGHT); }
                @Override public void mouseExited (MouseEvent e) { if (seeIcon == null) eye.setForeground(WHITE_FAINT); }
            });
            inputRow.add(eye, BorderLayout.EAST);
        } else {
            usernameField = new JTextField();
            styleField(usernameField, "Enter username");
            inputRow.add(usernameField, BorderLayout.CENTER);
        }

        wrap.add(inputRow, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildEmailField() {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setOpaque(false);

        JLabel lbl = new JLabel("Email Address");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(WHITE_DIM);
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel inputRow = makeInputRow();
        inputRow.setPreferredSize(new Dimension(0, 50));
        emailField = new JTextField();
        styleField(emailField, "Enter email");
        inputRow.add(emailField, BorderLayout.CENTER);
        wrap.add(inputRow, BorderLayout.CENTER);
        return wrap;
    }

    // ── Shared translucent input row ──────────────────────────────────────────

    private JPanel makeInputRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            boolean focused = false;
            {
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(focused ? new Color(255,255,255,28) : FIELD_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(focused ? FIELD_FOCUS : FIELD_BORDER);
                g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        return row;
    }

    private void styleField(JTextField f, String ph) {
        f.setBorder(null);
        f.setOpaque(false);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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

    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── Create Account button ─────────────────────────────────────────────────

    private JButton buildCreateBtn() {
        JButton btn = new JButton("Create Account") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setPaint(new GradientPaint(
                        0, 0, new Color(0xA78BFA),
                        getWidth(), getHeight(), new Color(0x6D28D9)));
                } else {
                    g2.setColor(BTN_FILL);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 52));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> register());
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed (MouseEvent e) { btn.setForeground(new Color(0x3B1F8C)); btn.repaint(); }
            @Override public void mouseReleased(MouseEvent e) { btn.setForeground(new Color(0x1E0A4B)); btn.repaint(); }
        });
        return btn;
    }

    // ── Sign-in row ───────────────────────────────────────────────────────────

    private JPanel buildSignInRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);

        JLabel already = new JLabel("Already have an account?");
        already.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        already.setForeground(WHITE_DIM);

        JButton signIn = new JButton("Sign In");
        signIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signIn.setForeground(ACCENT_BRIGHT);
        signIn.setContentAreaFilled(false);
        signIn.setBorderPainted(false);
        signIn.setFocusPainted(false);
        signIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signIn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { signIn.setForeground(WHITE); }
            @Override public void mouseExited (MouseEvent e) { signIn.setForeground(ACCENT_BRIGHT); }
        });
        signIn.addActionListener(e -> { new LoginGUI().setVisible(true); dispose(); });

        row.add(already);
        row.add(signIn);
        return row;
    }

    // ── Star field ────────────────────────────────────────────────────────────

    private static int[][] generateStars(int count) {
        int[][] stars = new int[count][4];
        java.util.Random rnd = new java.util.Random(99);
        for (int i = 0; i < count; i++) {
            stars[i][0] = rnd.nextInt(1000);
            stars[i][1] = rnd.nextInt(700);
            stars[i][2] = rnd.nextInt(2) + 1;
            stars[i][3] = 60 + rnd.nextInt(160);
        }
        return stars;
    }

    // ── Register logic ────────────────────────────────────────────────────────

    private void register() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (isEmpty(username, "Enter username") || isEmpty(email, "Enter email")
                || password.isEmpty() || confirm.isEmpty()) {
            showMsg("Please fill in all fields!"); return;
        }
        if (!termsCheckbox.isSelected()) {
            showMsg("Please accept the Terms and Conditions!"); return;
        }
        if (!password.equals(confirm)) {
            showMsg("Passwords do not match!"); return;
        }
        if (password.length() < 6) {
            showMsg("Password must be at least 6 characters!"); return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES (?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                new LoginGUI().setVisible(true);
                dispose();
            } else {
                showMsg("Registration failed!");
            }
        } catch (SQLException e) {
            showMsg(e.getMessage().contains("Duplicate")
                ? "Username already exists!" : "Database error: " + e.getMessage());
        }
    }

    private boolean isEmpty(String val, String placeholder) {
        return val.isEmpty() || val.equals(placeholder);
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterGUI().setVisible(true));
    }
}